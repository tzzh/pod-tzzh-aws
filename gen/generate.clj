(ns generate
  (:require [clojure.java.shell :as shell]
            [clojure.string :as s]))

(def SDK-VERSION
  "v1.34.28")

(def SERVICES
  "Services to generate code for"
  ["athena"
   "glue"
   "kafka"
   "kinesis"
   "lambda"
   "sqs"
   "ssm"
   "sns"
   "sts"
   "dynamodb"
   "s3"])

(defn read-service-source
  "Read the source file from a given service in the go sdk"
  [service-name]
  (-> (format "https://raw.githubusercontent.com/aws/aws-sdk-go/%s/service/%s/api.go" SDK-VERSION service-name)
      slurp))

(defn find-fns
  "Extract all the fns to be generated from the source file"
  [service-source]
  (->> service-source
       (re-seq #"func .* (\w+(?<!(Pages|Request)))\(input.*Input")
       (map (fn [[_ f]] f))))

(defn find-services-fns
  "Build a sorted map of service: fns for the given services"
  [services]
  (reduce (fn [acc service-name]
            (assoc acc service-name (-> service-name
                                        read-service-source
                                        find-fns)))
          (sorted-map)
          services))

(defn clj-fn-name
  "Converts from the the aws sdk to the cljfunction name"
  [aws-fn-name]
  (->> (s/split aws-fn-name #"(?=[A-Z])")
       (map s/lower-case)
       (s/join "-")))

(defn aws-input-name
  [aws-fn-name]
  (str aws-fn-name "Input"))

(defn aws-output-name
  [aws-fn-name]
  (str aws-fn-name "Output"))

(def quoted-get-paginator
  (quote
    (defn get-paginator
      "Returns a fn that lazily fetches the pages for a given aws fn"
      [page-fn]
      (fn get-pages
        ([]
         (get-pages {}))
        ([input]
         (lazy-seq
           (let [page (page-fn input)
                 next-continuation-token (:NextContinuationToken page)
                 next-token (:NextToken page)
                 next-marker (:NextMarker page)] ;; some services use different types of continuation tokens
             (cond next-continuation-token
                     (cons page (get-pages (assoc input :ContinuationToken next-continuation-token)))
                   next-token
                     (cons page (get-pages (assoc input :NextToken next-token)))
                   next-marker
                     (cons page (get-pages (assoc input :Marker next-marker)))
                   :else
                     [page]))))))))

(defn build-imports
  [namespaces]
  (let [aws-imports (->> namespaces
                        (map (fn [[namespace-name _]]
                               (format "\"github.com/aws/aws-sdk-go/service/%s\"" namespace-name)))
                        (s/join "\n"))]
    (format
      "import (
\"errors\"
\"encoding/json\"
\"github.com/aws/aws-sdk-go/aws\"
\"github.com/aws/aws-sdk-go/aws/session\"
%s
\"github.com/tzzh/pod-tzzh-aws/babashka\"
      )
type Config struct {
	Profile string
	Region  string
}
var SessionOptions = session.Options{}
" aws-imports)))

(defn build-ns-vars
  [ns-fns]
  (->> ns-fns
       (map (fn [ns-fn]
              (format "{Name: \"%s\"}," (clj-fn-name ns-fn))))
       (s/join "\n")))

(defn build-describe
  [namespaces]
  (let [ns-list (->> namespaces
                   (map (fn [[namespace-name ns-fns]]
                      (format "{Name: \"pod.tzzh.%s\",
            Vars: []babashka.Var{
        %s
            },
           },
" namespace-name (build-ns-vars ns-fns))))
                   (s/join "\n"))]
  (format "response := &babashka.DescribeResponse{
        Format: \"json\",
        Namespaces: []babashka.Namespace{
            {Name: \"pod.tzzh.paginator\",
             Vars: []babashka.Var{
                {Name: \"get-paginator\",
                 Code: `%s`},
                },
             },
             {Name: \"pod.tzzh.podconfig\",
					Vars: []babashka.Var{
						{Name: \"set-session-options\"},
					},
				},
    %s
        },
    }
  return response, nil
" (str quoted-get-paginator) ns-list)))

(defn build-invoke
  [namespaces]
  (let [ns-list (->> namespaces
                     (mapcat (fn [[namespace-name ns-fns]]
                               (map (fn [ns-fn]
                                      (format "case \"pod.tzzh.%1$s/%2$s\":
            svc := %1$s.New(session.Must(session.NewSessionWithOptions(SessionOptions)))
			input := &%1$s.%4$s{}
			inputList := []%1$s.%4$s{}
			err := json.Unmarshal([]byte(message.Args), &inputList)
			if err != nil {
				return nil, err
			}
            if len(inputList) > 0 {
                input = &inputList[0]
            }
            res, err := svc.%3$s(input)
            if err != nil {
                return nil, err
            }
            return res, nil
" namespace-name (clj-fn-name ns-fn) ns-fn (aws-input-name ns-fn)))
                      ns-fns)))
       (s/join "\n"))]
  (format
    "switch message.Var {
    case \"pod.tzzh.podconfig/set-session-options\":
        var cfg []Config
			err := json.Unmarshal([]byte(message.Args), &cfg)
			if err != nil {
				return nil, err
			}
			SessionOptions = session.Options{
				Config: aws.Config{
					Region: aws.String(cfg[0].Region),
				},
				Profile: cfg[0].Profile,
                SharedConfigState: session.SharedConfigEnable,
			}
			return cfg[0], nil
    %s
}
" ns-list)))

(defn build-process-message
  [namespaces]
  (format "func ProcessMessage(message *babashka.Message) (interface{}, error){
  if message.Op == \"describe\" {
    %s
  } else if message.Op == \"invoke\" {
    %s
  }
  return nil, errors.New(\"Unsupported Operation\")
}" (build-describe namespaces) (build-invoke namespaces)))

(defn build-aws-module
  [namespaces]
  (format "
// Code generated by generate.clj DO NOT EDIT
package aws
%s

%s
" (build-imports namespaces) (build-process-message namespaces)))

(when (= *file* (System/getProperty "babashka.file"))
  (let [aws-go "./aws/aws.go"
        aws-api (find-services-fns SERVICES)]
    (spit aws-go (build-aws-module aws-api))
    (let [{:keys [err exit]} (shell/sh "go" "fmt" aws-go)]
      (when-not (empty? err)
        (print err))
      (System/exit exit))))

(ns generate
  (:require [clojure.string :as s]
            [clojure.java.shell :as shell]
            [babashka.classpath :as c]))
(let [cp (-> (shell/sh "clojure" "-Spath" "-Sdeps" (str '{:deps {comb {:mvn/version "0.1.1"}}}))
             :out
             s/trim)]
  (c/add-classpath cp))
(require '[comb.template :as template])

(def aws-api {"dynamodb" ["BatchGetItem"
                          "BatchWriteItem"
                          "CreateBackup"
                          "CreateGlobalTable"
                          "CreateTable"
                          "DeleteBackup"
                          "DeleteItem"
                          "DeleteTable"
                          "DescribeBackup"
                          "DescribeContinuousBackups"
                          "DescribeContributorInsights"
                          "DescribeEndpoints"
                          "DescribeGlobalTable"
                          "DescribeGlobalTableSettings"
                          "DescribeLimits"
                          "DescribeTable"
                          "DescribeTableReplicaAutoScaling"
                          "DescribeTimeToLive"
                          "GetItem"
                          "ListBackups"
                          "ListContributorInsights"
                          "ListGlobalTables"
                          "ListTables"
                          "ListTagsOfResource"
                          "PutItem"
                          "Query"
                          "RestoreTableFromBackup"
                          "RestoreTableToPointInTime"
                          "Scan"
                          "TagResource"
                          "TransactGetItems"
                          "TransactWriteItems"
                          "UntagResource"
                          "UpdateContinuousBackups"
                          "UpdateContributorInsights"
                          "UpdateGlobalTable"
                          "UpdateGlobalTableSettings"
                          "UpdateItem"
                          "UpdateTable"
                          "UpdateTableReplicaAutoScaling"
                          "UpdateTimeToLive"]
              "s3" ["AbortMultipartUpload"
                    "CompleteMultipartUpload"
                    "CopyObject"
                    "CreateBucket"
                    "CreateMultipartUpload"
                    "DeleteBucket"
                    "DeleteBucketAnalyticsConfiguration"
                    "DeleteBucketCors"
                    "DeleteBucketEncryption"
                    "DeleteBucketInventoryConfiguration"
                    "DeleteBucketLifecycle"
                    "DeleteBucketMetricsConfiguration"
                    "DeleteBucketPolicy"
                    "DeleteBucketReplication"
                    "DeleteBucketTagging"
                    "DeleteBucketWebsite"
                    "DeleteObject"
                    "DeleteObjectTagging"
                    "DeleteObjects"
                    "DeletePublicAccessBlock"
                    "GetBucketAccelerateConfiguration"
                    "GetBucketAcl"
                    "GetBucketAnalyticsConfiguration"
                    "GetBucketCors"
                    "GetBucketEncryption"
                    "GetBucketInventoryConfiguration"
                    "GetBucketLifecycle"
                    "GetBucketLifecycleConfiguration"
                    "GetBucketLocation"
                    "GetBucketLogging"
                    "GetBucketMetricsConfiguration"
                    "GetBucketPolicy"
                    "GetBucketPolicyStatus"
                    "GetBucketReplication"
                    "GetBucketRequestPayment"
                    "GetBucketTagging"
                    "GetBucketVersioning"
                    "GetBucketWebsite"
                    "GetObject"
                    "GetObjectAcl"
                    "GetObjectLegalHold"
                    "GetObjectLockConfiguration"
                    "GetObjectRetention"
                    "GetObjectTagging"
                    "GetObjectTorrent"
                    "GetPublicAccessBlock"
                    "HeadBucket"
                    "HeadObject"
                    "ListBucketAnalyticsConfigurations"
                    "ListBucketInventoryConfigurations"
                    "ListBucketMetricsConfigurations"
                    "ListBuckets"
                    "ListMultipartUploads"
                    "ListObjectVersions"
                    "ListObjects"
                    "ListObjectsV2"
                    "ListParts"
                    "PutBucketAccelerateConfiguration"
                    "PutBucketAcl"
                    "PutBucketAnalyticsConfiguration"
                    "PutBucketCors"
                    "PutBucketEncryption"
                    "PutBucketInventoryConfiguration"
                    "PutBucketLifecycle"
                    "PutBucketLifecycleConfiguration"
                    "PutBucketLogging"
                    "PutBucketMetricsConfiguration"
                    "PutBucketNotification"
                    "PutBucketNotificationConfiguration"
                    "PutBucketPolicy"
                    "PutBucketReplication"
                    "PutBucketRequestPayment"
                    "PutBucketTagging"
                    "PutBucketVersioning"
                    "PutBucketWebsite"
                    "PutObject"
                    "PutObjectAcl"
                    "PutObjectLegalHold"
                    "PutObjectLockConfiguration"
                    "PutObjectRetention"
                    "PutObjectTagging"
                    "PutPublicAccessBlock"
                    "RestoreObject"
                    "SelectObjectContent"
                    "UploadPart"
                    "UploadPartCopy"]})

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
        [input]
        (lazy-seq
          (let [page (page-fn input)]
            (if-let [next-continuation-token (:NextContinuationToken page)]
              (cons page (get-pages (assoc input :ContinuationToken next-continuation-token)))
              [page])))))))

(def t
  "
// Code generated by generate.clj DO NOT EDIT
package aws

import (
\"encoding/json\"
\"github.com/aws/aws-sdk-go/aws/session\"
  <% (doseq [[ns-name _] namespaces]
  %> \"github.com/aws/aws-sdk-go/service/<%= ns-name %>\"
<% ) %>
  \"github.com/tzzh/pod-tzzh-aws/babashka\"
)

func ProcessMessage(message *babashka.Message) {

if message.Op == \"describe\" {
    response := &babashka.DescribeResponse{
        Format: \"json\",
        Namespaces: []babashka.Namespace{
            {Name: \"pod.tzzh.paginator\",
             Vars: []babashka.Var{
                {Name: \"get-paginator\",
                 Code: `<%= paginator %>`},
                },
             },
        <% (doseq [[ns-name ns-fns] namespaces]
          %>{Name: \"pod.tzzh.<%= ns-name %>\",
            Vars: []babashka.Var{
            <% (doseq [ns-fn ns-fns]
            %>  {Name: \"<%= (clj-fn-name ns-fn) %>\"},
            <% ) %>
            },
           },
        <% ) %>
        },
    }
    babashka.WriteDescribeResponse(response)

  } else if message.Op == \"invoke\" {

            switch message.Var {
<% (doseq [[ns-name ns-fns] namespaces
           ns-fn ns-fns]
            %>case \"pod.tzzh.<%= ns-name %>/<%= (clj-fn-name ns-fn) %>\":

    svc := <%= ns-name %>.New(session.New())
    input := &<%= ns-name %>.<%= (aws-input-name ns-fn)%>{}
    inputList := []<%= ns-name %>.<%= (aws-input-name ns-fn)%>{}
    err := json.Unmarshal([]byte(message.Args), &inputList)
    if err != nil {
        babashka.WriteErrorResponse(message, err)
    } else {
          if len(inputList) > 0 {
              input = &inputList[0]
          }
          res, err := svc.<%= ns-fn %>(input)
          if err != nil {
              babashka.WriteErrorResponse(message, err)
          } else {
              babashka.WriteInvokeResponse(message, res)
          }
      }
<% ) %>
          }
  }
}
")

(when (= *file* (System/getProperty "babashka.file"))
  (let [aws-go "./aws/aws.go"]
    (spit aws-go (template/eval t {:namespaces aws-api
                                   :paginator (str quoted-get-paginator)}))
    (let [{:keys [err exit]} (shell/sh "go" "fmt" aws-go)]
      (when-not (empty? err)
        (print err))
      (System/exit exit))))

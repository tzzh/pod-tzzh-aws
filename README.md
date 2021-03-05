# pod-tzzh-aws

A [pod](https://github.com/babashka/babashka.pods) to interact with AWS using [babashka](https://github.com/borkdude/babashka/).

The API is the same as the Java/Python/Go sdks, with clojurized function name, e.g `BatchGetItem` becomes `batch-get-item`.

[This file](./babashka/babashka.go) contains the code around receiving/sending bencoded messages from/to babashka.

Then [this](./gen/generate.clj) generates all the code to use the golang sdk. That might be a bit hacky but allows to have access to most of the AWS sdk really quickly and I believe this is relatively common in Go to get around the lack of generics.

The current release contains most of the sdk for dynamodb, s3, athena, glue, kafka, kinesis, lambda, sqs and ssm.

The code can be re-generated for other services by changing [this](https://github.com/tzzh/pod-tzzh-aws/blob/6633df0b2f5080f7d4374b5d4f20331d556f1c2f/gen/generate.clj#L11-L21) and then running `make generate` and `go build` to build the new binary.

## Usage

Get the latest release and then:
``` clojure
(require '[babashka.pods])
(babashka.pods/load-pod ["./pod-tzzh-aws"])
(require '[pod.tzzh.dynamodb :as d])
(require '[pod.tzzh.podconfig :as config]')
(require '[pod.tzzh.s3 :as s3])
(require '[pod.tzzh.glue :as g])
(require '[pod.tzzh.paginator :as p])

(config/set-creds {:Profile "default" :Region "us-east-1"})

(d/list-tables)

(d/batch-get-item {:RequestItems
                    {"AmazingTable" {:Keys [{:some-property {:S "SomeValue"} 
                                             :something-else {:S "SomethingSomething"}}]}}})

(d/batch-write-item {:RequestItems
                    {"AmazingTable" [{:PutRequest {:Item {:some-property {:S "abxdggje"}
                                                          :something-else {:S "zxcmbnj"}
                                                          :another-thing {:S "asdasdsa"}}}}]}})

(d/get-item {:Key {:lalala {:S "zzzzzzzz"}
                   :bbbbbb {:S "abxbxbxx"}}
             :TableName "SomeTable"})

(d/describe-table {:TableName "SomeTable"})

(s3/list-buckets)

;; Paginators example
(let [s3-paginator (p/get-paginator s3/list-objects-v2-pages)]
    (s3-paginator {:Bucket "some-bucket"
                   :Prefix "some-prefix/something/"}))
;; this returns a list of all the pages i.e a list of ListObjectsV2Output that are lazily fetched

(let [glue-paginator (p/get-paginator g/list-crawlers)]
         (glue-paginator))
```

## Paginators

In the Go sdk paginators take a function argument which is called on each page and returns a boolean that tells when to stop iterating, and the paginator itself doesn't return anything.
For example the signature of `ListObjectsV2Pages` is
```go
func (c *S3) ListObjectsV2Pages(input *ListObjectsV2Input, fn func(*ListObjectsV2Output, bool) bool) error
```
Whereas in the Python sdk, the paginators are instead generators that lazily loads the pages.
This approach is more functional and has been copied here.
To use it you need to use the `get-paginator` fn from the `pod.tzzh.paginator` namespace and pass the fn you need to use as an argument to `get-paginator` as shown in the example above.
The functions that use either `NextContinuationToken`, `NextToken` and `NextMarker` to paginate can currently be paginated.

## Debugging

For debugging set the environment variable `POD_TZZH_AWS_DEBUG=true` and the logs will show in stderr.

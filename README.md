# pod-tzzh-aws

A WIP [pod](https://github.com/babashka/babashka.pods) to interact with AWS using [babashka](https://github.com/borkdude/babashka/).

[This file](./babashka/babashka.go) contains the code around receiving/sending bencoded messages from/to babashka.

Then [this](./gen/generate.clj) generates all the code to use the golang sdk. That might be a bit too hacky but allows to have access to most of the AWS sdk really quickly and I believe this is relatively common in Go to get around the lack of generics.

Currently most dynamodb and s3 functions are supported (adding other services should be easy in most cases as the code is mostly auto generated).

## Usage

Get the latest release and then:
``` clojure
(require '[babashka.pods])
(babashka.pods/load-pod ["./pod-tzzh-aws"])
(require '[pod.tzzh.dynamodb :as d])
(require '[pod.tzzh.s3 :as s3])
(require '[pod.tzzh.paginator :as p])


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

(let [s3-paginator (p/get-paginator s3/list-objects-v2-pages)]
    (s3-paginator {:Bucket "some-bucket"
                   :Prefix "some-prefix/something/"}))
;; this returns a list of all the pages i.e a list of ListObjectsV2Output that are lazily fetched
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

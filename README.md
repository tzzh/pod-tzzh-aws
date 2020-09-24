# pod-tzzh-aws

A WIP [pod](https://github.com/babashka/babashka.pods) to interact with AWS using [babashka](https://github.com/borkdude/babashka/).

[This file](./babashka/babashka.go) contains the code around receiving/sending bencoded messages from/to babashka.

Then [this](./gen/generate.clj) generates all the code to use the golang sdk. That might be a bit too hacky but allows to have access to most of the AWS sdk really quickly and I believe this is relatively common in Go to get around the lack of generics.

Currently most dynamodb and s3 functions are supported (adding other services should be easy in most cases as the code is mostly auto generated).

## Usage

Compile the pod by running `go build`, then:
``` clojure
(require '[babashka.pods])
(babashka.pods/load-pod ["./pod-tzzh-aws"])
(require '[pod.tzzh.dynamodb :as d])
(require '[pod.tzzh.s3 :as s3])


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

(s3/list-objects-v2-pages {:Bucket "some-bucket"
                           :Prefix "some-prefix/something/"})
;; this returns a list of all the pages i.e a list of ListObjectsV2Output
```

## Paginators

In the Go sdk paginators use a function argument which is called on each page and returns a boolean that tells when to stop iterating.
That behaviour would be really tricky to implement so instead the paginators return a list of all the pages.

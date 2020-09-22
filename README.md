# pod-tzzh-aws

A WIP [pod](https://github.com/babashka/babashka.pods) to interact with AWS using [babashka](https://github.com/borkdude/babashka/).

[This file](./babashka.go) contains the code around receiving/sending bencoded messages from/to babashka.

Then [this](./dynamo.go) is just a wrapper around the golang sdk. Only a few functions are implementend at the moment but it would be quite easy to add more (and add other components than dynamodb) if that turns out to be useful.

## Usage
Compile the pod by running `go build`, then:
``` clojure
(require '[babashka.pods])
(babashka.pods/load-pod ["./pod-tzzh-aws"])
(require '[pod.tzzh.dynamodb :as d])

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
```

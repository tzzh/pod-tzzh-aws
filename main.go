package main

import (
	"github.com/tzzh/pod-tzzh-aws/aws"
	"github.com/tzzh/pod-tzzh-aws/babashka"
	"io/ioutil"
	"log"
	"os"
)

func main() {
	debug := os.Getenv("POD_TZZH_AWS_DEBUG")
	if debug != "true" {
		log.SetOutput(ioutil.Discard)
	}

	for {
		message := babashka.ReadMessage()
		res, err := aws.ProcessMessage(message)
		if err != nil {
			babashka.WriteErrorResponse(message, err)
			continue
		}
		describeRes, ok := res.(*babashka.DescribeResponse)
		if ok {
			babashka.WriteDescribeResponse(describeRes)
			continue
		}
		babashka.WriteInvokeResponse(message, res)
	}
}

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
		aws.ProcessMessage(message)
	}
}

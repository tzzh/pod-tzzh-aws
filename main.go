package main

import (
	"github.com/tzzh/pod-tzzh-aws/aws"
	"github.com/tzzh/pod-tzzh-aws/babashka"
	"log"
	"os"
)

func main() {
	f, err := os.OpenFile("/tmp/pod-tzzh-aws.log", os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
	if err != nil {
		log.Fatalf("error opening file: %v", err)
	}
	defer f.Close()

	log.SetOutput(f)
	for {
		message := babashka.ReadMessage()
		aws.ProcessMessage(message)
	}
}

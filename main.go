package main

import (
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
		message := ReadMessage()
		if message.Op == "describe" {
			response := &DescribeResponse{
				Format: "json",
				Namespaces: []Namespace{
					{Name: "pod.dynamodb",
						Vars: []Var{
							{Name: "batch-get-item"},
							{Name: "describe-table"},
							{Name: "get-item"},
							{Name: "list-tables"},
						},
					},
				},
			}
			WriteDescribeResponse(response)

		} else if message.Op == "invoke" {

			switch message.Var {
			case "pod.dynamodb/batch-get-item":
				res, err := BatchGetItem(message)
				if err != nil {
					WriteErrorResponse(message, err)
				} else {
					WriteInvokeResponse(message, res)
				}
			case "pod.dynamodb/describe-table":
				res, err := DescribeTable(message)
				if err != nil {
					WriteErrorResponse(message, err)
				} else {
					WriteInvokeResponse(message, res)
				}
			case "pod.dynamodb/get-item":
				res, err := GetItem(message)
				if err != nil {
					WriteErrorResponse(message, err)
				} else {
					WriteInvokeResponse(message, res)
				}
			case "pod.dynamodb/list-tables":
				res, err := ListTables(message)
				if err != nil {
					WriteErrorResponse(message, err)
				} else {
					WriteInvokeResponse(message, res)
				}
			}
		}
	}
}

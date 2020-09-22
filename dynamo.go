package main

import (
	"encoding/json"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/dynamodb"
)

func BatchGetItem(inputMessage *Message) (*dynamodb.BatchGetItemOutput, error) {

	svc := dynamodb.New(session.New())
	input := &dynamodb.BatchGetItemInput{}
	inputList := []dynamodb.BatchGetItemInput{}
	err := json.Unmarshal([]byte(inputMessage.Args), &inputList)
	if err != nil {
		return nil, err
	}
	if len(inputList) > 0 {
		input = &inputList[0]
	}

	res, err := svc.BatchGetItem(input)
	return res, err
}

func BatchWriteItem(inputMessage *Message) (*dynamodb.BatchWriteItemOutput, error) {

	svc := dynamodb.New(session.New())
	input := &dynamodb.BatchWriteItemInput{}
	inputList := []dynamodb.BatchWriteItemInput{}
	err := json.Unmarshal([]byte(inputMessage.Args), &inputList)
	if err != nil {
		return nil, err
	}
	if len(inputList) > 0 {
		input = &inputList[0]
	}

	res, err := svc.BatchWriteItem(input)
	return res, err
}

func DescribeTable(inputMessage *Message) (*dynamodb.DescribeTableOutput, error) {

	svc := dynamodb.New(session.New())
	input := &dynamodb.DescribeTableInput{}
	inputList := []dynamodb.DescribeTableInput{}
	err := json.Unmarshal([]byte(inputMessage.Args), &inputList)
	if err != nil {
		return nil, err
	}
	if len(inputList) > 0 {
		input = &inputList[0]
	}

	res, err := svc.DescribeTable(input)
	return res, err
}

func GetItem(inputMessage *Message) (*dynamodb.GetItemOutput, error) {

	svc := dynamodb.New(session.New())
	input := &dynamodb.GetItemInput{}
	inputList := []dynamodb.GetItemInput{}
	err := json.Unmarshal([]byte(inputMessage.Args), &inputList)
	if err != nil {
		return nil, err
	}
	if len(inputList) > 0 {
		input = &inputList[0]
	}

	res, err := svc.GetItem(input)
	return res, err
}

func ListTables(inputMessage *Message) (*dynamodb.ListTablesOutput, error) {

	svc := dynamodb.New(session.New())
	input := &dynamodb.ListTablesInput{}
	inputList := []dynamodb.ListTablesInput{}
	err := json.Unmarshal([]byte(inputMessage.Args), &inputList)
	if err != nil {
		return nil, err
	}
	if len(inputList) > 0 {
		input = &inputList[0]
	}

	res, err := svc.ListTables(input)
	return res, err
}

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }
  required_version = ">= 1.2.0"
}

provider "aws" {
  region = "ap-northeast-1"
}

variable "lambda_function_name" {
  default = "test_lambda"
}

resource "aws_lambda_function" "test_lambda" {
  function_name = var.lambda_function_name
  description   = "test with lambda, kinesis, and dynamo"

  package_type = "Image"
  image_uri    = "${var.account_id}.dkr.ecr.ap-northeast-1.amazonaws.com/test_repo:latest"
  timeout      = 300
  memory_size  = 1024

  lifecycle {
    ignore_changes = [image_uri]
  }

  role = "arn:aws:iam::${var.account_id}:role/lambda-kinesis-test"
}

resource "aws_lambda_event_source_mapping" "test_lambda_kinesis" {
  # change to be able to get arn kinesis resource
  event_source_arn  = "arn:aws:kinesis:ap-northeast-1:${var.account_id}:stream/terraform-kinesis-test"
  function_name     = aws_lambda_function.test_lambda.arn
  starting_position = "LATEST"
}

resource "aws_cloudwatch_log_group" "test_lambda_cloudwatch" {
  name              = "/aws/lambda/${var.lambda_function_name}"
  retention_in_days = 1
}

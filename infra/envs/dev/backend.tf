terraform {
  backend "s3" {
    bucket = "aimon-tfstate-bucket"
    key = "project/infra/dev/terraform.tfstate"
    region = "ap-northeast-2"
    dynamodb_table = "aimon-tf-lock-table"
    encrypt = true
  }
}
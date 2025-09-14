provider "aws" {
    region = "ap-northeast-2"
    profile = var.aws_profile # local AWS CLI 프로파일 이름
}

# variables.tf
variable "aws_profile" {
  type = string
  description = "로컬 AWS CLI 프로파일 이름"
  default = "default"
}
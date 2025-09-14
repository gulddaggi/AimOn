provider "aws" {
  region = "ap-northeast-2"
}

variable "cidr" {
  default = "10.0.0.0/16"
}

variable "azs" {
  default = ["ap-northeast-2a", "ap-northeast-2c"]
}

module "vpc" {
  source = "../../modules/vpc"
  cidr = var.cidr
  azs = var.azs
}
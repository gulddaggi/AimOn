variable "cidr" {
  description = "The CIDR block for the VPC"
  type = string
}

variable "azs" {
  description = "List of availability zones"
  type = list(string)
}
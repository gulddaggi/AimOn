resource "aws_vpc" "this" {
  cidr_block = var.cidr
  enable_dns_hostnames = true
  tags = {
    Name = "vpc-${terraform.workspace}"
  }
}

resource "aws_subnet" "public" {
  count = length(var.azs)
  vpc_id = aws_vpc.this.id
  cidr_block = cidrsubnet(var.cidr, 8, count.index)
  availability_zone = var.azs[count.index]
  map_public_ip_on_launch = true
  tags = {
    Name = "public-${var.azs[count.index]}"
  }
}
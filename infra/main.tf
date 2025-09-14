resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
  # tags = { Name = "${var.env}-vpc" }
}

resource "aws_subnet" "public" {
  # count             = length(var.azs)
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(aws_vpc.main.cidr_block, 8, count.index)
  
  # availability_zone = var.azs[count.index]
  map_public_ip_on_launch = true
  # tags = { Name = "${var.env}-public-${var.azs[count.index]}" }
}
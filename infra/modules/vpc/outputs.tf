output "vpc_id" {
  description = "The ID of the created VPC"
  value = aws_vpc.this.id
}

output "public_subnet_ids" {
  description = "The IDs of the public subnets"
  value = aws_subnet.public[*].id
}
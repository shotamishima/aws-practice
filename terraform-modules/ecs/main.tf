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

resource "aws_ecr_repository" "ecr" {
  name                 = "test-ecs"
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration {
    scan_on_push = false
  }

}

//
// Define VPC, subnet
//

data "aws_vpc" "myvpc" {
  id = "vpc-0adc21e246e1ec991"
}

output "vpc_cidr" {
  value = data.aws_vpc.myvpc.cidr_block
}

data "aws_internet_gateway" "myinternet_gateway" {
  internet_gateway_id = "igw-00e721b2e59021bc3"
}

resource "aws_subnet" "mysubnet_public" {
  vpc_id            = data.aws_vpc.myvpc.id
  cidr_block        = "10.0.0.0/28"
  availability_zone = "ap-northeast-1a"
  tags = {
    "Name" = "myecs-public-subnet"
  }
}

resource "aws_route_table" "myroute_table_public" {
  vpc_id = data.aws_vpc.myvpc.id
  tags = {
    "Name" = "myecs-public-subnet-route"
  }
}

resource "aws_route" "myroute_public" {
  destination_cidr_block = "0.0.0.0/0"
  route_table_id         = aws_route_table.myroute_table_public.id
  gateway_id             = data.aws_internet_gateway.myinternet_gateway.id
}

resource "aws_route_table_association" "my_public_subnet_route" {
  subnet_id      = aws_subnet.mysubnet_public.id
  route_table_id = aws_route_table.myroute_table_public.id
}


resource "aws_security_group" "myecs_service" {
  name   = "myecs-service-sg"
  vpc_id = data.aws_vpc.myvpc.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = -1
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    // the port test nginx is waiting
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    // from home pc
    cidr_blocks = ["60.108.236.22/32"]
  }
}

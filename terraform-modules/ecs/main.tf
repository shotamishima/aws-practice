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

// ECR

resource "aws_ecr_repository" "ecr" {
  name                 = "test-ecs"
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration {
    scan_on_push = false
  }

}

// Define VPC, subnet, security group

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

resource "aws_ecs_task_definition" "myecs" {
  family                   = join("-", ["myecs", "task", "definition"])
  requires_compatibilities = ["FARGATE"]

  network_mode = "awsvpc"
  cpu          = 256
  memory       = 512

  container_definitions = jsonencode([
    {
      name      = "test-server"
      image     = "${aws_ecr_repository.ecr.repository_url}:latest"
      essential = true
      portMappings = [
        {
          containerPort = 80
          hostPort      = 80
        }
      ]
    }
  ])

  execution_role_arn = aws_iam_role.myecs_task_execution_role.arn
}

// ECS task execution role

resource "aws_iam_role" "myecs_task_execution_role" {
  name               = join("-", ["myecs", "execution-role"])
  assume_role_policy = data.aws_iam_policy_document.myecs_task_execution_assume_policy.json
}

data "aws_iam_policy_document" "myecs_task_execution_assume_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy_attachment" "myecs_task_execution_policy" {
  role       = aws_iam_role.myecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

// ECS task role
// This role is not used because defined task don't use logdriver so far

resource "aws_iam_role" "myecs_task_role" {
  name               = join("-", ["myecs", "role"])
  assume_role_policy = data.aws_iam_policy_document.myecs_task_assume_policy.json
}

data "aws_iam_policy_document" "myecs_task_assume_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "myecs_task_policy" {
  name   = join("-", ["myecs", "policy"])
  policy = data.aws_iam_policy_document.myecs_task_policy.json
}

data "aws_iam_policy_document" "myecs_task_policy" {
  statement {
    actions = [
      "logs:CreateLogStream",
      "logs:CreateLogGroup",
      "logs:DescribeLogStreams",
      "logs:PutLogEvents"
    ]
    effect    = "Allow"
    resources = ["*"]
  }
}

resource "aws_iam_role_policy_attachment" "myecs_task_role" {
  role       = aws_iam_role.myecs_task_role.name
  policy_arn = aws_iam_policy.myecs_task_policy.arn
}

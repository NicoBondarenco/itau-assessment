# =============================================================================
# EKS Module - Authorizer Project
# =============================================================================
# Cria cluster Amazon EKS para aplicações Kubernetes
# =============================================================================

# =============================================================================
# LOCAL VALUES
# =============================================================================
locals {
  resource_prefix = "${var.project_name}-${var.environment}"

  # Tags comuns
  common_tags = merge(var.tags, {
    Cluster = "${local.resource_prefix}-eks-cluster"
  })

  # Node group defaults
  node_group_defaults = {
    instance_types = ["t3.medium"]
    capacity_type  = "ON_DEMAND"
    disk_size     = 20
    ami_type      = "AL2_x86_64"
  }
}

# =============================================================================
# DATA SOURCES
# =============================================================================
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

# =============================================================================
# VPC AND NETWORKING (se não fornecida)
# =============================================================================
resource "aws_vpc" "eks_vpc" {
  count = var.vpc_id == null ? 1 : 0

  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-vpc"
    "kubernetes.io/cluster/${local.resource_prefix}-eks-cluster" = "shared"
  })
}

resource "aws_internet_gateway" "eks_igw" {
  count = var.vpc_id == null ? 1 : 0

  vpc_id = aws_vpc.eks_vpc[0].id

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-igw"
  })
}

resource "aws_subnet" "eks_public_subnet" {
  count = var.vpc_id == null ? 2 : 0

  vpc_id            = aws_vpc.eks_vpc[0].id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + 1)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  map_public_ip_on_launch = true

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-public-subnet-${count.index + 1}"
    Type = "Public"
    "kubernetes.io/cluster/${local.resource_prefix}-eks-cluster" = "shared"
    "kubernetes.io/role/elb" = "1"
  })
}

resource "aws_subnet" "eks_private_subnet" {
  count = var.vpc_id == null ? 2 : 0

  vpc_id            = aws_vpc.eks_vpc[0].id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + 10)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-private-subnet-${count.index + 1}"
    Type = "Private"
    "kubernetes.io/cluster/${local.resource_prefix}-eks-cluster" = "owned"
    "kubernetes.io/role/internal-elb" = "1"
  })
}

# NAT Gateway for private subnets
resource "aws_eip" "eks_nat_eip" {
  count = var.vpc_id == null ? 2 : 0

  domain = "vpc"

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-nat-eip-${count.index + 1}"
  })

  depends_on = [aws_internet_gateway.eks_igw]
}

resource "aws_nat_gateway" "eks_nat_gateway" {
  count = var.vpc_id == null ? 2 : 0

  allocation_id = aws_eip.eks_nat_eip[count.index].id
  subnet_id     = aws_subnet.eks_public_subnet[count.index].id

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-nat-gateway-${count.index + 1}"
  })

  depends_on = [aws_internet_gateway.eks_igw]
}

# Route tables
resource "aws_route_table" "eks_public_rt" {
  count = var.vpc_id == null ? 1 : 0

  vpc_id = aws_vpc.eks_vpc[0].id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.eks_igw[0].id
  }

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-public-rt"
  })
}

resource "aws_route_table" "eks_private_rt" {
  count = var.vpc_id == null ? 2 : 0

  vpc_id = aws_vpc.eks_vpc[0].id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.eks_nat_gateway[count.index].id
  }

  tags = merge(local.common_tags, {
    Name = "${local.resource_prefix}-eks-private-rt-${count.index + 1}"
  })
}

resource "aws_route_table_association" "eks_public_rta" {
  count = var.vpc_id == null ? 2 : 0

  subnet_id      = aws_subnet.eks_public_subnet[count.index].id
  route_table_id = aws_route_table.eks_public_rt[0].id
}

resource "aws_route_table_association" "eks_private_rta" {
  count = var.vpc_id == null ? 2 : 0

  subnet_id      = aws_subnet.eks_private_subnet[count.index].id
  route_table_id = aws_route_table.eks_private_rt[count.index].id
}

# =============================================================================
# IAM ROLES FOR EKS
# =============================================================================
# EKS Cluster Service Role
resource "aws_iam_role" "eks_cluster_role" {
  name = "${local.resource_prefix}-eks-cluster-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "eks_cluster_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_cluster_role.name
}

resource "aws_iam_role_policy_attachment" "eks_vpc_resource_controller" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEKSVPCResourceController"
  role       = aws_iam_role.eks_cluster_role.name
}

# EKS Node Group Role
resource "aws_iam_role" "eks_node_group_role" {
  name = "${local.resource_prefix}-eks-node-group-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_node_group_role.name
}

resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_node_group_role.name
}

resource "aws_iam_role_policy_attachment" "eks_container_registry_policy" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_node_group_role.name
}

# Additional policies for accessing AWS resources
resource "aws_iam_policy" "eks_app_policy" {
  name        = "${local.resource_prefix}-eks-app-policy"
  description = "Policy for EKS applications to access AWS resources"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
          "dynamodb:Query",
          "dynamodb:Scan"
        ]
        Resource = var.dynamodb_table_arns != null ? var.dynamodb_table_arns : ["*"]
      },
      {
        Effect = "Allow"
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ]
        Resource = var.sqs_queue_arns != null ? var.sqs_queue_arns : ["*"]
      },
      {
        Effect = "Allow"
        Action = [
          "kafka:DescribeCluster",
          "kafka:GetBootstrapBrokers"
        ]
        Resource = var.msk_cluster_arn != null ? [var.msk_cluster_arn] : ["*"]
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "*"
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "eks_app_policy_attachment" {
  policy_arn = aws_iam_policy.eks_app_policy.arn
  role       = aws_iam_role.eks_node_group_role.name
}

# =============================================================================
# EKS CLUSTER
# =============================================================================
resource "aws_eks_cluster" "cluster" {
  name     = "${local.resource_prefix}-eks-cluster"
  role_arn = aws_iam_role.eks_cluster_role.arn
  version  = var.kubernetes_version

  vpc_config {
    subnet_ids              = var.subnet_ids != null ? var.subnet_ids : concat(aws_subnet.eks_public_subnet[*].id, aws_subnet.eks_private_subnet[*].id)
    endpoint_private_access = var.endpoint_private_access
    endpoint_public_access  = var.endpoint_public_access
    public_access_cidrs     = var.endpoint_public_access_cidrs
  }

  # Enable logging
  enabled_cluster_log_types = var.cluster_log_types

  # Encryption
  dynamic "encryption_config" {
    for_each = var.cluster_encryption_config
    content {
      provider {
        key_arn = encryption_config.value.provider_key_arn
      }
      resources = encryption_config.value.resources
    }
  }

  tags = local.common_tags

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy,
    aws_iam_role_policy_attachment.eks_vpc_resource_controller
  ]
}

# =============================================================================
# EKS NODE GROUP
# =============================================================================
resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.cluster.name
  node_group_name = "${local.resource_prefix}-main-node-group"
  node_role_arn   = aws_iam_role.eks_node_group_role.arn
  subnet_ids      = var.subnet_ids != null ? var.private_subnet_ids : aws_subnet.eks_private_subnet[*].id

  capacity_type  = var.node_group_capacity_type
  instance_types = var.node_group_instance_types
  ami_type      = var.node_group_ami_type
  disk_size     = var.node_group_disk_size

  scaling_config {
    desired_size = var.node_group_desired_size
    max_size     = var.node_group_max_size
    min_size     = var.node_group_min_size
  }

  update_config {
    max_unavailable = var.node_group_max_unavailable
  }

  # Launch template
  dynamic "launch_template" {
    for_each = var.node_group_launch_template != null ? [var.node_group_launch_template] : []
    content {
      id      = lookup(launch_template.value, "id", null)
      name    = lookup(launch_template.value, "name", null)
      version = lookup(launch_template.value, "version", null)
    }
  }

  # User data
  dynamic "remote_access" {
    for_each = var.node_group_remote_access != null ? [var.node_group_remote_access] : []
    content {
      ec2_ssh_key               = lookup(remote_access.value, "ec2_ssh_key", null)
      source_security_group_ids = lookup(remote_access.value, "source_security_group_ids", null)
    }
  }

  labels = merge(
    var.node_group_labels,
    {
      Environment = var.environment
      Project     = var.project_name
    }
  )

  tags = local.common_tags

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.eks_container_registry_policy,
    aws_iam_role_policy_attachment.eks_app_policy_attachment
  ]
}

# =============================================================================
# OIDC IDENTITY PROVIDER
# =============================================================================
data "tls_certificate" "cluster_tls" {
  url = aws_eks_cluster.cluster.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "cluster_oidc" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.cluster_tls.certificates[0].sha1_fingerprint]
  url             = aws_eks_cluster.cluster.identity[0].oidc[0].issuer

  tags = local.common_tags
}

# =============================================================================
# AWS LOAD BALANCER CONTROLLER IAM ROLE
# =============================================================================
data "aws_iam_policy_document" "aws_load_balancer_controller_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"

    condition {
      test     = "StringEquals"
      variable = "${replace(aws_iam_openid_connect_provider.cluster_oidc.url, "https://", "")}:sub"
      values   = ["system:serviceaccount:kube-system:aws-load-balancer-controller"]
    }

    principals {
      identifiers = [aws_iam_openid_connect_provider.cluster_oidc.arn]
      type        = "Federated"
    }
  }
}

resource "aws_iam_role" "aws_load_balancer_controller" {
  assume_role_policy = data.aws_iam_policy_document.aws_load_balancer_controller_assume_role_policy.json
  name               = "${local.resource_prefix}-aws-load-balancer-controller"

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "aws_load_balancer_controller" {
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/ElasticLoadBalancingFullAccess"
  role       = aws_iam_role.aws_load_balancer_controller.name
}

# =============================================================================
# KUBERNETES PROVIDER
# =============================================================================
data "aws_eks_cluster_auth" "cluster_auth" {
  name = aws_eks_cluster.cluster.name
}

# =============================================================================
# CLOUDWATCH LOG GROUP FOR EKS
# =============================================================================
resource "aws_cloudwatch_log_group" "eks_cluster_logs" {
  count = length(var.cluster_log_types) > 0 ? 1 : 0

  name              = "/aws/eks/${aws_eks_cluster.cluster.name}/cluster"
  retention_in_days = var.cluster_log_retention_days

  tags = local.common_tags
}

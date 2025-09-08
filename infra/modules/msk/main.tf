# =============================================================================
# MSK Module - Authorizer Project
# =============================================================================
# Cria cluster Amazon Managed Streaming for Kafka (MSK)
# =============================================================================

# =============================================================================
# LOCAL VALUES
# =============================================================================
locals {
  resource_prefix = "${var.project_name}-${var.environment}"

  # Configurações padrão do cluster
  cluster_defaults = {
    kafka_version          = "2.8.1"
    number_of_broker_nodes = 3
    instance_type         = "kafka.m5.large"
    ebs_volume_size       = 100
  }
}

# =============================================================================
# DATA SOURCES
# =============================================================================
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

# =============================================================================
# VPC AND NETWORKING (se não fornecida)
# =============================================================================
resource "aws_vpc" "msk_vpc" {
  count = var.vpc_id == null ? 1 : 0

  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-vpc"
    Type = "VPC"
  })
}

resource "aws_internet_gateway" "msk_igw" {
  count = var.vpc_id == null ? 1 : 0

  vpc_id = aws_vpc.msk_vpc[0].id

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-igw"
    Type = "InternetGateway"
  })
}

resource "aws_subnet" "msk_private_subnet" {
  count = var.vpc_id == null ? 3 : 0

  vpc_id            = aws_vpc.msk_vpc[0].id
  cidr_block        = "10.0.${count.index + 1}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-private-subnet-${count.index + 1}"
    Type = "PrivateSubnet"
  })
}

resource "aws_route_table" "msk_private_rt" {
  count = var.vpc_id == null ? 1 : 0

  vpc_id = aws_vpc.msk_vpc[0].id

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-private-rt"
    Type = "RouteTable"
  })
}

resource "aws_route_table_association" "msk_private_rta" {
  count = var.vpc_id == null ? 3 : 0

  subnet_id      = aws_subnet.msk_private_subnet[count.index].id
  route_table_id = aws_route_table.msk_private_rt[0].id
}

# =============================================================================
# SECURITY GROUPS
# =============================================================================
resource "aws_security_group" "msk_cluster_sg" {
  name_prefix = "${local.resource_prefix}-msk-cluster-"
  vpc_id      = var.vpc_id != null ? var.vpc_id : aws_vpc.msk_vpc[0].id

  description = "Security group for MSK cluster"

  # Kafka broker communication
  ingress {
    from_port   = 9092
    to_port     = 9098
    protocol    = "tcp"
    cidr_blocks = [var.vpc_id != null ? "10.0.0.0/8" : "10.0.0.0/16"]
    description = "Kafka brokers"
  }

  # Zookeeper
  ingress {
    from_port   = 2181
    to_port     = 2181
    protocol    = "tcp"
    cidr_blocks = [var.vpc_id != null ? "10.0.0.0/8" : "10.0.0.0/16"]
    description = "Zookeeper"
  }

  # JMX monitoring
  ingress {
    from_port   = 11001
    to_port     = 11002
    protocol    = "tcp"
    cidr_blocks = [var.vpc_id != null ? "10.0.0.0/8" : "10.0.0.0/16"]
    description = "JMX monitoring"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-cluster-sg"
    Type = "SecurityGroup"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# =============================================================================
# KMS KEY FOR ENCRYPTION
# =============================================================================
resource "aws_kms_key" "msk_encryption" {
  count = var.enable_encryption_at_rest ? 1 : 0

  description             = "KMS key for ${local.resource_prefix} MSK cluster encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "Enable IAM User Permissions"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action   = "kms:*"
        Resource = "*"
      },
      {
        Sid    = "Allow MSK to use the key"
        Effect = "Allow"
        Principal = {
          Service = "kafka.amazonaws.com"
        }
        Action = [
          "kms:Decrypt",
          "kms:GenerateDataKey*"
        ]
        Resource = "*"
      }
    ]
  })

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-kms-key"
    Type = "KMS"
  })
}

resource "aws_kms_alias" "msk_encryption" {
  count = var.enable_encryption_at_rest ? 1 : 0

  name          = "alias/${local.resource_prefix}-msk"
  target_key_id = aws_kms_key.msk_encryption[0].key_id
}

# =============================================================================
# CLOUDWATCH LOG GROUP
# =============================================================================
resource "aws_cloudwatch_log_group" "msk_log_group" {
  count = var.enable_logging ? 1 : 0

  name              = "/aws/msk/${local.resource_prefix}-cluster"
  retention_in_days = var.log_retention_days

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-logs"
    Type = "LogGroup"
  })
}

# =============================================================================
# MSK CLUSTER CONFIGURATION
# =============================================================================
resource "aws_msk_configuration" "cluster_config" {
  kafka_versions = [var.kafka_version]
  name          = "${local.resource_prefix}-msk-config"

  server_properties = <<PROPERTIES
auto.create.topics.enable=false
default.replication.factor=3
min.insync.replicas=2
num.partitions=3
log.retention.hours=168
log.retention.bytes=1073741824
log.segment.bytes=1073741824
log.roll.hours=24
compression.type=snappy
unclean.leader.election.enable=false
PROPERTIES

  description = "MSK configuration for ${local.resource_prefix}"
}

# =============================================================================
# MSK CLUSTER
# =============================================================================
resource "aws_msk_cluster" "cluster" {
  cluster_name           = "${local.resource_prefix}-msk-cluster"
  kafka_version         = var.kafka_version
  number_of_broker_nodes = var.number_of_broker_nodes

  broker_node_group_info {
    instance_type  = var.instance_type
    client_subnets = var.subnet_ids != null ? var.subnet_ids : aws_subnet.msk_private_subnet[*].id

    storage_info {
      ebs_storage_info {
        volume_size = var.ebs_volume_size
      }
    }

    security_groups = [aws_security_group.msk_cluster_sg.id]
  }

  configuration_info {
    arn      = aws_msk_configuration.cluster_config.arn
    revision = aws_msk_configuration.cluster_config.latest_revision
  }

  # Encryption settings
  encryption_info {
    encryption_at_rest_kms_key_id = var.enable_encryption_at_rest ? aws_kms_key.msk_encryption[0].arn : null

    encryption_in_transit {
      client_broker = var.encryption_in_transit_client_broker
      in_cluster   = var.encryption_in_transit_in_cluster
    }
  }

  # Enhanced monitoring
  enhanced_monitoring = var.enhanced_monitoring

  # Logging
  dynamic "logging_info" {
    for_each = var.enable_logging ? [1] : []

    content {
      broker_logs {
        cloudwatch_logs {
          enabled   = true
          log_group = aws_cloudwatch_log_group.msk_log_group[0].name
        }

        firehose {
          enabled = false
        }

        s3 {
          enabled = false
        }
      }
    }
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-msk-cluster"
    Type = "MSKCluster"
  })
}

# =============================================================================
# KAFKA TOPIC (usando script local)
# =============================================================================
resource "null_resource" "create_kafka_topic" {
  count = var.create_topics ? 1 : 0

  triggers = {
    cluster_arn = aws_msk_cluster.cluster.arn
    topics      = jsonencode(var.kafka_topics)
  }

  provisioner "local-exec" {
    command = <<-EOF
      # Aguardar cluster estar disponível
      sleep 300

      # Obter brokers
      BOOTSTRAP_SERVERS=$(aws kafka get-bootstrap-brokers \
        --cluster-arn ${aws_msk_cluster.cluster.arn} \
        --region ${var.aws_region} \
        --query 'BootstrapBrokerString' \
        --output text)

      # Criar tópicos
      ${join("\n", [for topic in var.kafka_topics :
        "kafka-topics --bootstrap-server $BOOTSTRAP_SERVERS --create --topic ${topic.name} --partitions ${topic.partitions} --replication-factor ${topic.replication_factor} --if-not-exists"
      ])}
    EOF

    environment = {
      AWS_REGION = var.aws_region
    }
  }

  depends_on = [aws_msk_cluster.cluster]
}

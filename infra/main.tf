# =============================================================================
# Main Terraform Configuration - Authorizer AWS Infrastructure
# =============================================================================
# Cria toda a infraestrutura AWS necessária para o projeto Authorizer
# Baseado no script init.sh do LocalStack
# =============================================================================

terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

}

# =============================================================================
# PROVIDER CONFIGURATION
# =============================================================================
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Owner       = var.owner
    }
  }
}

# =============================================================================
# DATA SOURCES
# =============================================================================
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# =============================================================================
# LOCAL VALUES
# =============================================================================
locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
    Owner       = var.owner
    Region      = data.aws_region.current.name
    AccountId   = data.aws_caller_identity.current.account_id
  }

  resource_prefix = "${var.project_name}-${var.environment}"
}

# =============================================================================
# SQS MODULE
# =============================================================================
module "sqs" {
  source = "./modules/sqs"

  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  # Configurações específicas das filas
  queue_config = {
    account_transaction = {
      name                        = "account-transaction"
      fifo_queue                 = true
      content_based_deduplication = true
      fifo_throughput_limit      = "perMessageGroupId"
      deduplication_scope        = "messageGroup"
      receive_wait_time_seconds   = 20
      visibility_timeout_seconds  = 45
      message_retention_seconds   = 1800
      max_receive_count          = 3
    }

    account_transaction_dlq = {
      name                        = "account-transaction-dlq"
      fifo_queue                 = true
      content_based_deduplication = true
      message_retention_seconds   = 1800
      is_dlq                     = true
    }
  }

  tags = local.common_tags
}

# =============================================================================
# DYNAMODB MODULE
# =============================================================================
module "dynamodb" {
  source = "./modules/dynamodb"

  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  # Configurações das tabelas
  tables_config = {
    account = {
      name               = "account"
      billing_mode       = "PAY_PER_REQUEST"
      hash_key          = "accountId"
      hash_key_type     = "S"
      enable_encryption = var.enable_dynamodb_encryption
      enable_backup     = var.enable_dynamodb_backup
    }

    balance = {
      name               = "balance"
      billing_mode       = "PAY_PER_REQUEST"
      hash_key          = "accountId"
      hash_key_type     = "S"
      enable_encryption = var.enable_dynamodb_encryption
      enable_backup     = var.enable_dynamodb_backup
    }

    transaction = {
      name               = "transaction"
      billing_mode       = "PAY_PER_REQUEST"
      hash_key          = "transactionId"
      hash_key_type     = "S"
      enable_encryption = var.enable_dynamodb_encryption
      enable_backup     = var.enable_dynamodb_backup

      # Global Secondary Index
      global_secondary_indexes = [
        {
          name            = "AccountIdIndex"
          hash_key        = "accountId"
          range_key       = "timestamp"
          hash_key_type   = "S"
          range_key_type  = "S"
          projection_type = "ALL"
        }
      ]

      # Atributos adicionais necessários para GSI
      attributes = [
        {
          name = "transactionId"
          type = "S"
        },
        {
          name = "accountId"
          type = "S"
        },
        {
          name = "timestamp"
          type = "S"
        }
      ]
    }
  }

  tags = local.common_tags
}

# =============================================================================
# IAM ROLES AND POLICIES MODULE (Opcional)
# =============================================================================
module "iam" {
  source = "./modules/iam"

  count = var.create_iam_resources ? 1 : 0

  project_name = var.project_name
  environment  = var.environment

  # Referencias dos recursos criados
  sqs_queue_arns      = module.sqs.queue_arns
  dynamodb_table_arns = module.dynamodb.table_arns

  tags = local.common_tags
}

# =============================================================================
# MSK MODULE
# =============================================================================
module "msk" {
  source = "./modules/msk"

  count = var.enable_msk ? 1 : 0

  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  # Configurações do cluster
  kafka_version          = var.msk_kafka_version
  number_of_broker_nodes = var.msk_number_of_broker_nodes
  instance_type         = var.msk_instance_type
  ebs_volume_size       = var.msk_ebs_volume_size

  # Configurações de rede
  vpc_id     = var.vpc_id
  subnet_ids = var.subnet_ids

  # Configurações de segurança
  enable_encryption_at_rest            = var.msk_enable_encryption_at_rest
  encryption_in_transit_client_broker = var.msk_encryption_in_transit_client_broker
  encryption_in_transit_in_cluster    = var.msk_encryption_in_transit_in_cluster

  # Configurações de monitoramento
  enhanced_monitoring   = var.msk_enhanced_monitoring
  enable_logging       = var.msk_enable_logging
  log_retention_days   = var.msk_log_retention_days

  # Configurações dos tópicos
  create_topics = var.msk_create_topics
  kafka_topics = [
    {
      name               = "transaction-executed-event"
      partitions         = var.msk_transaction_topic_partitions
      replication_factor = var.msk_transaction_topic_replication_factor
    }
  ]

  tags = local.common_tags
}

# =============================================================================
# EKS MODULE (Kubernetes)
# =============================================================================
module "eks" {
  source = "./modules/eks"

  count = var.enable_eks ? 1 : 0

  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  # Configurações do cluster
  kubernetes_version      = var.eks_kubernetes_version
  endpoint_private_access = var.eks_endpoint_private_access
  endpoint_public_access  = var.eks_endpoint_public_access

  # Configurações de rede
  vpc_id             = var.vpc_id
  subnet_ids         = var.subnet_ids
  private_subnet_ids = var.private_subnet_ids

  # Configurações do node group
  node_group_instance_types   = var.eks_node_group_instance_types
  node_group_capacity_type    = var.eks_node_group_capacity_type
  node_group_desired_size     = var.eks_node_group_desired_size
  node_group_max_size        = var.eks_node_group_max_size
  node_group_min_size        = var.eks_node_group_min_size
  node_group_disk_size       = var.eks_node_group_disk_size

  # Configurações de logging
  cluster_log_types           = var.eks_cluster_log_types
  cluster_log_retention_days  = var.eks_cluster_log_retention_days

  # Integração com outros serviços AWS
  dynamodb_table_arns = [for table in module.dynamodb.table_arns : table]
  sqs_queue_arns     = [for queue in module.sqs.queue_arns : queue]
  msk_cluster_arn    = var.enable_msk ? module.msk[0].cluster_arn : null

  # Configurações das aplicações
  applications = {
    authorization = {
      name           = "app-authorization"
      image          = var.eks_authorization_app_image
      port           = 8200
      replicas       = var.eks_authorization_app_replicas
      cpu_request    = var.eks_authorization_app_cpu_request
      cpu_limit      = var.eks_authorization_app_cpu_limit
      memory_request = var.eks_authorization_app_memory_request
      memory_limit   = var.eks_authorization_app_memory_limit
      env_vars = {
        SERVER_PORT = "8200"
        ENVIRONMENT = var.environment
        GRPC_ENDPOINT = "app-validation-service:8100"
      }
      health_check = {
        path                = "/actuator/health"
        initial_delay       = 60
        period              = 30
        timeout             = 10
        success_threshold   = 1
        failure_threshold   = 3
      }
    }
    validation = {
      name           = "app-validation"
      image          = var.eks_validation_app_image
      port           = 8100
      replicas       = var.eks_validation_app_replicas
      cpu_request    = var.eks_validation_app_cpu_request
      cpu_limit      = var.eks_validation_app_cpu_limit
      memory_request = var.eks_validation_app_memory_request
      memory_limit   = var.eks_validation_app_memory_limit
      env_vars = {
        SERVER_PORT = "8100"
        ENVIRONMENT = var.environment
      }
      health_check = {
        path                = "/actuator/health"
        initial_delay       = 60
        period              = 30
        timeout             = 10
        success_threshold   = 1
        failure_threshold   = 3
      }
    }
  }

  # Configurações do Kubernetes
  create_namespace     = var.eks_create_namespace
  namespace_name      = var.eks_namespace_name
  enable_load_balancer = var.eks_enable_load_balancer
  load_balancer_type   = var.eks_load_balancer_type
  enable_hpa          = var.eks_enable_hpa

  # Configurações de integração
  kafka_brokers        = var.enable_msk ? module.msk[0].bootstrap_brokers_tls : null
  schema_registry_url  = var.eks_schema_registry_url
  domain_name         = var.eks_domain_name
  ssl_certificate_arn = var.eks_ssl_certificate_arn

  tags = local.common_tags

  depends_on = [
    module.dynamodb,
    module.sqs,
    module.msk
  ]
}

# =============================================================================
# MONITORING MODULE (Opcional)
# =============================================================================
module "monitoring" {
  source = "./modules/monitoring"

  count = var.enable_monitoring ? 1 : 0

  project_name = var.project_name
  environment  = var.environment

  # Referencias dos recursos para monitoramento
  sqs_queue_names      = module.sqs.queue_names
  dynamodb_table_names = module.dynamodb.table_names
  msk_cluster_name     = var.enable_msk ? module.msk[0].cluster_name : null
  eks_cluster_name     = var.enable_eks ? module.eks[0].cluster_name : null

  # Configurações de alarmes
  alarm_email = var.alarm_email

  tags = local.common_tags
}

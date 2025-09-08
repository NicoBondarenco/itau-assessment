# =============================================================================
# Variables - Authorizer AWS Infrastructure
# =============================================================================

# =============================================================================
# CORE VARIABLES
# =============================================================================
variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "authorizer"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{1,61}[a-z0-9]$", var.project_name))
    error_message = "O nome do projeto deve conter apenas letras minúsculas, números e hífens."
  }
}

variable "environment" {
  description = "Ambiente de deployment (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment deve ser: dev, staging ou prod."
  }
}

variable "aws_region" {
  description = "Região AWS para deployment dos recursos"
  type        = string
  default     = "sa-east-1"

  validation {
    condition = can(regex("^[a-z]{2}-[a-z]+-[0-9]$", var.aws_region))
    error_message = "AWS region deve ter formato válido (ex: sa-east-1)."
  }
}

variable "owner" {
  description = "Proprietário/responsável pelos recursos"
  type        = string
  default     = "authorizer-team"
}

# =============================================================================
# DYNAMODB CONFIGURATION
# =============================================================================
variable "enable_dynamodb_encryption" {
  description = "Habilitar criptografia at-rest para tabelas DynamoDB"
  type        = bool
  default     = true
}

variable "enable_dynamodb_backup" {
  description = "Habilitar backup automático para tabelas DynamoDB"
  type        = bool
  default     = true
}

variable "dynamodb_point_in_time_recovery" {
  description = "Habilitar Point-in-Time Recovery para tabelas DynamoDB"
  type        = bool
  default     = true
}

# =============================================================================
# SQS CONFIGURATION
# =============================================================================
variable "sqs_kms_encryption" {
  description = "Habilitar criptografia KMS para filas SQS"
  type        = bool
  default     = true
}

variable "sqs_kms_key_id" {
  description = "ID da chave KMS para criptografia SQS (opcional)"
  type        = string
  default     = null
}

# =============================================================================
# OPTIONAL FEATURES
# =============================================================================
variable "create_iam_resources" {
  description = "Criar roles e policies IAM para as aplicações"
  type        = bool
  default     = true
}

variable "enable_monitoring" {
  description = "Criar recursos de monitoramento (CloudWatch Alarms)"
  type        = bool
  default     = false
}

variable "alarm_email" {
  description = "Email para receber alertas do CloudWatch"
  type        = string
  default     = ""
}

# =============================================================================
# NETWORK CONFIGURATION (Future use)
# =============================================================================
variable "vpc_id" {
  description = "ID da VPC para recursos que precisam de rede (futuro uso)"
  type        = string
  default     = null
}

variable "subnet_ids" {
  description = "IDs das subnets para recursos que precisam de rede (futuro uso)"
  type        = list(string)
  default     = []
}

# =============================================================================
# COST OPTIMIZATION
# =============================================================================
variable "enable_cost_optimization" {
  description = "Habilitar configurações de otimização de custos"
  type        = bool
  default     = true
}

variable "backup_retention_days" {
  description = "Dias de retenção para backups DynamoDB"
  type        = number
  default     = 7

  validation {
    condition     = var.backup_retention_days >= 1 && var.backup_retention_days <= 35
    error_message = "Retenção de backup deve ser entre 1 e 35 dias."
  }
}

# =============================================================================
# SECURITY CONFIGURATION
# =============================================================================
variable "enable_deletion_protection" {
  description = "Habilitar proteção contra deleção para recursos críticos"
  type        = bool
  default     = false
}

variable "allowed_cidr_blocks" {
  description = "Blocos CIDR permitidos para acesso (futuro uso com VPC)"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

# =============================================================================
# PERFORMANCE CONFIGURATION
# =============================================================================
variable "dynamodb_performance_mode" {
  description = "Modo de performance para DynamoDB (PAY_PER_REQUEST ou PROVISIONED)"
  type        = string
  default     = "PAY_PER_REQUEST"

  validation {
    condition     = contains(["PAY_PER_REQUEST", "PROVISIONED"], var.dynamodb_performance_mode)
    error_message = "Performance mode deve ser PAY_PER_REQUEST ou PROVISIONED."
  }
}

variable "provisioned_read_capacity" {
  description = "Capacidade de leitura provisionada (usado apenas se performance_mode = PROVISIONED)"
  type        = number
  default     = 5
}

variable "provisioned_write_capacity" {
  description = "Capacidade de escrita provisionada (usado apenas se performance_mode = PROVISIONED)"
  type        = number
  default     = 5
}

# =============================================================================
# MSK CONFIGURATION
# =============================================================================
variable "enable_msk" {
  description = "Habilitar criação do cluster MSK"
  type        = bool
  default     = true
}

variable "msk_kafka_version" {
  description = "Versão do Kafka para o cluster MSK"
  type        = string
  default     = "2.8.1"

  validation {
    condition     = can(regex("^[0-9]+\\.[0-9]+\\.[0-9]+$", var.msk_kafka_version))
    error_message = "Versão do Kafka deve ter formato válido (ex: 2.8.1)."
  }
}

variable "msk_number_of_broker_nodes" {
  description = "Número de broker nodes no cluster MSK"
  type        = number
  default     = 3

  validation {
    condition     = var.msk_number_of_broker_nodes >= 2 && var.msk_number_of_broker_nodes <= 15
    error_message = "Número de broker nodes deve ser entre 2 e 15."
  }
}

variable "msk_instance_type" {
  description = "Tipo de instância para os brokers MSK"
  type        = string
  default     = "kafka.m5.large"

  validation {
    condition = can(regex("^kafka\\.[a-z0-9]+\\.[a-z0-9]+$", var.msk_instance_type))
    error_message = "Tipo de instância deve ter formato válido (ex: kafka.m5.large)."
  }
}

variable "msk_ebs_volume_size" {
  description = "Tamanho do volume EBS por broker MSK (GB)"
  type        = number
  default     = 100

  validation {
    condition     = var.msk_ebs_volume_size >= 1 && var.msk_ebs_volume_size <= 16384
    error_message = "Tamanho do volume EBS deve ser entre 1 e 16384 GB."
  }
}

# =============================================================================
# MSK SECURITY CONFIGURATION
# =============================================================================
variable "msk_enable_encryption_at_rest" {
  description = "Habilitar criptografia at-rest para MSK"
  type        = bool
  default     = true
}

variable "msk_encryption_in_transit_client_broker" {
  description = "Tipo de criptografia in-transit entre cliente e broker"
  type        = string
  default     = "TLS"

  validation {
    condition     = contains(["PLAINTEXT", "TLS", "TLS_PLAINTEXT"], var.msk_encryption_in_transit_client_broker)
    error_message = "Deve ser PLAINTEXT, TLS, ou TLS_PLAINTEXT."
  }
}

variable "msk_encryption_in_transit_in_cluster" {
  description = "Habilitar criptografia in-transit dentro do cluster MSK"
  type        = bool
  default     = true
}

# =============================================================================
# MSK MONITORING CONFIGURATION
# =============================================================================
variable "msk_enhanced_monitoring" {
  description = "Nível de monitoramento enhanced para MSK"
  type        = string
  default     = "PER_BROKER"

  validation {
    condition = contains([
      "DEFAULT", 
      "PER_BROKER", 
      "PER_TOPIC_PER_BROKER", 
      "PER_TOPIC_PER_PARTITION"
    ], var.msk_enhanced_monitoring)
    error_message = "Enhanced monitoring deve ser DEFAULT, PER_BROKER, PER_TOPIC_PER_BROKER, ou PER_TOPIC_PER_PARTITION."
  }
}

variable "msk_enable_logging" {
  description = "Habilitar logging do CloudWatch para MSK"
  type        = bool
  default     = true
}

variable "msk_log_retention_days" {
  description = "Dias de retenção dos logs MSK no CloudWatch"
  type        = number
  default     = 7

  validation {
    condition = contains([
      1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653
    ], var.msk_log_retention_days)
    error_message = "Log retention deve ser um valor válido do CloudWatch."
  }
}

# =============================================================================
# KAFKA TOPICS CONFIGURATION
# =============================================================================
variable "msk_create_topics" {
  description = "Criar tópicos Kafka automaticamente"
  type        = bool
  default     = true
}

variable "msk_transaction_topic_partitions" {
  description = "Número de partições para o tópico transaction-executed-event"
  type        = number
  default     = 3

  validation {
    condition     = var.msk_transaction_topic_partitions >= 1 && var.msk_transaction_topic_partitions <= 100
    error_message = "Número de partições deve ser entre 1 e 100."
  }
}

variable "msk_transaction_topic_replication_factor" {
  description = "Fator de replicação para o tópico transaction-executed-event"
  type        = number
  default     = 3

  validation {
    condition     = var.msk_transaction_topic_replication_factor >= 1 && var.msk_transaction_topic_replication_factor <= 10
    error_message = "Fator de replicação deve ser entre 1 e 10."
  }
}

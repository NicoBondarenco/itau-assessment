# =============================================================================
# MSK Module Variables
# =============================================================================

variable "project_name" {
  description = "Nome do projeto"
  type        = string
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
}

variable "aws_region" {
  description = "Região AWS"
  type        = string
}

# =============================================================================
# MSK CLUSTER CONFIGURATION
# =============================================================================
variable "kafka_version" {
  description = "Versão do Kafka"
  type        = string
  default     = "2.8.1"

  validation {
    condition     = can(regex("^[0-9]+\\.[0-9]+\\.[0-9]+$", var.kafka_version))
    error_message = "Versão do Kafka deve ter formato válido (ex: 2.8.1)."
  }
}

variable "number_of_broker_nodes" {
  description = "Número de broker nodes no cluster"
  type        = number
  default     = 3

  validation {
    condition     = var.number_of_broker_nodes >= 2 && var.number_of_broker_nodes <= 15
    error_message = "Número de broker nodes deve ser entre 2 e 15."
  }
}

variable "instance_type" {
  description = "Tipo de instância para os brokers"
  type        = string
  default     = "kafka.m5.large"

  validation {
    condition = can(regex("^kafka\\.[a-z0-9]+\\.[a-z0-9]+$", var.instance_type))
    error_message = "Tipo de instância deve ter formato válido (ex: kafka.m5.large)."
  }
}

variable "ebs_volume_size" {
  description = "Tamanho do volume EBS por broker (GB)"
  type        = number
  default     = 100

  validation {
    condition     = var.ebs_volume_size >= 1 && var.ebs_volume_size <= 16384
    error_message = "Tamanho do volume EBS deve ser entre 1 e 16384 GB."
  }
}

# =============================================================================
# NETWORK CONFIGURATION
# =============================================================================
variable "vpc_id" {
  description = "ID da VPC existente (se null, uma nova será criada)"
  type        = string
  default     = null
}

variable "subnet_ids" {
  description = "IDs das subnets para os brokers (se null, novas serão criadas)"
  type        = list(string)
  default     = null

  validation {
    condition     = var.subnet_ids == null || length(var.subnet_ids) >= 2
    error_message = "Deve haver pelo menos 2 subnets para os brokers."
  }
}

variable "allowed_cidr_blocks" {
  description = "Blocos CIDR permitidos para acesso ao cluster"
  type        = list(string)
  default     = ["10.0.0.0/8"]
}

# =============================================================================
# SECURITY CONFIGURATION
# =============================================================================
variable "enable_encryption_at_rest" {
  description = "Habilitar criptografia at-rest"
  type        = bool
  default     = true
}

variable "encryption_in_transit_client_broker" {
  description = "Tipo de criptografia in-transit entre cliente e broker"
  type        = string
  default     = "TLS"

  validation {
    condition     = contains(["PLAINTEXT", "TLS", "TLS_PLAINTEXT"], var.encryption_in_transit_client_broker)
    error_message = "Deve ser PLAINTEXT, TLS, ou TLS_PLAINTEXT."
  }
}

variable "encryption_in_transit_in_cluster" {
  description = "Habilitar criptografia in-transit dentro do cluster"
  type        = bool
  default     = true
}

# =============================================================================
# MONITORING AND LOGGING
# =============================================================================
variable "enhanced_monitoring" {
  description = "Nível de monitoramento enhanced (DEFAULT, PER_BROKER, PER_TOPIC_PER_BROKER, PER_TOPIC_PER_PARTITION)"
  type        = string
  default     = "PER_BROKER"

  validation {
    condition = contains([
      "DEFAULT",
      "PER_BROKER",
      "PER_TOPIC_PER_BROKER",
      "PER_TOPIC_PER_PARTITION"
    ], var.enhanced_monitoring)
    error_message = "Enhanced monitoring deve ser DEFAULT, PER_BROKER, PER_TOPIC_PER_BROKER, ou PER_TOPIC_PER_PARTITION."
  }
}

variable "enable_logging" {
  description = "Habilitar logging do CloudWatch"
  type        = bool
  default     = true
}

variable "log_retention_days" {
  description = "Dias de retenção dos logs no CloudWatch"
  type        = number
  default     = 7

  validation {
    condition = contains([
      1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653
    ], var.log_retention_days)
    error_message = "Log retention deve ser um valor válido do CloudWatch."
  }
}

# =============================================================================
# KAFKA TOPICS CONFIGURATION
# =============================================================================
variable "create_topics" {
  description = "Criar tópicos Kafka automaticamente"
  type        = bool
  default     = true
}

variable "kafka_topics" {
  description = "Lista de tópicos Kafka para criar"
  type = list(object({
    name               = string
    partitions         = number
    replication_factor = number
  }))
  default = [
    {
      name               = "transaction-executed-event"
      partitions         = 3
      replication_factor = 3
    }
  ]
}

# =============================================================================
# TAGS
# =============================================================================
variable "tags" {
  description = "Tags para aplicar aos recursos"
  type        = map(string)
  default     = {}
}

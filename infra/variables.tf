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

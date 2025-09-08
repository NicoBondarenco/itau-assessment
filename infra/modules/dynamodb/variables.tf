# =============================================================================
# DynamoDB Module Variables
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

variable "tables_config" {
  description = "Configuração das tabelas DynamoDB"
  type = map(object({
    name                          = string
    billing_mode                 = optional(string, "PAY_PER_REQUEST")
    hash_key                     = string
    hash_key_type                = optional(string, "S")
    range_key                    = optional(string, null)
    range_key_type               = optional(string, "S")
    read_capacity                = optional(number, 5)
    write_capacity               = optional(number, 5)
    enable_encryption            = optional(bool, true)
    enable_backup                = optional(bool, true)
    enable_point_in_time_recovery = optional(bool, true)
    enable_stream                = optional(bool, false)
    stream_view_type             = optional(string, "NEW_AND_OLD_IMAGES")
    ttl_attribute                = optional(string, null)

    # Atributos personalizados (para GSI/LSI)
    attributes = optional(list(object({
      name = string
      type = string
    })), [])

    # Global Secondary Indexes
    global_secondary_indexes = optional(list(object({
      name                = string
      hash_key           = string
      range_key          = optional(string, null)
      hash_key_type      = optional(string, "S")
      range_key_type     = optional(string, "S")
      projection_type    = optional(string, "ALL")
      non_key_attributes = optional(list(string), null)
      read_capacity      = optional(number, 5)
      write_capacity     = optional(number, 5)
    })), [])

    # Local Secondary Indexes
    local_secondary_indexes = optional(list(object({
      name               = string
      range_key          = string
      projection_type    = optional(string, "ALL")
      non_key_attributes = optional(list(string), null)
    })), [])
  }))
}

variable "enable_kms_encryption" {
  description = "Habilitar criptografia KMS customizada para as tabelas"
  type        = bool
  default     = false
}

variable "kms_key_id" {
  description = "ID da chave KMS customizada (opcional)"
  type        = string
  default     = null
}

variable "enable_deletion_protection" {
  description = "Habilitar proteção contra deleção das tabelas"
  type        = bool
  default     = false
}

variable "backup_retention_days" {
  description = "Dias de retenção para backups"
  type        = number
  default     = 7
}

variable "enable_cloudwatch_alarms" {
  description = "Criar alarmes CloudWatch para as tabelas"
  type        = bool
  default     = false
}

variable "alarm_sns_topic_arn" {
  description = "ARN do tópico SNS para envio de alarmes"
  type        = string
  default     = null
}

variable "tags" {
  description = "Tags para aplicar aos recursos"
  type        = map(string)
  default     = {}
}

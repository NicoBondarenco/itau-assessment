# =============================================================================
# SQS Module Variables
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

variable "queue_config" {
  description = "Configuração das filas SQS"
  type = map(object({
    name                        = string
    fifo_queue                 = optional(bool, true)
    content_based_deduplication = optional(bool, true)
    visibility_timeout_seconds  = optional(number, 30)
    message_retention_seconds   = optional(number, 1800)
    receive_wait_time_seconds   = optional(number, 0)
    max_receive_count          = optional(number, 3)
    fifo_throughput_limit      = optional(string, "perQueue")
    deduplication_scope        = optional(string, "queue")
    is_dlq                     = optional(bool, false)
  }))
}

variable "enable_kms_encryption" {
  description = "Habilitar criptografia KMS para as filas"
  type        = bool
  default     = true
}

variable "kms_key_id" {
  description = "ID da chave KMS customizada (opcional)"
  type        = string
  default     = null
}

variable "enable_cross_account_access" {
  description = "Habilitar políticas de acesso cross-account"
  type        = bool
  default     = false
}

variable "allowed_account_ids" {
  description = "IDs de contas AWS permitidas para acesso cross-account"
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Tags para aplicar aos recursos"
  type        = map(string)
  default     = {}
}

# =============================================================================
# Monitoring Module Variables
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

variable "sqs_queue_names" {
  description = "Nomes das filas SQS para monitoramento"
  type        = map(string)
}

variable "dynamodb_table_names" {
  description = "Nomes das tabelas DynamoDB para monitoramento"
  type        = map(string)
}

variable "alarm_email" {
  description = "Email para receber alertas"
  type        = string
  default     = ""
}

# =============================================================================
# SQS ALARM THRESHOLDS
# =============================================================================
variable "sqs_visible_messages_threshold" {
  description = "Threshold para número de mensagens visíveis na fila"
  type        = number
  default     = 100
}

variable "sqs_message_age_threshold" {
  description = "Threshold para idade máxima da mensagem (segundos)"
  type        = number
  default     = 1800  # 30 minutos
}

# =============================================================================
# DYNAMODB ALARM THRESHOLDS
# =============================================================================
variable "dynamodb_read_latency_threshold" {
  description = "Threshold para latência de leitura DynamoDB (milliseconds)"
  type        = number
  default     = 100
}

variable "dynamodb_write_latency_threshold" {
  description = "Threshold para latência de escrita DynamoDB (milliseconds)"
  type        = number
  default     = 100
}

variable "dynamodb_user_errors_threshold" {
  description = "Threshold para erros de usuário DynamoDB"
  type        = number
  default     = 5
}

# =============================================================================
# DASHBOARD CONFIGURATION
# =============================================================================
variable "create_dashboard" {
  description = "Criar dashboard CloudWatch"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags para aplicar aos recursos"
  type        = map(string)
  default     = {}
}

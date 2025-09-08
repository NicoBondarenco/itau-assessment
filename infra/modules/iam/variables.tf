# =============================================================================
# IAM Module Variables
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

variable "sqs_queue_arns" {
  description = "ARNs das filas SQS que as aplicações precisam acessar"
  type        = map(string)
}

variable "dynamodb_table_arns" {
  description = "ARNs das tabelas DynamoDB que as aplicações precisam acessar"
  type        = map(string)
}

variable "kms_key_arns" {
  description = "ARNs das chaves KMS para criptografia (opcional)"
  type        = list(string)
  default     = []
}

variable "create_instance_profiles" {
  description = "Criar instance profiles para EC2 (necessário para aplicações rodando em EC2)"
  type        = bool
  default     = false
}

variable "allowed_external_account_ids" {
  description = "IDs de contas AWS externas permitidas para assumir as roles"
  type        = list(string)
  default     = []
}

variable "custom_policies" {
  description = "Policies IAM customizadas para aplicar às roles"
  type = map(object({
    name        = string
    description = string
    policy      = string
  }))
  default = {}
}

variable "tags" {
  description = "Tags para aplicar aos recursos"
  type        = map(string)
  default     = {}
}

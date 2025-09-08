# =============================================================================
# Outputs - Authorizer AWS Infrastructure
# =============================================================================

# =============================================================================
# GENERAL INFORMATION
# =============================================================================
output "aws_region" {
  description = "Região AWS utilizada"
  value       = var.aws_region
}

output "account_id" {
  description = "ID da conta AWS"
  value       = data.aws_caller_identity.current.account_id
}

output "project_name" {
  description = "Nome do projeto"
  value       = var.project_name
}

output "environment" {
  description = "Ambiente de deployment"
  value       = var.environment
}

# =============================================================================
# SQS OUTPUTS
# =============================================================================
output "sqs_queue_urls" {
  description = "URLs das filas SQS criadas"
  value       = module.sqs.queue_urls
}

output "sqs_queue_arns" {
  description = "ARNs das filas SQS criadas"
  value       = module.sqs.queue_arns
}

output "sqs_queue_names" {
  description = "Nomes das filas SQS criadas"
  value       = module.sqs.queue_names
}

output "account_transaction_queue_url" {
  description = "URL da fila principal account-transaction"
  value       = module.sqs.queue_urls["account-transaction"]
}

output "account_transaction_dlq_url" {
  description = "URL da fila DLQ account-transaction-dlq"
  value       = module.sqs.queue_urls["account-transaction-dlq"]
}

# =============================================================================
# DYNAMODB OUTPUTS
# =============================================================================
output "dynamodb_table_names" {
  description = "Nomes das tabelas DynamoDB criadas"
  value       = module.dynamodb.table_names
}

output "dynamodb_table_arns" {
  description = "ARNs das tabelas DynamoDB criadas"
  value       = module.dynamodb.table_arns
}

output "dynamodb_table_stream_arns" {
  description = "ARNs dos streams das tabelas DynamoDB (se habilitados)"
  value       = module.dynamodb.table_stream_arns
}

output "account_table_name" {
  description = "Nome da tabela account"
  value       = module.dynamodb.table_names["account"]
}

output "balance_table_name" {
  description = "Nome da tabela balance"
  value       = module.dynamodb.table_names["balance"]
}

output "transaction_table_name" {
  description = "Nome da tabela transaction"
  value       = module.dynamodb.table_names["transaction"]
}

# =============================================================================
# IAM OUTPUTS (se criados)
# =============================================================================
output "application_role_arns" {
  description = "ARNs das roles IAM criadas para as aplicações"
  value       = var.create_iam_resources ? module.iam[0].application_role_arns : {}
}

output "application_policy_arns" {
  description = "ARNs das policies IAM criadas"
  value       = var.create_iam_resources ? module.iam[0].policy_arns : {}
}

# =============================================================================
# MONITORING OUTPUTS (se habilitado)
# =============================================================================
output "cloudwatch_alarm_arns" {
  description = "ARNs dos alarmes CloudWatch criados"
  value       = var.enable_monitoring ? module.monitoring[0].alarm_arns : {}
}

output "sns_topic_arn" {
  description = "ARN do tópico SNS para alertas"
  value       = var.enable_monitoring && var.alarm_email != "" ? module.monitoring[0].sns_topic_arn : null
}

# =============================================================================
# CONFIGURATION OUTPUTS (para aplicações)
# =============================================================================
output "application_config" {
  description = "Configurações para uso nas aplicações Spring Boot"
  value = {
    aws = {
      region = var.aws_region
    }
    sqs = {
      account_transaction_queue_url = module.sqs.queue_urls["account-transaction"]
      account_transaction_dlq_url   = module.sqs.queue_urls["account-transaction-dlq"]
    }
    dynamodb = {
      account_table_name     = module.dynamodb.table_names["account"]
      balance_table_name     = module.dynamodb.table_names["balance"]
      transaction_table_name = module.dynamodb.table_names["transaction"]
    }
  }
  sensitive = false
}

# =============================================================================
# TERRAFORM OUTPUTS (para referência)
# =============================================================================
output "terraform_workspace" {
  description = "Workspace do Terraform utilizado"
  value       = terraform.workspace
}

output "resource_tags" {
  description = "Tags aplicadas aos recursos"
  value       = local.common_tags
  sensitive   = false
}

# =============================================================================
# COST TRACKING
# =============================================================================
output "estimated_monthly_cost_info" {
  description = "Informações sobre custos estimados (apenas informativo)"
  value = {
    dynamodb_tables = length(keys(module.dynamodb.table_names))
    sqs_queues     = length(keys(module.sqs.queue_names))
    billing_mode   = var.dynamodb_performance_mode
    note          = "Custos reais dependem do uso. DynamoDB PAY_PER_REQUEST cobra por requisição."
  }
}

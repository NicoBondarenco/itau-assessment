# =============================================================================
# DynamoDB Module Outputs
# =============================================================================

# Nomes das tabelas
output "table_names" {
  description = "Nomes de todas as tabelas DynamoDB criadas"
  value = {
    for k, v in aws_dynamodb_table.tables : k => v.name
  }
}

# ARNs das tabelas
output "table_arns" {
  description = "ARNs de todas as tabelas DynamoDB criadas"
  value = {
    for k, v in aws_dynamodb_table.tables : k => v.arn
  }
}

# Stream ARNs (se habilitados)
output "table_stream_arns" {
  description = "ARNs dos streams das tabelas DynamoDB"
  value = {
    for k, v in aws_dynamodb_table.tables : k => v.stream_arn
    if v.stream_arn != null
  }
}

# IDs das tabelas
output "table_ids" {
  description = "IDs de todas as tabelas DynamoDB criadas"
  value = {
    for k, v in aws_dynamodb_table.tables : k => v.id
  }
}

# Detalhes completos das tabelas
output "table_details" {
  description = "Detalhes completos das tabelas DynamoDB"
  value = {
    for k, v in aws_dynamodb_table.tables : k => {
      name         = v.name
      arn          = v.arn
      id           = v.id
      hash_key     = v.hash_key
      range_key    = v.range_key
      billing_mode = v.billing_mode
      stream_arn   = v.stream_arn
    }
  }
}

# Global Secondary Indexes
output "global_secondary_indexes" {
  description = "Informações dos Global Secondary Indexes"
  value = {
    for k, v in aws_dynamodb_table.tables : k => {
      indexes = [
        for gsi in v.global_secondary_index : {
          name         = gsi.name
          hash_key     = gsi.hash_key
          range_key    = gsi.range_key
        }
      ]
    }
  }
}

# KMS Key (se criada)
output "kms_key_id" {
  description = "ID da chave KMS para criptografia DynamoDB"
  value       = var.enable_kms_encryption ? aws_kms_key.dynamodb_encryption[0].id : null
}

output "kms_key_arn" {
  description = "ARN da chave KMS para criptografia DynamoDB"
  value       = var.enable_kms_encryption ? aws_kms_key.dynamodb_encryption[0].arn : null
}

# Informações de backup
output "backup_details" {
  description = "Detalhes dos backups criados"
  value = {
    for k, v in aws_dynamodb_backup.table_backups : k => {
      name       = v.name
      table_name = v.table_name
      arn        = v.arn
    }
  }
}

# Alarmes CloudWatch (se criados)
output "cloudwatch_alarms" {
  description = "ARNs dos alarmes CloudWatch criados"
  value = var.enable_cloudwatch_alarms ? {
    read_throttles = {
      for k, v in aws_cloudwatch_metric_alarm.read_throttles : k => v.arn
    }
    write_throttles = {
      for k, v in aws_cloudwatch_metric_alarm.write_throttles : k => v.arn
    }
  } : {}
}

# Configuração para aplicações Spring Boot
output "spring_boot_config" {
  description = "Configuração das tabelas para uso no Spring Boot"
  value = {
    for k, v in aws_dynamodb_table.tables : k => {
      table_name = v.name
      region     = var.aws_region
      endpoint   = null  # null significa usar endpoint padrão da AWS
    }
  }
}

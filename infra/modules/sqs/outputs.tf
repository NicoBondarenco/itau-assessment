# =============================================================================
# SQS Module Outputs
# =============================================================================

# URLs das filas principais
output "queue_urls" {
  description = "URLs de todas as filas criadas"
  value = merge(
    {
      for k, v in aws_sqs_queue.main_queue :
      replace(k, "_", "-") => v.url
    },
    {
      for k, v in aws_sqs_queue.dead_letter_queue :
      replace(k, "_", "-") => v.url
    }
  )
}

# ARNs das filas
output "queue_arns" {
  description = "ARNs de todas as filas criadas"
  value = merge(
    {
      for k, v in aws_sqs_queue.main_queue :
      replace(k, "_", "-") => v.arn
    },
    {
      for k, v in aws_sqs_queue.dead_letter_queue :
      replace(k, "_", "-") => v.arn
    }
  )
}

# Nomes das filas
output "queue_names" {
  description = "Nomes de todas as filas criadas"
  value = merge(
    {
      for k, v in aws_sqs_queue.main_queue :
      replace(k, "_", "-") => v.name
    },
    {
      for k, v in aws_sqs_queue.dead_letter_queue :
      replace(k, "_", "-") => v.name
    }
  )
}

# KMS Key (se criada)
output "kms_key_id" {
  description = "ID da chave KMS para criptografia"
  value       = var.enable_kms_encryption ? aws_kms_key.sqs_encryption[0].id : null
}

output "kms_key_arn" {
  description = "ARN da chave KMS para criptografia"
  value       = var.enable_kms_encryption ? aws_kms_key.sqs_encryption[0].arn : null
}

# Informações específicas das filas principais
output "main_queue_details" {
  description = "Detalhes das filas principais"
  value = {
    for k, v in aws_sqs_queue.main_queue : k => {
      name = v.name
      url  = v.url
      arn  = v.arn
    }
  }
}

# Informações específicas das DLQs
output "dlq_details" {
  description = "Detalhes das Dead Letter Queues"
  value = {
    for k, v in aws_sqs_queue.dead_letter_queue : k => {
      name = v.name
      url  = v.url
      arn  = v.arn
    }
  }
}

# =============================================================================
# IAM Module Outputs
# =============================================================================

# ARNs das roles criadas
output "application_role_arns" {
  description = "ARNs das roles IAM criadas para as aplicações"
  value = {
    for k, v in aws_iam_role.application_role : k => v.arn
  }
}

# Nomes das roles criadas
output "application_role_names" {
  description = "Nomes das roles IAM criadas para as aplicações"
  value = {
    for k, v in aws_iam_role.application_role : k => v.name
  }
}

# ARNs das policies criadas
output "policy_arns" {
  description = "ARNs das policies IAM criadas"
  value = {
    dynamodb_access     = aws_iam_policy.dynamodb_access.arn
    sqs_access         = aws_iam_policy.sqs_access.arn
    cloudwatch_logs    = aws_iam_policy.cloudwatch_logs.arn
    cloudwatch_metrics = aws_iam_policy.cloudwatch_metrics.arn
    kms_access        = var.kms_key_arns != null && length(var.kms_key_arns) > 0 ? aws_iam_policy.kms_access[0].arn : null
  }
}

# Instance Profiles (se criados)
output "instance_profile_names" {
  description = "Nomes dos instance profiles criados"
  value = {
    for k, v in aws_iam_instance_profile.application_profile : k => v.name
  }
}

output "instance_profile_arns" {
  description = "ARNs dos instance profiles criados"
  value = {
    for k, v in aws_iam_instance_profile.application_profile : k => v.arn
  }
}

# Configuração para aplicações
output "application_aws_config" {
  description = "Configuração AWS para as aplicações (roles para assumir)"
  value = {
    for app in ["authorization", "validation", "web"] : app => {
      role_arn    = aws_iam_role.application_role[app].arn
      external_id = "${var.project_name}-${var.environment}-${app}"
    }
  }
  sensitive = false
}

# Informações para desenvolvimento local
output "local_development_config" {
  description = "Configuração para desenvolvimento local usando roles"
  value = {
    region = var.aws_region
    roles = {
      for app in ["authorization", "validation", "web"] : app => {
        role_arn    = aws_iam_role.application_role[app].arn
        external_id = "${var.project_name}-${var.environment}-${app}"
        command     = "aws sts assume-role --role-arn ${aws_iam_role.application_role[app].arn} --role-session-name ${app}-session --external-id ${var.project_name}-${var.environment}-${app}"
      }
    }
  }
  sensitive = false
}

# Detalhes das roles para debugging
output "role_details" {
  description = "Detalhes completos das roles criadas"
  value = {
    for k, v in aws_iam_role.application_role : k => {
      name         = v.name
      arn          = v.arn
      id           = v.id
      unique_id    = v.unique_id
      policies_attached = [
        aws_iam_policy.dynamodb_access.arn,
        aws_iam_policy.sqs_access.arn,
        aws_iam_policy.cloudwatch_logs.arn,
        aws_iam_policy.cloudwatch_metrics.arn
      ]
    }
  }
}

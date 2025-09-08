# =============================================================================
# DynamoDB Module - Authorizer Project
# =============================================================================
# Cria tabelas DynamoDB baseadas no script init.sh do LocalStack
# =============================================================================

# =============================================================================
# LOCAL VALUES
# =============================================================================
locals {
  resource_prefix = "${var.project_name}-${var.environment}"

  # Configurações padrão das tabelas
  table_defaults = {
    billing_mode                = "PAY_PER_REQUEST"
    enable_encryption          = true
    enable_backup              = true
    enable_point_in_time_recovery = true
    enable_stream              = false
    stream_view_type           = "NEW_AND_OLD_IMAGES"
  }
}

# =============================================================================
# KMS KEY FOR ENCRYPTION (se habilitado globalmente)
# =============================================================================
resource "aws_kms_key" "dynamodb_encryption" {
  count = var.enable_kms_encryption ? 1 : 0

  description             = "KMS key for ${local.resource_prefix} DynamoDB tables encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-dynamodb-kms-key"
    Type = "KMS"
  })
}

resource "aws_kms_alias" "dynamodb_encryption" {
  count = var.enable_kms_encryption ? 1 : 0

  name          = "alias/${local.resource_prefix}-dynamodb"
  target_key_id = aws_kms_key.dynamodb_encryption[0].key_id
}

# =============================================================================
# DYNAMODB TABLES
# =============================================================================
resource "aws_dynamodb_table" "tables" {
  for_each = var.tables_config

  name           = "${local.resource_prefix}-${each.value.name}"
  billing_mode   = each.value.billing_mode
  hash_key       = each.value.hash_key
  range_key      = lookup(each.value, "range_key", null)

  # Capacidade provisionada (apenas se billing_mode = PROVISIONED)
  read_capacity  = each.value.billing_mode == "PROVISIONED" ? lookup(each.value, "read_capacity", 5) : null
  write_capacity = each.value.billing_mode == "PROVISIONED" ? lookup(each.value, "write_capacity", 5) : null

  # Configuração do stream (se habilitado)
  stream_enabled   = lookup(each.value, "enable_stream", local.table_defaults.enable_stream)
  stream_view_type = lookup(each.value, "enable_stream", false) ? lookup(each.value, "stream_view_type", local.table_defaults.stream_view_type) : null

  # Atributos - definidos dinamicamente baseado na configuração
  dynamic "attribute" {
    for_each = lookup(each.value, "attributes", [
      {
        name = each.value.hash_key
        type = each.value.hash_key_type
      }
    ])

    content {
      name = attribute.value.name
      type = attribute.value.type
    }
  }

  # Global Secondary Indexes
  dynamic "global_secondary_index" {
    for_each = lookup(each.value, "global_secondary_indexes", [])

    content {
      name               = global_secondary_index.value.name
      hash_key           = global_secondary_index.value.hash_key
      range_key          = lookup(global_secondary_index.value, "range_key", null)
      projection_type    = lookup(global_secondary_index.value, "projection_type", "ALL")
      non_key_attributes = lookup(global_secondary_index.value, "non_key_attributes", null)

      # Capacidade provisionada para GSI (apenas se billing_mode = PROVISIONED)
      read_capacity  = each.value.billing_mode == "PROVISIONED" ? lookup(global_secondary_index.value, "read_capacity", 5) : null
      write_capacity = each.value.billing_mode == "PROVISIONED" ? lookup(global_secondary_index.value, "write_capacity", 5) : null
    }
  }

  # Local Secondary Indexes
  dynamic "local_secondary_index" {
    for_each = lookup(each.value, "local_secondary_indexes", [])

    content {
      name               = local_secondary_index.value.name
      range_key          = local_secondary_index.value.range_key
      projection_type    = lookup(local_secondary_index.value, "projection_type", "ALL")
      non_key_attributes = lookup(local_secondary_index.value, "non_key_attributes", null)
    }
  }

  # Configuração TTL (se especificado)
  dynamic "ttl" {
    for_each = lookup(each.value, "ttl_attribute", null) != null ? [1] : []

    content {
      attribute_name = each.value.ttl_attribute
      enabled        = true
    }
  }

  # Configuração de backup contínuo
  point_in_time_recovery {
    enabled = lookup(each.value, "enable_point_in_time_recovery", local.table_defaults.enable_point_in_time_recovery)
  }

  # Criptografia server-side
  server_side_encryption {
    enabled     = lookup(each.value, "enable_encryption", local.table_defaults.enable_encryption)
    kms_key_arn = var.enable_kms_encryption ? aws_kms_key.dynamodb_encryption[0].arn : null
  }

  # Proteção contra deleção
  deletion_protection_enabled = var.enable_deletion_protection

  tags = merge(var.tags, {
    Name      = "${local.resource_prefix}-${each.value.name}"
    Type      = "DynamoDB"
    TableName = each.value.name
  })

  # Lifecycle para evitar problemas com mudanças de GSI
  lifecycle {
    prevent_destroy = false # Altere para true em produção se necessário
  }
}

# =============================================================================
# DYNAMODB TABLE BACKUPS (se habilitado)
# =============================================================================
resource "aws_dynamodb_backup" "table_backups" {
  for_each = {
    for table_name, config in var.tables_config : table_name => config
    if lookup(config, "enable_backup", local.table_defaults.enable_backup)
  }

  table_name = aws_dynamodb_table.tables[each.key].name
  name       = "${local.resource_prefix}-${each.value.name}-backup-${formatdate("YYYY-MM-DD", timestamp())}"

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.value.name}-backup"
    Type = "DynamoDBBackup"
  })
}

# =============================================================================
# CLOUDWATCH ALARMS (Opcional)
# =============================================================================
resource "aws_cloudwatch_metric_alarm" "read_throttles" {
  for_each = var.enable_cloudwatch_alarms ? aws_dynamodb_table.tables : {}

  alarm_name          = "${each.value.name}-read-throttles"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ReadThrottles"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "0"
  alarm_description   = "This metric monitors read throttles for ${each.value.name}"
  alarm_actions       = var.alarm_sns_topic_arn != null ? [var.alarm_sns_topic_arn] : []

  dimensions = {
    TableName = each.value.name
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "write_throttles" {
  for_each = var.enable_cloudwatch_alarms ? aws_dynamodb_table.tables : {}

  alarm_name          = "${each.value.name}-write-throttles"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "WriteThrottles"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "0"
  alarm_description   = "This metric monitors write throttles for ${each.value.name}"
  alarm_actions       = var.alarm_sns_topic_arn != null ? [var.alarm_sns_topic_arn] : []

  dimensions = {
    TableName = each.value.name
  }

  tags = var.tags
}

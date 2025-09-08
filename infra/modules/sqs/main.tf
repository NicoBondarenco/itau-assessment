# =============================================================================
# SQS Module - Authorizer Project
# =============================================================================
# Cria filas SQS FIFO com Dead Letter Queue baseado no script init.sh
# =============================================================================

# =============================================================================
# LOCAL VALUES
# =============================================================================
locals {
  resource_prefix = "${var.project_name}-${var.environment}"

  # Configurações das filas com valores padrão
  queue_defaults = {
    fifo_queue                 = true
    content_based_deduplication = true
    visibility_timeout_seconds  = 30
    message_retention_seconds   = 1209600  # 14 dias (máximo)
    receive_wait_time_seconds   = 0
    max_receive_count          = 3
    fifo_throughput_limit      = "perQueue"
    deduplication_scope        = "queue"
  }
}

# =============================================================================
# KMS KEY FOR ENCRYPTION (se habilitado)
# =============================================================================
resource "aws_kms_key" "sqs_encryption" {
  count = var.enable_kms_encryption ? 1 : 0

  description             = "KMS key for ${local.resource_prefix} SQS queues encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-sqs-kms-key"
    Type = "KMS"
  })
}

resource "aws_kms_alias" "sqs_encryption" {
  count = var.enable_kms_encryption ? 1 : 0

  name          = "alias/${local.resource_prefix}-sqs"
  target_key_id = aws_kms_key.sqs_encryption[0].key_id
}

# =============================================================================
# DEAD LETTER QUEUE (deve ser criada primeiro)
# =============================================================================
resource "aws_sqs_queue" "dead_letter_queue" {
  for_each = {
    for queue_name, config in var.queue_config : queue_name => config
    if lookup(config, "is_dlq", false) == true
  }

  name                        = "${local.resource_prefix}-${each.value.name}.fifo"
  fifo_queue                 = each.value.fifo_queue
  content_based_deduplication = each.value.content_based_deduplication
  message_retention_seconds   = each.value.message_retention_seconds

  # Criptografia (se habilitada)
  kms_master_key_id                 = var.enable_kms_encryption ? aws_kms_key.sqs_encryption[0].arn : null
  kms_data_key_reuse_period_seconds = var.enable_kms_encryption ? 300 : null

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.value.name}"
    Type = "DLQ"
  })
}

# =============================================================================
# MAIN QUEUES (com referência para DLQ)
# =============================================================================
resource "aws_sqs_queue" "main_queue" {
  for_each = {
    for queue_name, config in var.queue_config : queue_name => config
    if lookup(config, "is_dlq", false) == false
  }

  name                        = "${local.resource_prefix}-${each.value.name}.fifo"
  fifo_queue                 = each.value.fifo_queue
  content_based_deduplication = each.value.content_based_deduplication
  visibility_timeout_seconds  = lookup(each.value, "visibility_timeout_seconds", local.queue_defaults.visibility_timeout_seconds)
  message_retention_seconds   = each.value.message_retention_seconds
  receive_wait_time_seconds   = lookup(each.value, "receive_wait_time_seconds", local.queue_defaults.receive_wait_time_seconds)

  # Configurações FIFO avançadas
  fifo_throughput_limit = lookup(each.value, "fifo_throughput_limit", local.queue_defaults.fifo_throughput_limit)
  deduplication_scope   = lookup(each.value, "deduplication_scope", local.queue_defaults.deduplication_scope)

  # Criptografia (se habilitada)
  kms_master_key_id                 = var.enable_kms_encryption ? aws_kms_key.sqs_encryption[0].arn : null
  kms_data_key_reuse_period_seconds = var.enable_kms_encryption ? 300 : null

  # Dead Letter Queue configuration
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dead_letter_queue["${each.key}_dlq"].arn
    maxReceiveCount     = lookup(each.value, "max_receive_count", local.queue_defaults.max_receive_count)
  })

  # Permitir que outros recursos referenciem esta fila
  redrive_allow_policy = jsonencode({
    redrivePermission = "byQueue",
    sourceQueueArns   = [aws_sqs_queue.dead_letter_queue["${each.key}_dlq"].arn]
  })

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.value.name}"
    Type = "MainQueue"
  })

  depends_on = [aws_sqs_queue.dead_letter_queue]
}

# =============================================================================
# QUEUE POLICIES (Opcional - para acesso cross-account)
# =============================================================================
resource "aws_sqs_queue_policy" "queue_policy" {
  for_each = var.enable_cross_account_access ? aws_sqs_queue.main_queue : {}

  queue_url = each.value.id

  policy = jsonencode({
    Version = "2012-10-17"
    Id      = "${each.key}-policy"
    Statement = [
      {
        Sid    = "AllowCurrentAccountFullAccess"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action   = "sqs:*"
        Resource = each.value.arn
      }
    ]
  })
}

# =============================================================================
# DATA SOURCES
# =============================================================================
data "aws_caller_identity" "current" {}

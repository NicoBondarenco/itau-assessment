# =============================================================================
# Monitoring Module - Authorizer Project
# =============================================================================
# Cria recursos de monitoramento CloudWatch para SQS e DynamoDB
# =============================================================================

# =============================================================================
# LOCAL VALUES
# =============================================================================
locals {
  resource_prefix = "${var.project_name}-${var.environment}"
}

# =============================================================================
# SNS TOPIC PARA ALERTAS (se email fornecido)
# =============================================================================
resource "aws_sns_topic" "alerts" {
  count = var.alarm_email != "" ? 1 : 0

  name = "${local.resource_prefix}-alerts"

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-alerts"
    Type = "SNS"
  })
}

resource "aws_sns_topic_subscription" "email_alerts" {
  count = var.alarm_email != "" ? 1 : 0

  topic_arn = aws_sns_topic.alerts[0].arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

# =============================================================================
# CLOUDWATCH ALARMS PARA SQS
# =============================================================================

# Número de mensagens visíveis na fila
resource "aws_cloudwatch_metric_alarm" "sqs_messages_visible" {
  for_each = var.sqs_queue_names

  alarm_name          = "${local.resource_prefix}-${each.key}-messages-visible"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ApproximateNumberOfVisibleMessages"
  namespace           = "AWS/SQS"
  period              = "300"
  statistic           = "Average"
  threshold           = var.sqs_visible_messages_threshold
  alarm_description   = "Queue ${each.value} has too many visible messages"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  ok_actions          = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = each.value
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-messages-visible"
    Type = "CloudWatchAlarm"
  })
}

# Idade da mensagem mais antiga
resource "aws_cloudwatch_metric_alarm" "sqs_message_age" {
  for_each = var.sqs_queue_names

  alarm_name          = "${local.resource_prefix}-${each.key}-message-age"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = "AWS/SQS"
  period              = "300"
  statistic           = "Maximum"
  threshold           = var.sqs_message_age_threshold
  alarm_description   = "Queue ${each.value} has old messages"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  ok_actions          = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = each.value
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-message-age"
    Type = "CloudWatchAlarm"
  })
}

# Mensagens na Dead Letter Queue
resource "aws_cloudwatch_metric_alarm" "dlq_messages" {
  for_each = {
    for k, v in var.sqs_queue_names : k => v
    if can(regex(".*dlq.*", k))
  }

  alarm_name          = "${local.resource_prefix}-${each.key}-dlq-messages"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "ApproximateNumberOfVisibleMessages"
  namespace           = "AWS/SQS"
  period              = "300"
  statistic           = "Maximum"
  threshold           = "0"
  alarm_description   = "Messages found in Dead Letter Queue ${each.value}"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    QueueName = each.value
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-dlq-messages"
    Type = "CloudWatchAlarm"
  })
}

# =============================================================================
# CLOUDWATCH ALARMS PARA DYNAMODB
# =============================================================================

# Read Throttles
resource "aws_cloudwatch_metric_alarm" "dynamodb_read_throttles" {
  for_each = var.dynamodb_table_names

  alarm_name          = "${local.resource_prefix}-${each.key}-read-throttles"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "ReadThrottles"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "0"
  alarm_description   = "DynamoDB table ${each.value} experiencing read throttles"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  ok_actions          = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    TableName = each.value
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-read-throttles"
    Type = "CloudWatchAlarm"
  })
}

# Write Throttles
resource "aws_cloudwatch_metric_alarm" "dynamodb_write_throttles" {
  for_each = var.dynamodb_table_names

  alarm_name          = "${local.resource_prefix}-${each.key}-write-throttles"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "WriteThrottles"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = "0"
  alarm_description   = "DynamoDB table ${each.value} experiencing write throttles"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  ok_actions          = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    TableName = each.value
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-write-throttles"
    Type = "CloudWatchAlarm"
  })
}

# Latência de leitura
resource "aws_cloudwatch_metric_alarm" "dynamodb_read_latency" {
  for_each = var.dynamodb_table_names

  alarm_name          = "${local.resource_prefix}-${each.key}-read-latency"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "3"
  metric_name         = "SuccessfulRequestLatency"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Average"
  threshold           = var.dynamodb_read_latency_threshold
  alarm_description   = "DynamoDB table ${each.value} read latency is high"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  ok_actions          = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    TableName = each.value
    Operation = "GetItem"
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-read-latency"
    Type = "CloudWatchAlarm"
  })
}

# Latência de escrita
resource "aws_cloudwatch_metric_alarm" "dynamodb_write_latency" {
  for_each = var.dynamodb_table_names

  alarm_name          = "${local.resource_prefix}-${each.key}-write-latency"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "3"
  metric_name         = "SuccessfulRequestLatency"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Average"
  threshold           = var.dynamodb_write_latency_threshold
  alarm_description   = "DynamoDB table ${each.value} write latency is high"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  ok_actions          = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    TableName = each.value
    Operation = "PutItem"
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-write-latency"
    Type = "CloudWatchAlarm"
  })
}

# Erros do usuário (4xx)
resource "aws_cloudwatch_metric_alarm" "dynamodb_user_errors" {
  for_each = var.dynamodb_table_names

  alarm_name          = "${local.resource_prefix}-${each.key}-user-errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "UserErrors"
  namespace           = "AWS/DynamoDB"
  period              = "300"
  statistic           = "Sum"
  threshold           = var.dynamodb_user_errors_threshold
  alarm_description   = "DynamoDB table ${each.value} experiencing user errors"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  ok_actions          = var.alarm_email != "" ? [aws_sns_topic.alerts[0].arn] : []
  treat_missing_data  = "notBreaching"

  dimensions = {
    TableName = each.value
  }

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-${each.key}-user-errors"
    Type = "CloudWatchAlarm"
  })
}

# =============================================================================
# CLOUDWATCH DASHBOARD (Opcional)
# =============================================================================
resource "aws_cloudwatch_dashboard" "main" {
  count = var.create_dashboard ? 1 : 0

  dashboard_name = "${local.resource_prefix}-monitoring"

  dashboard_body = jsonencode({
    widgets = [
      # SQS Metrics
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6

        properties = {
          metrics = [
            for queue_name in values(var.sqs_queue_names) : [
              "AWS/SQS", "ApproximateNumberOfVisibleMessages", "QueueName", queue_name
            ]
          ]
          view    = "timeSeries"
          stacked = false
          region  = var.aws_region
          title   = "SQS - Visible Messages"
          period  = 300
        }
      },
      # DynamoDB Metrics
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6

        properties = {
          metrics = [
            for table_name in values(var.dynamodb_table_names) : [
              "AWS/DynamoDB", "ConsumedReadCapacityUnits", "TableName", table_name
            ]
          ]
          view    = "timeSeries"
          stacked = false
          region  = var.aws_region
          title   = "DynamoDB - Read Capacity"
          period  = 300
        }
      }
    ]
  })
}

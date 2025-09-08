# =============================================================================
# Monitoring Module Outputs
# =============================================================================

# SNS Topic
output "sns_topic_arn" {
  description = "ARN do tópico SNS para alertas"
  value       = var.alarm_email != "" ? aws_sns_topic.alerts[0].arn : null
}

# Alarm ARNs
output "alarm_arns" {
  description = "ARNs de todos os alarmes criados"
  value = merge(
    # SQS Alarms
    {
      for k, v in aws_cloudwatch_metric_alarm.sqs_messages_visible : "sqs-${k}-messages-visible" => v.arn
    },
    {
      for k, v in aws_cloudwatch_metric_alarm.sqs_message_age : "sqs-${k}-message-age" => v.arn
    },
    {
      for k, v in aws_cloudwatch_metric_alarm.dlq_messages : "sqs-${k}-dlq-messages" => v.arn
    },
    # DynamoDB Alarms
    {
      for k, v in aws_cloudwatch_metric_alarm.dynamodb_read_throttles : "dynamodb-${k}-read-throttles" => v.arn
    },
    {
      for k, v in aws_cloudwatch_metric_alarm.dynamodb_write_throttles : "dynamodb-${k}-write-throttles" => v.arn
    },
    {
      for k, v in aws_cloudwatch_metric_alarm.dynamodb_read_latency : "dynamodb-${k}-read-latency" => v.arn
    },
    {
      for k, v in aws_cloudwatch_metric_alarm.dynamodb_write_latency : "dynamodb-${k}-write-latency" => v.arn
    },
    {
      for k, v in aws_cloudwatch_metric_alarm.dynamodb_user_errors : "dynamodb-${k}-user-errors" => v.arn
    }
  )
}

# Dashboard
output "dashboard_url" {
  description = "URL do dashboard CloudWatch"
  value       = var.create_dashboard ? "https://${var.aws_region}.console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards:name=${aws_cloudwatch_dashboard.main[0].dashboard_name}" : null
}

output "dashboard_name" {
  description = "Nome do dashboard CloudWatch"
  value       = var.create_dashboard ? aws_cloudwatch_dashboard.main[0].dashboard_name : null
}

# Alarm details for debugging
output "alarm_details" {
  description = "Detalhes dos alarmes para debugging"
  value = {
    sqs_alarms = {
      messages_visible = {
        for k, v in aws_cloudwatch_metric_alarm.sqs_messages_visible : k => {
          name      = v.alarm_name
          threshold = v.threshold
        }
      }
      message_age = {
        for k, v in aws_cloudwatch_metric_alarm.sqs_message_age : k => {
          name      = v.alarm_name
          threshold = v.threshold
        }
      }
      dlq_messages = {
        for k, v in aws_cloudwatch_metric_alarm.dlq_messages : k => {
          name      = v.alarm_name
          threshold = v.threshold
        }
      }
    }
    dynamodb_alarms = {
      read_throttles = {
        for k, v in aws_cloudwatch_metric_alarm.dynamodb_read_throttles : k => {
          name      = v.alarm_name
          threshold = v.threshold
        }
      }
      write_throttles = {
        for k, v in aws_cloudwatch_metric_alarm.dynamodb_write_throttles : k => {
          name      = v.alarm_name
          threshold = v.threshold
        }
      }
    }
  }
}

# =============================================================================
# IAM Module - Authorizer Project
# =============================================================================
# Cria roles e policies IAM para as aplicações acessarem SQS e DynamoDB
# =============================================================================

# =============================================================================
# LOCAL VALUES
# =============================================================================
locals {
  resource_prefix = "${var.project_name}-${var.environment}"

  applications = [
    "authorization",
    "validation",
    "web"
  ]
}

# =============================================================================
# IAM ROLE PARA AS APLICAÇÕES
# =============================================================================
resource "aws_iam_role" "application_role" {
  for_each = toset(local.applications)

  name = "${local.resource_prefix}-${each.key}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = ["ec2.amazonaws.com", "ecs-tasks.amazonaws.com", "lambda.amazonaws.com"]
        }
      },
      # Permitir que a aplicação assuma a role (para desenvolvimento local)
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Condition = {
          StringEquals = {
            "sts:ExternalId" = "${local.resource_prefix}-${each.key}"
          }
        }
      }
    ]
  })

  tags = merge(var.tags, {
    Name        = "${local.resource_prefix}-${each.key}-role"
    Application = each.key
    Type        = "IAMRole"
  })
}

# =============================================================================
# POLICY PARA ACESSO AO DYNAMODB
# =============================================================================
resource "aws_iam_policy" "dynamodb_access" {
  name        = "${local.resource_prefix}-dynamodb-access"
  description = "Policy for DynamoDB access for ${var.project_name} applications"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:BatchGetItem",
          "dynamodb:BatchWriteItem",
          "dynamodb:ConditionCheckItem",
          "dynamodb:DeleteItem",
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:Query",
          "dynamodb:Scan",
          "dynamodb:UpdateItem",
          "dynamodb:DescribeTable",
          "dynamodb:GetRecords",
          "dynamodb:GetShardIterator",
          "dynamodb:DescribeStream",
          "dynamodb:ListStreams"
        ]
        Resource = flatten([
          values(var.dynamodb_table_arns),
          [for arn in values(var.dynamodb_table_arns) : "${arn}/*"],
          [for arn in values(var.dynamodb_table_arns) : "${arn}/index/*"]
        ])
      }
    ]
  })

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-dynamodb-access"
    Type = "IAMPolicy"
  })
}

# =============================================================================
# POLICY PARA ACESSO AO SQS
# =============================================================================
resource "aws_iam_policy" "sqs_access" {
  name        = "${local.resource_prefix}-sqs-access"
  description = "Policy for SQS access for ${var.project_name} applications"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:GetQueueUrl",
          "sqs:ListQueues",
          "sqs:ReceiveMessage",
          "sqs:SendMessage",
          "sqs:SendMessageBatch",
          "sqs:ChangeMessageVisibility",
          "sqs:ChangeMessageVisibilityBatch"
        ]
        Resource = values(var.sqs_queue_arns)
      }
    ]
  })

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-sqs-access"
    Type = "IAMPolicy"
  })
}

# =============================================================================
# POLICY PARA CLOUDWATCH LOGS
# =============================================================================
resource "aws_iam_policy" "cloudwatch_logs" {
  name        = "${local.resource_prefix}-cloudwatch-logs"
  description = "Policy for CloudWatch Logs access for ${var.project_name} applications"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogGroups",
          "logs:DescribeLogStreams"
        ]
        Resource = "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/${var.project_name}/*"
      }
    ]
  })

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-cloudwatch-logs"
    Type = "IAMPolicy"
  })
}

# =============================================================================
# POLICY PARA CLOUDWATCH METRICS
# =============================================================================
resource "aws_iam_policy" "cloudwatch_metrics" {
  name        = "${local.resource_prefix}-cloudwatch-metrics"
  description = "Policy for CloudWatch Metrics access for ${var.project_name} applications"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "cloudwatch:PutMetricData",
          "cloudwatch:GetMetricStatistics",
          "cloudwatch:ListMetrics"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "cloudwatch:namespace" = var.project_name
          }
        }
      }
    ]
  })

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-cloudwatch-metrics"
    Type = "IAMPolicy"
  })
}

# =============================================================================
# POLICY PARA KMS (se habilitado)
# =============================================================================
resource "aws_iam_policy" "kms_access" {
  count = var.kms_key_arns != null && length(var.kms_key_arns) > 0 ? 1 : 0

  name        = "${local.resource_prefix}-kms-access"
  description = "Policy for KMS access for ${var.project_name} applications"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:DescribeKey"
        ]
        Resource = var.kms_key_arns
      }
    ]
  })

  tags = merge(var.tags, {
    Name = "${local.resource_prefix}-kms-access"
    Type = "IAMPolicy"
  })
}

# =============================================================================
# ATTACH POLICIES TO ROLES
# =============================================================================
resource "aws_iam_role_policy_attachment" "dynamodb_access" {
  for_each = toset(local.applications)

  role       = aws_iam_role.application_role[each.key].name
  policy_arn = aws_iam_policy.dynamodb_access.arn
}

resource "aws_iam_role_policy_attachment" "sqs_access" {
  for_each = toset(local.applications)

  role       = aws_iam_role.application_role[each.key].name
  policy_arn = aws_iam_policy.sqs_access.arn
}

resource "aws_iam_role_policy_attachment" "cloudwatch_logs" {
  for_each = toset(local.applications)

  role       = aws_iam_role.application_role[each.key].name
  policy_arn = aws_iam_policy.cloudwatch_logs.arn
}

resource "aws_iam_role_policy_attachment" "cloudwatch_metrics" {
  for_each = toset(local.applications)

  role       = aws_iam_role.application_role[each.key].name
  policy_arn = aws_iam_policy.cloudwatch_metrics.arn
}

resource "aws_iam_role_policy_attachment" "kms_access" {
  for_each = var.kms_key_arns != null && length(var.kms_key_arns) > 0 ? toset(local.applications) : toset([])

  role       = aws_iam_role.application_role[each.key].name
  policy_arn = aws_iam_policy.kms_access[0].arn
}

# =============================================================================
# INSTANCE PROFILE (para EC2 se necessário)
# =============================================================================
resource "aws_iam_instance_profile" "application_profile" {
  for_each = var.create_instance_profiles ? toset(local.applications) : toset([])

  name = "${local.resource_prefix}-${each.key}-profile"
  role = aws_iam_role.application_role[each.key].name

  tags = merge(var.tags, {
    Name        = "${local.resource_prefix}-${each.key}-profile"
    Application = each.key
    Type        = "InstanceProfile"
  })
}

# =============================================================================
# DATA SOURCES
# =============================================================================
data "aws_caller_identity" "current" {}

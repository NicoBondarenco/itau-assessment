# =============================================================================
# MSK Module Outputs
# =============================================================================

# =============================================================================
# CLUSTER INFORMATION
# =============================================================================
output "cluster_arn" {
  description = "ARN do cluster MSK"
  value       = aws_msk_cluster.cluster.arn
}

output "cluster_name" {
  description = "Nome do cluster MSK"
  value       = aws_msk_cluster.cluster.cluster_name
}

output "cluster_uuid" {
  description = "UUID do cluster MSK"
  value       = aws_msk_cluster.cluster.cluster_uuid
}

output "kafka_version" {
  description = "Versão do Kafka"
  value       = aws_msk_cluster.cluster.kafka_version
}

# =============================================================================
# CONNECTION INFORMATION
# =============================================================================
output "bootstrap_brokers" {
  description = "Lista de bootstrap brokers (PLAINTEXT)"
  value       = aws_msk_cluster.cluster.bootstrap_brokers
}

output "bootstrap_brokers_sasl_scram" {
  description = "Lista de bootstrap brokers (SASL/SCRAM)"
  value       = aws_msk_cluster.cluster.bootstrap_brokers_sasl_scram
}

output "bootstrap_brokers_tls" {
  description = "Lista de bootstrap brokers (TLS)"
  value       = aws_msk_cluster.cluster.bootstrap_brokers_tls
}

output "zookeeper_connect_string" {
  description = "String de conexão do Zookeeper"
  value       = aws_msk_cluster.cluster.zookeeper_connect_string
}

# =============================================================================
# SECURITY INFORMATION
# =============================================================================
output "security_group_id" {
  description = "ID do security group do cluster"
  value       = aws_security_group.msk_cluster_sg.id
}

output "security_group_arn" {
  description = "ARN do security group do cluster"
  value       = aws_security_group.msk_cluster_sg.arn
}

output "kms_key_id" {
  description = "ID da chave KMS para criptografia"
  value       = var.enable_encryption_at_rest ? aws_kms_key.msk_encryption[0].id : null
}

output "kms_key_arn" {
  description = "ARN da chave KMS para criptografia"
  value       = var.enable_encryption_at_rest ? aws_kms_key.msk_encryption[0].arn : null
}

# =============================================================================
# NETWORK INFORMATION
# =============================================================================
output "vpc_id" {
  description = "ID da VPC utilizada"
  value       = var.vpc_id != null ? var.vpc_id : aws_vpc.msk_vpc[0].id
}

output "subnet_ids" {
  description = "IDs das subnets utilizadas"
  value       = var.subnet_ids != null ? var.subnet_ids : aws_subnet.msk_private_subnet[*].id
}

# =============================================================================
# MONITORING INFORMATION
# =============================================================================
output "cloudwatch_log_group_name" {
  description = "Nome do log group do CloudWatch"
  value       = var.enable_logging ? aws_cloudwatch_log_group.msk_log_group[0].name : null
}

output "cloudwatch_log_group_arn" {
  description = "ARN do log group do CloudWatch"
  value       = var.enable_logging ? aws_cloudwatch_log_group.msk_log_group[0].arn : null
}

# =============================================================================
# TOPIC INFORMATION
# =============================================================================
output "kafka_topics" {
  description = "Lista de tópicos Kafka configurados"
  value = [
    for topic in var.kafka_topics : {
      name               = topic.name
      partitions         = topic.partitions
      replication_factor = topic.replication_factor
    }
  ]
}

output "transaction_executed_event_topic" {
  description = "Informações do tópico transaction-executed-event"
  value = {
    name               = "transaction-executed-event"
    partitions         = 3
    replication_factor = 3
  }
}

# =============================================================================
# CONFIGURATION OUTPUTS (para aplicações)
# =============================================================================
output "spring_kafka_config" {
  description = "Configurações para Spring Kafka"
  value = {
    bootstrap_servers = aws_msk_cluster.cluster.bootstrap_brokers_tls
    security_protocol = var.encryption_in_transit_client_broker == "TLS" ? "SSL" : "PLAINTEXT"
    topic_name       = "transaction-executed-event"
  }
  sensitive = false
}

# =============================================================================
# CLUSTER DETAILS
# =============================================================================
output "cluster_details" {
  description = "Detalhes completos do cluster MSK"
  value = {
    name                   = aws_msk_cluster.cluster.cluster_name
    arn                   = aws_msk_cluster.cluster.arn
    kafka_version         = aws_msk_cluster.cluster.kafka_version
    number_of_broker_nodes = var.number_of_broker_nodes
    instance_type         = var.instance_type
    ebs_volume_size       = var.ebs_volume_size
    enhanced_monitoring   = var.enhanced_monitoring
    encryption_at_rest    = var.enable_encryption_at_rest
    encryption_in_transit = var.encryption_in_transit_client_broker
  }
}

# =============================================================================
# EKS Module Outputs
# =============================================================================

# =============================================================================
# CLUSTER INFORMATION
# =============================================================================
output "cluster_id" {
  description = "ID do cluster EKS"
  value       = aws_eks_cluster.cluster.id
}

output "cluster_name" {
  description = "Nome do cluster EKS"
  value       = aws_eks_cluster.cluster.name
}

output "cluster_arn" {
  description = "ARN do cluster EKS"
  value       = aws_eks_cluster.cluster.arn
}

output "cluster_endpoint" {
  description = "Endpoint da API do cluster EKS"
  value       = aws_eks_cluster.cluster.endpoint
}

output "cluster_version" {
  description = "Versão do Kubernetes do cluster"
  value       = aws_eks_cluster.cluster.version
}

output "cluster_platform_version" {
  description = "Platform version do cluster EKS"
  value       = aws_eks_cluster.cluster.platform_version
}

output "cluster_status" {
  description = "Status do cluster EKS"
  value       = aws_eks_cluster.cluster.status
}

# =============================================================================
# CLUSTER SECURITY
# =============================================================================
output "cluster_security_group_id" {
  description = "Security group ID do cluster"
  value       = aws_eks_cluster.cluster.vpc_config[0].cluster_security_group_id
}

output "cluster_certificate_authority_data" {
  description = "Dados do certificado da autoridade certificadora"
  value       = aws_eks_cluster.cluster.certificate_authority[0].data
}

# =============================================================================
# OIDC IDENTITY PROVIDER
# =============================================================================
output "cluster_oidc_issuer_url" {
  description = "URL do OIDC issuer do cluster"
  value       = aws_eks_cluster.cluster.identity[0].oidc[0].issuer
}

output "oidc_provider_arn" {
  description = "ARN do OIDC provider"
  value       = aws_iam_openid_connect_provider.cluster_oidc.arn
}

# =============================================================================
# NODE GROUP INFORMATION
# =============================================================================
output "node_group_arn" {
  description = "ARN do node group"
  value       = aws_eks_node_group.main.arn
}

output "node_group_status" {
  description = "Status do node group"
  value       = aws_eks_node_group.main.status
}

output "node_group_capacity_type" {
  description = "Tipo de capacidade do node group"
  value       = aws_eks_node_group.main.capacity_type
}

output "node_group_instance_types" {
  description = "Tipos de instância do node group"
  value       = aws_eks_node_group.main.instance_types
}

output "node_group_remote_access" {
  description = "Configuração de acesso remoto do node group"
  value       = aws_eks_node_group.main.remote_access
  sensitive   = true
}

# =============================================================================
# NETWORK INFORMATION
# =============================================================================
output "vpc_id" {
  description = "ID da VPC utilizada"
  value       = var.vpc_id != null ? var.vpc_id : aws_vpc.eks_vpc[0].id
}

output "public_subnet_ids" {
  description = "IDs das subnets públicas"
  value       = var.vpc_id != null ? var.subnet_ids : aws_subnet.eks_public_subnet[*].id
}

output "private_subnet_ids" {
  description = "IDs das subnets privadas"
  value       = var.vpc_id != null ? var.private_subnet_ids : aws_subnet.eks_private_subnet[*].id
}

# =============================================================================
# IAM ROLES AND POLICIES
# =============================================================================
output "cluster_iam_role_arn" {
  description = "ARN da role IAM do cluster"
  value       = aws_iam_role.eks_cluster_role.arn
}

output "node_group_iam_role_arn" {
  description = "ARN da role IAM do node group"
  value       = aws_iam_role.eks_node_group_role.arn
}

output "aws_load_balancer_controller_role_arn" {
  description = "ARN da role do AWS Load Balancer Controller"
  value       = aws_iam_role.aws_load_balancer_controller.arn
}

output "application_policy_arn" {
  description = "ARN da policy para aplicações"
  value       = aws_iam_policy.eks_app_policy.arn
}

# =============================================================================
# CLOUDWATCH LOGS
# =============================================================================
output "cluster_log_group_name" {
  description = "Nome do log group do CloudWatch"
  value       = length(var.cluster_log_types) > 0 ? aws_cloudwatch_log_group.eks_cluster_logs[0].name : null
}

output "cluster_log_group_arn" {
  description = "ARN do log group do CloudWatch"
  value       = length(var.cluster_log_types) > 0 ? aws_cloudwatch_log_group.eks_cluster_logs[0].arn : null
}

# =============================================================================
# KUBECTL CONFIGURATION
# =============================================================================
output "kubectl_config" {
  description = "Configuração kubectl para conectar ao cluster"
  value = {
    cluster_name = aws_eks_cluster.cluster.name
    region      = var.aws_region
    command     = "aws eks update-kubeconfig --region ${var.aws_region} --name ${aws_eks_cluster.cluster.name}"
  }
}

# =============================================================================
# APPLICATION INFORMATION
# =============================================================================
output "applications_config" {
  description = "Configurações das aplicações deployadas"
  value = {
    for app_key, app_config in var.applications : app_key => {
      name      = app_config.name
      port      = app_config.port
      replicas  = app_config.replicas
      image     = app_config.image
      namespace = "default"
      service_name = "${app_config.name}-service"
    }
  }
}

# =============================================================================
# LOAD BALANCER INFORMATION
# =============================================================================
output "load_balancer_controller_enabled" {
  description = "Indica se o AWS Load Balancer Controller está habilitado"
  value       = var.enable_load_balancer
}

output "load_balancer_type" {
  description = "Tipo de load balancer configurado"
  value       = var.load_balancer_type
}

# =============================================================================
# CLUSTER ACCESS INFORMATION
# =============================================================================
output "cluster_access_config" {
  description = "Configuração de acesso ao cluster"
  value = {
    endpoint                    = aws_eks_cluster.cluster.endpoint
    certificate_authority_data  = aws_eks_cluster.cluster.certificate_authority[0].data
    cluster_security_group_id   = aws_eks_cluster.cluster.vpc_config[0].cluster_security_group_id
    endpoint_public_access      = var.endpoint_public_access
    endpoint_private_access     = var.endpoint_private_access
  }
  sensitive = true
}

# =============================================================================
# TAGS INFORMATION
# =============================================================================
output "cluster_tags" {
  description = "Tags aplicadas ao cluster"
  value       = aws_eks_cluster.cluster.tags
}

# =============================================================================
# ENVIRONMENT CONFIGURATION FOR APPLICATIONS
# =============================================================================
output "application_environment_config" {
  description = "Configuração de ambiente para as aplicações Spring Boot"
  value = {
    aws_region            = var.aws_region
    kubernetes_namespace  = "default"
    cluster_name         = aws_eks_cluster.cluster.name
    service_account_annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.eks_node_group_role.arn
    }
  }
}

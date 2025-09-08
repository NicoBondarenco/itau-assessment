# =============================================================================
# EKS Module Variables
# =============================================================================

variable "project_name" {
  description = "Nome do projeto"
  type        = string
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
}

variable "aws_region" {
  description = "Região AWS"
  type        = string
}

# =============================================================================
# VPC CONFIGURATION
# =============================================================================
variable "vpc_id" {
  description = "ID da VPC existente (se null, uma nova será criada)"
  type        = string
  default     = null
}

variable "vpc_cidr" {
  description = "CIDR block para a VPC (usado apenas se vpc_id for null)"
  type        = string
  default     = "10.0.0.0/16"

  validation {
    condition     = can(cidrhost(var.vpc_cidr, 0))
    error_message = "VPC CIDR deve ser um bloco CIDR válido."
  }
}

variable "subnet_ids" {
  description = "IDs das subnets existentes (se null, novas serão criadas)"
  type        = list(string)
  default     = null
}

variable "private_subnet_ids" {
  description = "IDs das subnets privadas para node groups"
  type        = list(string)
  default     = null
}

# =============================================================================
# EKS CLUSTER CONFIGURATION
# =============================================================================
variable "kubernetes_version" {
  description = "Versão do Kubernetes"
  type        = string
  default     = "1.28"

  validation {
    condition     = can(regex("^[0-9]+\\.[0-9]+$", var.kubernetes_version))
    error_message = "Versão do Kubernetes deve ter formato válido (ex: 1.28)."
  }
}

variable "endpoint_private_access" {
  description = "Habilitar acesso privado ao endpoint da API"
  type        = bool
  default     = true
}

variable "endpoint_public_access" {
  description = "Habilitar acesso público ao endpoint da API"
  type        = bool
  default     = true
}

variable "endpoint_public_access_cidrs" {
  description = "Blocos CIDR permitidos para acesso público"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

# =============================================================================
# EKS LOGGING
# =============================================================================
variable "cluster_log_types" {
  description = "Tipos de logs do cluster EKS para habilitar"
  type        = list(string)
  default     = ["api", "audit", "authenticator", "controllerManager", "scheduler"]

  validation {
    condition = alltrue([
      for log_type in var.cluster_log_types : contains([
        "api", "audit", "authenticator", "controllerManager", "scheduler"
      ], log_type)
    ])
    error_message = "Tipos de log válidos: api, audit, authenticator, controllerManager, scheduler."
  }
}

variable "cluster_log_retention_days" {
  description = "Dias de retenção dos logs do cluster"
  type        = number
  default     = 7

  validation {
    condition = contains([
      1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653
    ], var.cluster_log_retention_days)
    error_message = "Log retention deve ser um valor válido do CloudWatch."
  }
}

# =============================================================================
# EKS ENCRYPTION
# =============================================================================
variable "cluster_encryption_config" {
  description = "Configuração de criptografia do cluster"
  type = list(object({
    provider_key_arn = string
    resources        = list(string)
  }))
  default = []
}

# =============================================================================
# NODE GROUP CONFIGURATION
# =============================================================================
variable "node_group_instance_types" {
  description = "Tipos de instância para o node group"
  type        = list(string)
  default     = ["t3.medium"]

  validation {
    condition     = length(var.node_group_instance_types) > 0
    error_message = "Deve haver pelo menos um tipo de instância."
  }
}

variable "node_group_capacity_type" {
  description = "Tipo de capacidade do node group (ON_DEMAND ou SPOT)"
  type        = string
  default     = "ON_DEMAND"

  validation {
    condition     = contains(["ON_DEMAND", "SPOT"], var.node_group_capacity_type)
    error_message = "Capacity type deve ser ON_DEMAND ou SPOT."
  }
}

variable "node_group_ami_type" {
  description = "Tipo de AMI para o node group"
  type        = string
  default     = "AL2_x86_64"

  validation {
    condition = contains([
      "AL2_x86_64", "AL2_x86_64_GPU", "AL2_ARM_64",
      "CUSTOM", "BOTTLEROCKET_ARM_64", "BOTTLEROCKET_x86_64"
    ], var.node_group_ami_type)
    error_message = "AMI type deve ser um dos tipos suportados pelo EKS."
  }
}

variable "node_group_disk_size" {
  description = "Tamanho do disco dos nós em GB"
  type        = number
  default     = 20

  validation {
    condition     = var.node_group_disk_size >= 20 && var.node_group_disk_size <= 1000
    error_message = "Disk size deve ser entre 20 e 1000 GB."
  }
}

variable "node_group_desired_size" {
  description = "Número desejado de nós"
  type        = number
  default     = 2

  validation {
    condition     = var.node_group_desired_size >= 1
    error_message = "Desired size deve ser pelo menos 1."
  }
}

variable "node_group_max_size" {
  description = "Número máximo de nós"
  type        = number
  default     = 4

  validation {
    condition     = var.node_group_max_size >= 1
    error_message = "Max size deve ser pelo menos 1."
  }
}

variable "node_group_min_size" {
  description = "Número mínimo de nós"
  type        = number
  default     = 1

  validation {
    condition     = var.node_group_min_size >= 1
    error_message = "Min size deve ser pelo menos 1."
  }
}

variable "node_group_max_unavailable" {
  description = "Número máximo de nós indisponíveis durante atualizações"
  type        = number
  default     = 1

  validation {
    condition     = var.node_group_max_unavailable >= 1
    error_message = "Max unavailable deve ser pelo menos 1."
  }
}

variable "node_group_labels" {
  description = "Labels para os nós do cluster"
  type        = map(string)
  default     = {}
}

variable "node_group_launch_template" {
  description = "Configuração do launch template"
  type = object({
    id      = optional(string)
    name    = optional(string)
    version = optional(string)
  })
  default = null
}

variable "node_group_remote_access" {
  description = "Configuração de acesso remoto aos nós"
  type = object({
    ec2_ssh_key               = optional(string)
    source_security_group_ids = optional(list(string))
  })
  default = null
}

# =============================================================================
# INTEGRATION WITH OTHER AWS SERVICES
# =============================================================================
variable "dynamodb_table_arns" {
  description = "ARNs das tabelas DynamoDB para permissões IAM"
  type        = list(string)
  default     = null
}

variable "sqs_queue_arns" {
  description = "ARNs das filas SQS para permissões IAM"
  type        = list(string)
  default     = null
}

variable "msk_cluster_arn" {
  description = "ARN do cluster MSK para permissões IAM"
  type        = string
  default     = null
}

# =============================================================================
# APPLICATION CONFIGURATION
# =============================================================================
variable "applications" {
  description = "Configurações das aplicações para deployment"
  type = map(object({
    name           = string
    image          = string
    port           = number
    replicas       = optional(number, 2)
    cpu_request    = optional(string, "100m")
    cpu_limit      = optional(string, "500m")
    memory_request = optional(string, "256Mi")
    memory_limit   = optional(string, "512Mi")
    env_vars       = optional(map(string), {})
    health_check   = optional(object({
      path                = optional(string, "/actuator/health")
      initial_delay       = optional(number, 30)
      period              = optional(number, 30)
      timeout             = optional(number, 10)
      success_threshold   = optional(number, 1)
      failure_threshold   = optional(number, 3)
    }), {})
  }))
  default = {
    authorization = {
      name  = "app-authorization"
      image = "app-authorization:latest"
      port  = 8200
      env_vars = {
        SERVER_PORT = "8200"
        ENVIRONMENT = "k8s"
      }
    }
    validation = {
      name  = "app-validation"
      image = "app-validation:latest"
      port  = 8100
      env_vars = {
        SERVER_PORT = "8100"
        ENVIRONMENT = "k8s"
      }
    }
  }
}

# =============================================================================
# LOAD BALANCER CONFIGURATION
# =============================================================================
variable "enable_load_balancer" {
  description = "Habilitar AWS Load Balancer Controller"
  type        = bool
  default     = true
}

variable "load_balancer_type" {
  description = "Tipo de load balancer (application ou network)"
  type        = string
  default     = "application"

  validation {
    condition     = contains(["application", "network"], var.load_balancer_type)
    error_message = "Load balancer type deve ser 'application' ou 'network'."
  }
}

# =============================================================================
# KUBERNETES CONFIGURATION
# =============================================================================
variable "create_namespace" {
  description = "Criar namespace customizado para as aplicações"
  type        = bool
  default     = false
}

variable "namespace_name" {
  description = "Nome do namespace Kubernetes"
  type        = string
  default     = "authorizer"
}

variable "aws_credentials" {
  description = "Credenciais AWS para as aplicações (opcional se usando IAM roles)"
  type = object({
    access_key = string
    secret_key = string
  })
  default   = null
  sensitive = true
}

variable "kafka_brokers" {
  description = "Lista de brokers Kafka"
  type        = string
  default     = null
}

variable "schema_registry_url" {
  description = "URL do Schema Registry"
  type        = string
  default     = null
}

variable "dynamodb_endpoint" {
  description = "Endpoint do DynamoDB (opcional)"
  type        = string
  default     = null
}

variable "sqs_endpoint" {
  description = "Endpoint do SQS (opcional)"
  type        = string
  default     = null
}

variable "domain_name" {
  description = "Nome de domínio para o Ingress"
  type        = string
  default     = null
}

variable "ssl_certificate_arn" {
  description = "ARN do certificado SSL para HTTPS"
  type        = string
  default     = null
}

variable "enable_hpa" {
  description = "Habilitar Horizontal Pod Autoscaler"
  type        = bool
  default     = false
}

# =============================================================================
# TAGS
# =============================================================================
variable "tags" {
  description = "Tags para aplicar aos recursos"
  type        = map(string)
  default     = {}
}

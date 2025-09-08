# =============================================================================
# Kubernetes Manifests - Authorizer Applications
# =============================================================================
# Deploy das aplicações app-authorization e app-validation
# =============================================================================

terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.20"
    }
  }
}

provider "kubernetes" {
  host                   = aws_eks_cluster.cluster.endpoint
  cluster_ca_certificate = base64decode(aws_eks_cluster.cluster.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.cluster_auth.token
}

# =============================================================================
# NAMESPACE (Opcional - usando default por simplicidade)
# =============================================================================
resource "kubernetes_namespace" "authorizer" {
  count = var.create_namespace ? 1 : 0

  metadata {
    name = var.namespace_name

    labels = {
      name        = var.namespace_name
      environment = var.environment
      project     = var.project_name
    }
  }
}

locals {
  namespace = var.create_namespace ? kubernetes_namespace.authorizer[0].metadata[0].name : "default"
}

# =============================================================================
# CONFIGMAP PARA CONFIGURAÇÕES COMUNS
# =============================================================================
resource "kubernetes_config_map" "app_config" {
  metadata {
    name      = "authorizer-config"
    namespace = local.namespace
  }

  data = {
    AWS_REGION                = var.aws_region
    ENVIRONMENT              = var.environment
    KAFKA_BROKERS           = var.kafka_brokers != null ? var.kafka_brokers : ""
    SCHEMA_REGISTRY_URL     = var.schema_registry_url != null ? var.schema_registry_url : ""
    AWS_DYNAMODB_ENDPOINT   = var.dynamodb_endpoint != null ? var.dynamodb_endpoint : ""
    AWS_SQS_ENDPOINT        = var.sqs_endpoint != null ? var.sqs_endpoint : ""
  }
}

# =============================================================================
# SECRET PARA CREDENCIAIS AWS (se necessário)
# =============================================================================
resource "kubernetes_secret" "aws_credentials" {
  count = var.aws_credentials != null ? 1 : 0

  metadata {
    name      = "aws-credentials"
    namespace = local.namespace
  }

  type = "Opaque"

  data = {
    AWS_ACCESS_KEY = base64encode(var.aws_credentials.access_key)
    AWS_SECRET_KEY = base64encode(var.aws_credentials.secret_key)
  }
}

# =============================================================================
# DEPLOYMENTS
# =============================================================================
resource "kubernetes_deployment" "applications" {
  for_each = var.applications

  metadata {
    name      = each.value.name
    namespace = local.namespace

    labels = {
      app         = each.value.name
      environment = var.environment
      project     = var.project_name
    }
  }

  spec {
    replicas = each.value.replicas

    selector {
      match_labels = {
        app = each.value.name
      }
    }

    template {
      metadata {
        labels = {
          app         = each.value.name
          environment = var.environment
          project     = var.project_name
        }
      }

      spec {
        service_account_name = kubernetes_service_account.app_service_account.metadata[0].name

        container {
          image = each.value.image
          name  = each.value.name

          port {
            container_port = each.value.port
            protocol       = "TCP"
          }

          # Environment variables
          dynamic "env" {
            for_each = merge(each.value.env_vars, {
              AWS_REGION = var.aws_region
              ENVIRONMENT = var.environment
            })
            content {
              name  = env.key
              value = env.value
            }
          }

          # Environment variables from ConfigMap
          env_from {
            config_map_ref {
              name = kubernetes_config_map.app_config.metadata[0].name
            }
          }

          # Environment variables from Secret (if exists)
          dynamic "env_from" {
            for_each = var.aws_credentials != null ? [1] : []
            content {
              secret_ref {
                name = kubernetes_secret.aws_credentials[0].metadata[0].name
              }
            }
          }

          # Resource limits and requests
          resources {
            requests = {
              cpu    = each.value.cpu_request
              memory = each.value.memory_request
            }
            limits = {
              cpu    = each.value.cpu_limit
              memory = each.value.memory_limit
            }
          }

          # Health checks
          liveness_probe {
            http_get {
              path = each.value.health_check.path
              port = each.value.port
            }
            initial_delay_seconds = each.value.health_check.initial_delay
            period_seconds        = each.value.health_check.period
            timeout_seconds       = each.value.health_check.timeout
            success_threshold     = each.value.health_check.success_threshold
            failure_threshold     = each.value.health_check.failure_threshold
          }

          readiness_probe {
            http_get {
              path = each.value.health_check.path
              port = each.value.port
            }
            initial_delay_seconds = each.value.health_check.initial_delay
            period_seconds        = each.value.health_check.period
            timeout_seconds       = each.value.health_check.timeout
            success_threshold     = each.value.health_check.success_threshold
            failure_threshold     = each.value.health_check.failure_threshold
          }

          # Security context
          security_context {
            run_as_non_root = true
            run_as_user     = 1001
            run_as_group    = 1001
          }
        }

        # Pod security context
        security_context {
          fs_group = 1001
        }

        # Restart policy
        restart_policy = "Always"
      }
    }
  }

  depends_on = [
    aws_eks_node_group.main,
    kubernetes_service_account.app_service_account
  ]
}

# =============================================================================
# SERVICES
# =============================================================================
resource "kubernetes_service" "applications" {
  for_each = var.applications

  metadata {
    name      = "${each.value.name}-service"
    namespace = local.namespace

    labels = {
      app         = each.value.name
      environment = var.environment
      project     = var.project_name
    }
  }

  spec {
    selector = {
      app = each.value.name
    }

    port {
      name        = "http"
      port        = each.value.port
      target_port = each.value.port
      protocol    = "TCP"
    }

    type = "ClusterIP"
  }

  depends_on = [kubernetes_deployment.applications]
}

# =============================================================================
# SERVICE ACCOUNT
# =============================================================================
resource "kubernetes_service_account" "app_service_account" {
  metadata {
    name      = "authorizer-service-account"
    namespace = local.namespace

    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.eks_node_group_role.arn
    }

    labels = {
      app         = "authorizer"
      environment = var.environment
      project     = var.project_name
    }
  }
}

# =============================================================================
# INGRESS (Application Load Balancer)
# =============================================================================
resource "kubernetes_ingress_v1" "applications" {
  count = var.enable_load_balancer ? 1 : 0

  metadata {
    name      = "authorizer-ingress"
    namespace = local.namespace

    annotations = {
      "kubernetes.io/ingress.class"                    = "alb"
      "alb.ingress.kubernetes.io/scheme"              = "internet-facing"
      "alb.ingress.kubernetes.io/target-type"         = "ip"
      "alb.ingress.kubernetes.io/load-balancer-name"  = "${var.project_name}-${var.environment}-alb"
      "alb.ingress.kubernetes.io/healthcheck-path"    = "/actuator/health"
      "alb.ingress.kubernetes.io/listen-ports"        = jsonencode([{HTTP = 80}, {HTTPS = 443}])
      "alb.ingress.kubernetes.io/certificate-arn"     = var.ssl_certificate_arn != null ? var.ssl_certificate_arn : ""
      "alb.ingress.kubernetes.io/ssl-redirect"        = var.ssl_certificate_arn != null ? "443" : ""
    }

    labels = {
      app         = "authorizer"
      environment = var.environment
      project     = var.project_name
    }
  }

  spec {
    dynamic "rule" {
      for_each = var.applications
      content {
        host = "${rule.value.name}.${var.domain_name != null ? var.domain_name : "example.com"}"

        http {
          path {
            path      = "/"
            path_type = "Prefix"

            backend {
              service {
                name = "${rule.value.name}-service"
                port {
                  number = rule.value.port
                }
              }
            }
          }
        }
      }
    }
  }

  depends_on = [kubernetes_service.applications]
}

# =============================================================================
# HORIZONTAL POD AUTOSCALER
# =============================================================================
resource "kubernetes_horizontal_pod_autoscaler_v2" "applications" {
  for_each = var.enable_hpa ? var.applications : {}

  metadata {
    name      = "${each.value.name}-hpa"
    namespace = local.namespace
  }

  spec {
    scale_target_ref {
      api_version = "apps/v1"
      kind        = "Deployment"
      name        = each.value.name
    }

    min_replicas = each.value.replicas
    max_replicas = each.value.replicas * 3

    metric {
      type = "Resource"
      resource {
        name = "cpu"
        target {
          type                = "Utilization"
          average_utilization = 70
        }
      }
    }

    metric {
      type = "Resource"
      resource {
        name = "memory"
        target {
          type                = "Utilization"
          average_utilization = 80
        }
      }
    }
  }

  depends_on = [kubernetes_deployment.applications]
}

resource "aws_ecs_cluster" "this" {
  name = "${var.name_prefix}-cluster"
  setting {
    name  = "containerInsights"
    value = var.container_insights ? "enabled" : "disabled"
  }
}

resource "aws_service_discovery_http_namespace" "this" {
  name        = "${var.name_prefix}.local"
  description = "Service Connect namespace for ${var.name_prefix}"
}

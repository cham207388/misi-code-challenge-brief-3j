output "ecr_repository_urls" {
  value = { for name, repo in aws_ecr_repository.this : name => repo.repository_url }
}

output "gha_role_arns" {
  value = { for key, role in aws_iam_role.gha : key => role.arn }
}

output "oidc_provider_arn" {
  value = local.oidc_provider_arn
}

output "ecs_permissions_boundary_arn" {
  value = aws_iam_policy.ecs_permissions_boundary.arn
}

output "deploy_permissions_boundary_arn" {
  value = aws_iam_policy.deploy_boundary.arn
}

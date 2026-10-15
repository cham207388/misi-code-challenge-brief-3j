output "ecr_repository_urls" {
  value = { for name, repo in aws_ecr_repository.this : name => repo.repository_url }
}

output "gha_role_arns" {
  value = { for key, role in aws_iam_role.gha : key => role.arn }
}

output "oidc_provider_arn" {
  value = local.oidc_provider_arn
}

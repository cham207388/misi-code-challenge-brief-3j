output "secret_arns" {
  value = [for secret in aws_secretsmanager_secret.this : secret.arn]
}

output "secret_arn_map" {
  value = { for key, secret in aws_secretsmanager_secret.this : key => secret.arn }
}

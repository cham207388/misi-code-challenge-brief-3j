locals {
  secret_names = [
    "hmac-webhook",
    "permit-signing-key",
    "registry-api-key",
    "calendar-client",
    "sftp",
    "demo-passwords",
  ]
}

resource "random_password" "this" {
  for_each = toset(local.secret_names)
  length   = 32
  special  = false
}

resource "aws_secretsmanager_secret" "this" {
  for_each = toset(local.secret_names)
  name     = "brief3/${var.environment}/app/${each.value}"
}

resource "aws_secretsmanager_secret_version" "this" {
  for_each      = aws_secretsmanager_secret.this
  secret_id     = each.value.id
  secret_string = random_password.this[each.key].result
}

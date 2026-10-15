output "hostname" {
  value = var.hostname
}

output "app_url" {
  value = "https://${var.hostname}"
}

output "state_key_hint" {
  value = "s3://misi-va-code-challenges/env:/${terraform.workspace}/brief-3/terraform.tfstate"
}

output "rds_endpoint" {
  value = module.rds.endpoint
}

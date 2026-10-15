check "allowed_workspace" {
  assert {
    condition     = contains(["dev", "prod"], terraform.workspace)
    error_message = "Terraform workspace must be 'dev' or 'prod' (current: ${terraform.workspace}). The default workspace is blocked so state is not written to brief-3/terraform.tfstate."
  }
}

locals {
  name_prefix = "brief3-${terraform.workspace}"
}

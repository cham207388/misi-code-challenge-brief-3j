variable "github_repository" {
  type        = string
  description = "GitHub org/repo used in OIDC subject claims."
  default     = "cham207388/misi-code-challenge-brief-3j"
}

variable "create_oidc_provider" {
  type        = bool
  description = "Create the GitHub OIDC provider. Set false to reuse the account provider."
  default     = false
}

variable "ecr_keep_untagged" {
  type    = number
  default = 10
}

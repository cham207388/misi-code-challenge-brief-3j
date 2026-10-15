provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = "brief3"
      Environment = terraform.workspace
      ManagedBy   = "terraform"
      Repo        = var.github_repository
    }
  }
}

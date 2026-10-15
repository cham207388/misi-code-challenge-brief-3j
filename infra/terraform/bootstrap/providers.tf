provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project     = "brief3"
      Environment = "shared"
      ManagedBy   = "terraform"
      Repo        = var.github_repository
    }
  }
}

locals {
  repositories = [
    "brief3/app",
    "brief3/mock-applicant-registry",
    "brief3/mock-regional-calendar",
    "brief3/mock-notification-gateway",
    "brief3/mock-weather-advisory",
    "brief3/mock-geocoding",
    "brief3/mock-partner-feed",
  ]
}

resource "aws_ecr_repository" "this" {
  for_each             = toset(local.repositories)
  name                 = each.value
  image_tag_mutability = "IMMUTABLE"
  force_delete         = false

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "this" {
  for_each   = aws_ecr_repository.this
  repository = each.value.name
  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Expire untagged images"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 14
        }
        action = { type = "expire" }
      }
    ]
  })
}

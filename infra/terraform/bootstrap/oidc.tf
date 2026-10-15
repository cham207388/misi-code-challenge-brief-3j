data "aws_iam_openid_connect_provider" "github" {
  count = var.create_oidc_provider ? 0 : 1
  url   = "https://token.actions.githubusercontent.com"
}

resource "aws_iam_openid_connect_provider" "github" {
  count           = var.create_oidc_provider ? 1 : 0
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
}

locals {
  oidc_provider_arn = var.create_oidc_provider ? aws_iam_openid_connect_provider.github[0].arn : data.aws_iam_openid_connect_provider.github[0].arn
  roles = {
    plan = {
      name = "brief3-gha-plan"
      sub  = "repo:${var.github_repository}:pull_request"
    }
    ecr_push = {
      name = "brief3-gha-ecr-push"
      sub  = "repo:${var.github_repository}:ref:refs/heads/main"
    }
    deploy_dev = {
      name = "brief3-gha-deploy-dev"
      sub  = "repo:${var.github_repository}:environment:dev"
    }
    deploy_prod = {
      name = "brief3-gha-deploy-prod"
      sub  = "repo:${var.github_repository}:environment:production"
    }
  }
}

data "aws_iam_policy_document" "trust" {
  for_each = local.roles
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    principals {
      type        = "Federated"
      identifiers = [local.oidc_provider_arn]
    }
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values   = [each.value.sub]
    }
  }
}

resource "aws_iam_role" "gha" {
  for_each           = local.roles
  name               = each.value.name
  assume_role_policy = data.aws_iam_policy_document.trust[each.key].json
}

data "aws_iam_policy_document" "state" {
  statement {
    sid = "StatePrefix"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
      "s3:ListBucket",
    ]
    resources = [
      "arn:aws:s3:::misi-va-code-challenges",
      "arn:aws:s3:::misi-va-code-challenges/brief-3/*",
      "arn:aws:s3:::misi-va-code-challenges/env/*/brief-3/*",
    ]
  }
}

resource "aws_iam_role_policy" "state" {
  for_each = aws_iam_role.gha
  name     = "brief3-state"
  role     = each.value.id
  policy   = data.aws_iam_policy_document.state.json
}

data "aws_iam_policy_document" "plan_readonly" {
  statement {
    sid       = "ReadOnlyPlan"
    actions   = ["ec2:Describe*", "ecs:Describe*", "ecs:List*", "elasticloadbalancing:Describe*", "rds:Describe*", "s3:Get*", "s3:List*", "acm:Describe*", "acm:List*", "route53:Get*", "route53:List*", "iam:GetRole", "iam:ListRolePolicies", "iam:GetRolePolicy", "secretsmanager:DescribeSecret", "secretsmanager:ListSecrets", "logs:Describe*", "ecr:Describe*"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "plan" {
  name   = "brief3-plan-readonly"
  role   = aws_iam_role.gha["plan"].id
  policy = data.aws_iam_policy_document.plan_readonly.json
}

data "aws_iam_policy_document" "ecr_push" {
  statement {
    actions = [
      "ecr:GetAuthorizationToken",
      "ecr:BatchCheckLayerAvailability",
      "ecr:CompleteLayerUpload",
      "ecr:UploadLayerPart",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:BatchGetImage",
      "ecr:DescribeRepositories",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "ecr_push" {
  name   = "brief3-ecr-push"
  role   = aws_iam_role.gha["ecr_push"].id
  policy = data.aws_iam_policy_document.ecr_push.json
}

data "aws_iam_policy_document" "deploy" {
  statement {
    actions = [
      "ecs:*",
      "ec2:*",
      "elasticloadbalancing:*",
      "rds:*",
      "s3:*",
      "iam:PassRole",
      "iam:GetRole",
      "logs:*",
      "secretsmanager:*",
      "ssm:GetParameter*",
      "application-autoscaling:*",
      "servicediscovery:*",
      "elasticfilesystem:*",
      "wafv2:*",
      "acm:*",
      "route53:*",
      "sns:*",
      "cloudwatch:*",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "deploy_dev" {
  name   = "brief3-deploy"
  role   = aws_iam_role.gha["deploy_dev"].id
  policy = data.aws_iam_policy_document.deploy.json
}

resource "aws_iam_role_policy" "deploy_prod" {
  name   = "brief3-deploy"
  role   = aws_iam_role.gha["deploy_prod"].id
  policy = data.aws_iam_policy_document.deploy.json
}

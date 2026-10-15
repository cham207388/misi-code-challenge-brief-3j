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

data "aws_caller_identity" "current" {}

data "aws_route53_zone" "public" {
  name         = var.hosted_zone_name
  private_zone = false
}

locals {
  oidc_provider_arn = var.create_oidc_provider ? aws_iam_openid_connect_provider.github[0].arn : data.aws_iam_openid_connect_provider.github[0].arn
  account_id        = data.aws_caller_identity.current.account_id
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
  for_each             = local.roles
  name                 = each.value.name
  assume_role_policy   = data.aws_iam_policy_document.trust[each.key].json
  permissions_boundary = aws_iam_policy.deploy_boundary.arn
}

data "aws_iam_policy_document" "deploy_boundary" {
  statement {
    sid       = "AllowTaggedBrief3"
    effect    = "Allow"
    actions   = ["*"]
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "aws:ResourceTag/Project"
      values   = ["brief3"]
    }
  }
  statement {
    sid     = "AllowBrief3Names"
    effect  = "Allow"
    actions = ["*"]
    resources = [
      "arn:aws:iam::${local.account_id}:role/brief3-*",
      "arn:aws:iam::${local.account_id}:policy/brief3-*",
      "arn:aws:ecs:us-east-1:${local.account_id}:*/*brief3-*",
      "arn:aws:elasticloadbalancing:us-east-1:${local.account_id}:*/*brief3-*",
      "arn:aws:logs:us-east-1:${local.account_id}:log-group:/brief3/*",
      "arn:aws:logs:us-east-1:${local.account_id}:log-group:/brief3/*:*",
      "arn:aws:s3:::brief3-*",
      "arn:aws:s3:::brief3-*/*",
      "arn:aws:s3:::misi-va-code-challenges",
      "arn:aws:s3:::misi-va-code-challenges/*",
      "arn:aws:rds:us-east-1:${local.account_id}:*:*brief3-*",
      "arn:aws:ecr:us-east-1:${local.account_id}:repository/brief3/*",
      "arn:aws:secretsmanager:us-east-1:${local.account_id}:secret:brief3/*",
      "arn:aws:elasticfilesystem:us-east-1:${local.account_id}:file-system/*",
      "arn:aws:sns:us-east-1:${local.account_id}:brief3-*",
      "arn:aws:wafv2:us-east-1:${local.account_id}:*/webacl/brief3-*",
      "arn:aws:servicediscovery:us-east-1:${local.account_id}:*/*",
      "arn:aws:kms:us-east-1:${local.account_id}:key/*",
      "arn:aws:acm:us-east-1:${local.account_id}:certificate/*",
      "arn:aws:route53:::hostedzone/${data.aws_route53_zone.public.zone_id}",
      "arn:aws:route53:::change/*",
    ]
  }
  statement {
    sid       = "AllowDescribe"
    effect    = "Allow"
    actions   = ["*:Describe*", "*:Get*", "*:List*"]
    resources = ["*"]
  }
  statement {
    sid       = "DenyIamEscalation"
    effect    = "Deny"
    actions   = ["iam:CreateUser", "iam:CreateAccessKey", "iam:AttachUserPolicy", "iam:PutUserPolicy"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "deploy_boundary" {
  name   = "brief3-gha-permissions-boundary"
  policy = data.aws_iam_policy_document.deploy_boundary.json
}

data "aws_iam_policy_document" "ecs_boundary" {
  statement {
    sid    = "TaskRuntime"
    effect = "Allow"
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
      "s3:ListBucket",
      "secretsmanager:GetSecretValue",
      "kms:Decrypt",
      "ssmmessages:CreateControlChannel",
      "ssmmessages:CreateDataChannel",
      "ssmmessages:OpenControlChannel",
      "ssmmessages:OpenDataChannel",
    ]
    resources = ["*"]
  }
  statement {
    sid       = "DenyPrivilegeEscalation"
    effect    = "Deny"
    actions   = ["iam:*", "organizations:*", "account:*", "ec2:AuthorizeSecurityGroupIngress"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "ecs_permissions_boundary" {
  name   = "brief3-ecs-permissions-boundary"
  policy = data.aws_iam_policy_document.ecs_boundary.json
}

data "aws_iam_policy_document" "state" {
  statement {
    sid       = "ListStateBucket"
    actions   = ["s3:ListBucket"]
    resources = ["arn:aws:s3:::misi-va-code-challenges"]
    condition {
      test     = "StringLike"
      variable = "s3:prefix"
      values = [
        "brief-3",
        "brief-3/*",
        "env:/",
        "env:/*",
        "env:/*/brief-3",
        "env:/*/brief-3/*",
      ]
    }
  }
  statement {
    sid = "StateObjects"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
    ]
    resources = [
      "arn:aws:s3:::misi-va-code-challenges/brief-3/*",
      "arn:aws:s3:::misi-va-code-challenges/env:/*/brief-3/*",
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
    sid = "ReadOnlyPlan"
    actions = [
      "ec2:Describe*",
      "ecs:Describe*",
      "ecs:List*",
      "elasticloadbalancing:Describe*",
      "rds:Describe*",
      "s3:Get*",
      "s3:List*",
      "acm:Describe*",
      "acm:List*",
      "route53:Get*",
      "route53:List*",
      "iam:GetRole",
      "iam:GetRolePolicy",
      "iam:GetPolicy",
      "iam:GetPolicyVersion",
      "iam:ListRolePolicies",
      "iam:ListAttachedRolePolicies",
      "iam:ListInstanceProfilesForRole",
      "iam:GetInstanceProfile",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecrets",
      "logs:Describe*",
      "ecr:Describe*",
      "wafv2:Get*",
      "wafv2:List*",
      "wafv2:Describe*",
      "elasticfilesystem:Describe*",
      "elasticfilesystem:ListTagsForResource",
      "sns:Get*",
      "sns:List*",
      "cloudwatch:Describe*",
      "cloudwatch:Get*",
      "cloudwatch:List*",
      "servicediscovery:Get*",
      "servicediscovery:List*",
      "application-autoscaling:Describe*",
      "kms:Describe*",
      "kms:Get*",
      "kms:List*",
    ]
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
    sid       = "AuthToken"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }
  statement {
    sid = "PushBrief3"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:CompleteLayerUpload",
      "ecr:UploadLayerPart",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:BatchGetImage",
      "ecr:DescribeRepositories",
      "ecr:DescribeImages",
    ]
    resources = [for name in local.repositories : "arn:aws:ecr:us-east-1:${local.account_id}:repository/${name}"]
  }
}

resource "aws_iam_role_policy" "ecr_push" {
  name   = "brief3-ecr-push"
  role   = aws_iam_role.gha["ecr_push"].id
  policy = data.aws_iam_policy_document.ecr_push.json
}

locals {
  deploy_envs = {
    deploy_dev  = "dev"
    deploy_prod = "prod"
  }
}

data "aws_iam_policy_document" "deploy" {
  for_each = local.deploy_envs

  statement {
    sid = "DescribeAll"
    actions = [
      "ec2:Describe*",
      "ecs:Describe*",
      "ecs:List*",
      "elasticloadbalancing:Describe*",
      "rds:Describe*",
      "s3:Get*",
      "s3:List*",
      "acm:Describe*",
      "acm:List*",
      "logs:Describe*",
      "logs:ListTagsForResource",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecrets",
      "ssm:GetParameter*",
      "ecr:Describe*",
      "ecr:GetAuthorizationToken",
      "wafv2:Get*",
      "wafv2:List*",
      "wafv2:Describe*",
      "elasticfilesystem:Describe*",
      "sns:Get*",
      "sns:List*",
      "cloudwatch:Describe*",
      "cloudwatch:Get*",
      "cloudwatch:List*",
      "servicediscovery:Get*",
      "servicediscovery:List*",
      "application-autoscaling:Describe*",
      "kms:Describe*",
      "kms:Get*",
      "kms:List*",
      "iam:GetRole",
      "iam:GetRolePolicy",
      "iam:GetPolicy",
      "iam:GetPolicyVersion",
      "iam:ListRolePolicies",
      "iam:ListAttachedRolePolicies",
      "iam:ListInstanceProfilesForRole",
    ]
    resources = ["*"]
  }

  statement {
    sid = "MutateTagged"
    actions = [
      "ec2:*",
      "ecs:*",
      "elasticloadbalancing:*",
      "rds:*",
      "s3:*",
      "logs:*",
      "secretsmanager:*",
      "ssm:*",
      "application-autoscaling:*",
      "servicediscovery:*",
      "elasticfilesystem:*",
      "wafv2:*",
      "acm:*",
      "sns:*",
      "cloudwatch:*",
      "kms:*",
    ]
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "aws:ResourceTag/Project"
      values   = ["brief3"]
    }
    condition {
      test     = "StringEquals"
      variable = "aws:ResourceTag/Environment"
      values   = [each.value]
    }
  }

  statement {
    sid = "CreateTagged"
    actions = [
      "ec2:Create*",
      "ecs:Create*",
      "ecs:RegisterTaskDefinition",
      "elasticloadbalancing:Create*",
      "rds:Create*",
      "s3:CreateBucket",
      "logs:CreateLogGroup",
      "secretsmanager:CreateSecret",
      "servicediscovery:Create*",
      "elasticfilesystem:Create*",
      "wafv2:Create*",
      "sns:CreateTopic",
      "cloudwatch:PutMetricAlarm",
      "cloudwatch:PutDashboard",
      "application-autoscaling:RegisterScalableTarget",
      "application-autoscaling:PutScalingPolicy",
    ]
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "aws:RequestTag/Project"
      values   = ["brief3"]
    }
    condition {
      test     = "StringEquals"
      variable = "aws:RequestTag/Environment"
      values   = [each.value]
    }
  }

  statement {
    sid = "NamedResources"
    actions = [
      "ecs:*",
      "elasticloadbalancing:*",
      "rds:*",
      "s3:*",
      "logs:*",
      "secretsmanager:*",
      "elasticfilesystem:*",
      "sns:*",
      "wafv2:*",
      "servicediscovery:*",
      "cloudwatch:*",
    ]
    resources = [
      "arn:aws:ecs:us-east-1:${local.account_id}:cluster/brief3-${each.value}-*",
      "arn:aws:ecs:us-east-1:${local.account_id}:service/brief3-${each.value}-*/*",
      "arn:aws:ecs:us-east-1:${local.account_id}:task-definition/brief3-${each.value}-*:*",
      "arn:aws:elasticloadbalancing:us-east-1:${local.account_id}:loadbalancer/app/brief3-${each.value}-*/*",
      "arn:aws:elasticloadbalancing:us-east-1:${local.account_id}:targetgroup/brief3-${each.value}-*/*",
      "arn:aws:elasticloadbalancing:us-east-1:${local.account_id}:listener/app/brief3-${each.value}-*/*",
      "arn:aws:rds:us-east-1:${local.account_id}:db:brief3-${each.value}-*",
      "arn:aws:rds:us-east-1:${local.account_id}:subgrp:brief3-${each.value}-*",
      "arn:aws:s3:::brief3-${each.value}-*",
      "arn:aws:s3:::brief3-${each.value}-*/*",
      "arn:aws:logs:us-east-1:${local.account_id}:log-group:/brief3/${each.value}/*",
      "arn:aws:logs:us-east-1:${local.account_id}:log-group:/brief3/${each.value}/*:*",
      "arn:aws:secretsmanager:us-east-1:${local.account_id}:secret:brief3/${each.value}/*",
      "arn:aws:sns:us-east-1:${local.account_id}:brief3-${each.value}-*",
      "arn:aws:wafv2:us-east-1:${local.account_id}:*/webacl/brief3-${each.value}-*",
    ]
  }

  statement {
    sid       = "PassEcsRoles"
    actions   = ["iam:PassRole"]
    resources = ["arn:aws:iam::${local.account_id}:role/brief3-${each.value}-*"]
    condition {
      test     = "StringEquals"
      variable = "iam:PassedToService"
      values   = ["ecs-tasks.amazonaws.com"]
    }
  }

  statement {
    sid = "ManageEcsRoles"
    actions = [
      "iam:CreateRole",
      "iam:DeleteRole",
      "iam:PutRolePolicy",
      "iam:DeleteRolePolicy",
      "iam:AttachRolePolicy",
      "iam:DetachRolePolicy",
      "iam:TagRole",
      "iam:UntagRole",
      "iam:UpdateAssumeRolePolicy",
      "iam:UpdateRole",
      "iam:GetRole",
      "iam:GetRolePolicy",
      "iam:ListRolePolicies",
      "iam:ListAttachedRolePolicies",
      "iam:ListInstanceProfilesForRole",
    ]
    resources = ["arn:aws:iam::${local.account_id}:role/brief3-${each.value}-*"]
    condition {
      test     = "StringEquals"
      variable = "iam:PermissionsBoundary"
      values   = [aws_iam_policy.ecs_permissions_boundary.arn]
    }
  }

  statement {
    sid = "ReadEcsRolesWithoutBoundary"
    actions = [
      "iam:GetRole",
      "iam:GetRolePolicy",
      "iam:ListRolePolicies",
      "iam:ListAttachedRolePolicies",
      "iam:ListInstanceProfilesForRole",
    ]
    resources = ["arn:aws:iam::${local.account_id}:role/brief3-${each.value}-*"]
  }

  statement {
    sid     = "AttachManagedEcsExecution"
    actions = ["iam:AttachRolePolicy", "iam:DetachRolePolicy"]
    resources = [
      "arn:aws:iam::${local.account_id}:role/brief3-${each.value}-*",
      "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy",
    ]
  }

  statement {
    sid = "Route53Zone"
    actions = [
      "route53:GetHostedZone",
      "route53:ListResourceRecordSets",
      "route53:ChangeResourceRecordSets",
      "route53:GetChange",
      "route53:ListTagsForResource",
    ]
    resources = [
      "arn:aws:route53:::hostedzone/${data.aws_route53_zone.public.zone_id}",
      "arn:aws:route53:::change/*",
    ]
  }

  statement {
    sid       = "Route53List"
    actions   = ["route53:ListHostedZones", "route53:ListHostedZonesByName"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "deploy" {
  for_each = local.deploy_envs
  name     = "brief3-deploy-${each.value}"
  role     = aws_iam_role.gha[each.key].id
  policy   = data.aws_iam_policy_document.deploy[each.key].json
}

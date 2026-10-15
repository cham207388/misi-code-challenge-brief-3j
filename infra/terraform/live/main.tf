data "aws_caller_identity" "current" {}

locals {
  app_image = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com/brief3/app:${var.image_tag}"
}

module "network" {
  source      = "../modules/network"
  name_prefix = local.name_prefix
  cidr_block  = var.vpc_cidr
  region      = var.region
  nat_per_az  = var.enable_nat_per_az
}

module "dns_cert" {
  source    = "../modules/dns-cert"
  zone_name = var.hosted_zone_name
  hostname  = var.hostname
}

module "alb" {
  source            = "../modules/alb"
  name_prefix       = local.name_prefix
  vpc_id            = module.network.vpc_id
  subnet_ids        = module.network.public_subnet_ids
  security_group_id = module.network.alb_security_group_id
  certificate_arn   = module.dns_cert.certificate_arn
  hostname          = var.hostname
  zone_id           = module.dns_cert.zone_id
}

module "ecs_cluster" {
  source             = "../modules/ecs-cluster"
  name_prefix        = local.name_prefix
  container_insights = terraform.workspace == "prod"
}

module "rds" {
  source              = "../modules/rds"
  name_prefix         = local.name_prefix
  subnet_ids          = module.network.private_subnet_ids
  security_group_id   = module.network.db_security_group_id
  instance_class      = var.db_instance_class
  multi_az            = var.db_multi_az
  backup_retention    = var.db_backup_retention
  deletion_protection = var.deletion_protection
}

module "s3_storage" {
  source      = "../modules/s3-storage"
  name_prefix = local.name_prefix
  hostname    = var.hostname
}

module "secrets" {
  source      = "../modules/secrets"
  environment = terraform.workspace
}

module "efs" {
  source                    = "../modules/efs"
  name_prefix               = local.name_prefix
  vpc_id                    = module.network.vpc_id
  subnet_ids                = module.network.private_subnet_ids
  client_security_group_ids = [module.network.app_security_group_id]
}

module "app" {
  source             = "../modules/ecs-service"
  name_prefix        = local.name_prefix
  environment        = terraform.workspace
  service_name       = "app"
  cluster_id         = module.ecs_cluster.cluster_id
  namespace_arn      = module.ecs_cluster.namespace_arn
  image              = local.app_image
  cpu                = var.app_cpu
  memory             = var.app_memory
  desired_count      = var.app_desired_count
  subnet_ids         = module.network.private_subnet_ids
  security_group_ids = [module.network.app_security_group_id]
  target_group_arn   = module.alb.target_group_arn
  region             = var.region
  log_retention_days = var.log_retention_days
  s3_bucket_arns     = module.s3_storage.bucket_arns
  secret_arns        = concat(module.secrets.secret_arns, [module.rds.secret_arn])
  environment_variables = {
    SPRING_PROFILES_ACTIVE     = "aws,${terraform.workspace}"
    SPRING_DATASOURCE_URL      = "jdbc:postgresql://${module.rds.endpoint}:5432/memorialops"
    SPRING_DATASOURCE_USERNAME = "memorial"
    SCENARIO_CONTROL_ENABLED   = tostring(var.scenario_control_enabled)
    SEED_ON_DEPLOY             = tostring(var.seed_on_deploy)
  }
  secrets = {
    SPRING_DATASOURCE_PASSWORD = "${module.rds.secret_arn}:password::"
    NOTIFICATION_HMAC_SECRET   = module.secrets.secret_arn_map["hmac-webhook"]
    REGISTRY_API_KEY           = module.secrets.secret_arn_map["registry-api-key"]
  }
}

module "observability" {
  source         = "../modules/observability"
  name_prefix    = local.name_prefix
  region         = var.region
  alb_arn_suffix = module.alb.arn_suffix
  alarm_email    = var.alarm_email
}

module "waf" {
  source      = "../modules/waf"
  name_prefix = local.name_prefix
  enabled     = var.enable_waf
  alb_arn     = module.alb.arn
}

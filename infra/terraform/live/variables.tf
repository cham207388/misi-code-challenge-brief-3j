variable "region" {
  type    = string
  default = "us-east-1"
}

variable "github_repository" {
  type    = string
  default = "cham207388/misi-code-challenge-brief-3j"
}

variable "hostname" {
  type        = string
  description = "Public hostname. Assumption: brief3-dev.wsllclabs.com / brief3.wsllclabs.com."
}

variable "hosted_zone_name" {
  type    = string
  default = "wsllclabs.com"
}

variable "vpc_cidr" {
  type = string
}

variable "image_tag" {
  type        = string
  description = "Immutable image tag (git SHA) for the app service."
  default     = "latest"
}

variable "app_cpu" {
  type    = number
  default = 1024
}

variable "app_memory" {
  type    = number
  default = 2048
}

variable "app_desired_count" {
  type    = number
  default = 1
}

variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "db_multi_az" {
  type    = bool
  default = false
}

variable "db_backup_retention" {
  type    = number
  default = 1
}

variable "deletion_protection" {
  type    = bool
  default = false
}

variable "enable_waf" {
  type    = bool
  default = false
}

variable "enable_nat_per_az" {
  type    = bool
  default = false
}

variable "log_retention_days" {
  type    = number
  default = 14
}

variable "scenario_control_enabled" {
  type    = bool
  default = true
}

variable "seed_on_deploy" {
  type    = bool
  default = true
}

variable "alarm_email" {
  type    = string
  default = ""
}

variable "alb_access_logs" {
  type    = bool
  default = false
}

variable "name_prefix" { type = string }
variable "environment" { type = string }
variable "service_name" { type = string }
variable "cluster_id" { type = string }
variable "namespace_arn" { type = string }
variable "image" { type = string }
variable "cpu" { type = number }
variable "memory" { type = number }
variable "desired_count" { type = number }
variable "container_port" {
  type    = number
  default = 8080
}
variable "subnet_ids" { type = list(string) }
variable "security_group_ids" { type = list(string) }
variable "target_group_arn" {
  type    = string
  default = ""
}
variable "region" { type = string }
variable "log_retention_days" { type = number }
variable "environment_variables" {
  type    = map(string)
  default = {}
}
variable "secrets" {
  type    = map(string)
  default = {}
}
variable "secret_arns" {
  type    = list(string)
  default = []
}
variable "s3_bucket_arns" {
  type    = list(string)
  default = []
}
variable "use_spot" {
  type    = bool
  default = false
}

variable "permissions_boundary_arn" {
  type    = string
  default = ""
}

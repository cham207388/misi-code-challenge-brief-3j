variable "name_prefix" { type = string }
variable "subnet_ids" { type = list(string) }
variable "security_group_id" { type = string }
variable "instance_class" { type = string }
variable "allocated_storage" {
  type    = number
  default = 20
}
variable "multi_az" { type = bool }
variable "backup_retention" { type = number }
variable "deletion_protection" { type = bool }

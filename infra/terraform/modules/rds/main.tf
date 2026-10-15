resource "aws_db_subnet_group" "this" {
  name       = "${var.name_prefix}-db"
  subnet_ids = var.subnet_ids
}

resource "aws_db_instance" "this" {
  identifier                   = "${var.name_prefix}-pg"
  engine                       = "postgres"
  engine_version               = "17"
  instance_class               = var.instance_class
  allocated_storage            = var.allocated_storage
  storage_type                 = "gp3"
  db_name                      = "memorialops"
  username                     = "memorial"
  manage_master_user_password  = true
  db_subnet_group_name         = aws_db_subnet_group.this.name
  vpc_security_group_ids       = [var.security_group_id]
  multi_az                     = var.multi_az
  publicly_accessible          = false
  storage_encrypted            = true
  backup_retention_period      = var.backup_retention
  deletion_protection          = var.deletion_protection
  skip_final_snapshot          = !var.deletion_protection
  final_snapshot_identifier    = var.deletion_protection ? "${var.name_prefix}-final" : null
  performance_insights_enabled = var.multi_az
  auto_minor_version_upgrade   = true
  copy_tags_to_snapshot        = true
}

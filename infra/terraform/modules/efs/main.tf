resource "aws_efs_file_system" "this" {
  encrypted = true
  tags      = { Name = "${var.name_prefix}-sftp" }
}

resource "aws_efs_access_point" "this" {
  file_system_id = aws_efs_file_system.this.id
  posix_user {
    gid = 100
    uid = 1001
  }
  root_directory {
    path = "/upload"
    creation_info {
      owner_gid   = 100
      owner_uid   = 1001
      permissions = "0755"
    }
  }
}

resource "aws_security_group" "efs" {
  name   = "${var.name_prefix}-efs"
  vpc_id = var.vpc_id
  ingress {
    from_port       = 2049
    to_port         = 2049
    protocol        = "tcp"
    security_groups = var.client_security_group_ids
  }
}

resource "aws_efs_mount_target" "this" {
  count           = length(var.subnet_ids)
  file_system_id  = aws_efs_file_system.this.id
  subnet_id       = var.subnet_ids[count.index]
  security_groups = [aws_security_group.efs.id]
}

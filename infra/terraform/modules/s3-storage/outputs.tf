output "bucket_arns" { value = [for bucket in aws_s3_bucket.this : bucket.arn] }
output "attachments_bucket" { value = aws_s3_bucket.this["attachments"].id }
output "permits_bucket" { value = aws_s3_bucket.this["permits"].id }

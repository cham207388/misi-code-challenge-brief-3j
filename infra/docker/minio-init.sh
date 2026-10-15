#!/bin/sh
set -eu
mc alias set local http://minio:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"
mc mb --ignore-existing local/attachments
mc mb --ignore-existing local/permits
mc anonymous set none local/attachments
mc anonymous set none local/permits
mc admin user add local "$MINIO_APP_USER" "$MINIO_APP_PASSWORD" || true
cat >/tmp/policy.json <<'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:ListBucket"],
      "Resource": ["arn:aws:s3:::attachments", "arn:aws:s3:::attachments/*", "arn:aws:s3:::permits", "arn:aws:s3:::permits/*"]
    }
  ]
}
EOF
mc admin policy create local memorial-app /tmp/policy.json || true
mc admin policy attach local memorial-app --user "$MINIO_APP_USER" || true
echo "minio buckets ready"

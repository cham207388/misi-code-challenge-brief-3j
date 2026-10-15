#!/bin/sh
# Creates private attachment and permit buckets on the local S3-compatible store.
# Official minio/minio and minio/mc images are no longer anonymously pullable, so
# the Compose stack uses rustfs/rustfs plus the AWS CLI.
set -eu
ENDPOINT="${S3_ENDPOINT:-http://minio:9000}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-${MINIO_ROOT_USER:-minio}}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-${MINIO_ROOT_PASSWORD:-minio123456}}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-us-east-1}"

for bucket in attachments permits; do
  if aws --endpoint-url "$ENDPOINT" s3api head-bucket --bucket "$bucket" >/dev/null 2>&1; then
    echo "bucket ${bucket} already exists"
  else
    aws --endpoint-url "$ENDPOINT" s3 mb "s3://${bucket}"
  fi
done
echo "object-storage buckets ready"

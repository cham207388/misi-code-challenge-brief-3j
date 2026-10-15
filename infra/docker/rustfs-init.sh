#!/bin/sh
# Creates private attachment and permit buckets on the local RustFS store.
set -eu
ENDPOINT="${S3_ENDPOINT:-http://rustfs:9000}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:?RUSTFS access key required}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:?RUSTFS secret key required}"
export AWS_DEFAULT_REGION="${AWS_DEFAULT_REGION:-us-east-1}"

for bucket in attachments permits; do
  if aws --endpoint-url "$ENDPOINT" s3api head-bucket --bucket "$bucket" >/dev/null 2>&1; then
    echo "bucket ${bucket} already exists"
  else
    aws --endpoint-url "$ENDPOINT" s3 mb "s3://${bucket}"
  fi
done
echo "object-storage buckets ready"

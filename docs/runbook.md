# Runbook (Sprint 0)

## Local

```bash
make init
make up          # full stack
make up-core     # app + postgres + minio
make down
make logs SERVICE=app
```

## Rollback (cloud, later)

Re-apply the previous image tag:

```bash
terraform -chdir=infra/terraform/live workspace select dev
terraform -chdir=infra/terraform/live apply -var-file=env/dev.tfvars -var image_tag=<previous-sha>
```

## Terraform

Do not apply from a laptop unless asked. Validate with `make tf-validate`. Plans require AWS credentials and an explicit workspace `dev` or `prod`.

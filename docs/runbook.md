# Runbook (Sprint 0)

## Local

```bash
make init
make up          # full stack
make up-core     # app + postgres + object storage
make down
make logs SERVICE=app
```

Local object storage is `rustfs/rustfs` (S3 API on :9000). If containers can resolve each other but TCP times out, Docker bridge ICC is being filtered (`net.bridge.bridge-nf-call-iptables=1`). Published ports still work; `sudo sysctl -w net.bridge.bridge-nf-call-iptables=0` restores container-to-container traffic on that host.

## Rollback (cloud, later)

Re-apply the previous image tag:

```bash
terraform -chdir=infra/terraform/live workspace select dev
terraform -chdir=infra/terraform/live apply -var-file=env/dev.tfvars -var image_tag=<previous-sha>
```

## Terraform

Do not apply from a laptop unless asked. Validate with `make tf-validate`. Plans require AWS credentials and an explicit workspace `dev` or `prod`.

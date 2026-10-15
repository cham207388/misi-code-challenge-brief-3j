# ADR-011: Terraform live root with workspaces

`infra/terraform/live` uses the shared bucket `misi-va-code-challenges`, key `brief-3/terraform.tfstate`, `use_lockfile = true`. Workspaces `dev` and `prod` store state at `env:/dev|prod/brief-3/terraform.tfstate`. The `default` workspace is blocked. Bootstrap is a separate root at `brief-3/bootstrap/terraform.tfstate`.

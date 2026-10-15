# Known limitations (Sprint 0)

- Hostname assumptions (`brief3-dev.wsllclabs.com` / `brief3.wsllclabs.com`) are not applied to AWS in this sprint. Terraform is written and validated only.
- Pages 02–26 are routed shells with purpose copy. Domain CRUD, workflow, PDFs and integrations are Sprint 1+.
- Mock services expose `/__admin` and a single contract stub each. Failure-matrix UX is not wired through the app yet.
- Demo passwords are local README defaults. Cloud demo passwords will live in Secrets Manager (`brief3/<env>/app/demo-passwords`).
- Map tiles, SMS, ClamAV and WAF-on-dev are out of scope or later.
- Mocks also run in prod later because no real providers exist.
- `terraform apply` is intentionally not run by the Sprint 0 agent.
- Local object storage uses `rustfs/rustfs` (S3-compatible, ports 9000/9001) because `minio/minio` and `minio/mc` are no longer anonymously pullable from Docker Hub. AWS still uses S3.

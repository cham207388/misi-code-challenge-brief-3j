# Implementation log

## 2026-10-15 — Sprint 0 foundations

Scaffolded the Memorial Event Operations walking skeleton from `docs/plan.md` and brief 3.

**Done against story IDs**

- DO-1.1 / DO-1.2 / DO-1.3: Git hygiene, `.env.example`, profiles `local` / `test` / `aws`
- DO-2.1 / DO-2.2 / DO-2.3: Gradle multi-project, version catalog, web `dist` copied into the API jar, SPA fallback
- DO-3.1 / DO-3.3: App image Dockerfile, SHA tagging contract
- DO-4.1 (core + mocks): Compose stack for Postgres, MinIO, SFTP and six mocks
- DO-5.1 / DO-5.4: `ci.yml`, `terraform-plan.yml`, `deploy.yml` (OIDC, no static keys)
- DO-6.1–6.4, DO-7.1–7.3, DO-8.1–8.3, DO-9.1: Terraform bootstrap + live modules (fmt/validate only)
- BE-1.1–1.6 (skeleton): modules, problem+json, correlation ID, health, Clock, springdoc
- BE-2.1 (initial): Flyway baseline for the planned entities
- BE-3.1 / BE-3.2 (initial): session login/logout, CSRF, `/api/me`
- UX-1.1 / UX-1.2 / UX-1.9: tokens, app shell, login
- TA-1.1 / TA-1.2: test strategy, Testcontainers smoke

**Decisions**

- Package `com.wsllclabs.memorialops`
- SPA fallback lists explicit application routes rather than a greedy `/**`
- Readiness depends on `ping` + `db` so missing mocks do not fail the app container
- Secrets module uses `random_password` + `secret_string` in Sprint 0; write-only ephemeral values can replace this once apply is reviewed

**AI assistance**

Implemented with an AI coding agent under the plan's master build prompt. No `terraform apply` and no AWS mutations were performed.

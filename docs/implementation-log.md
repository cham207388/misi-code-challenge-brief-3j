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
- Container images are multi-stage: Node 24 builds the SPA, JDK 25 produces the bootJar, and a digest-pinned JRE runs it. CI uses Node 24.21.
- Local Compose object storage is `rustfs/rustfs` (S3 API + console) after `minio/minio` returned pull-access-denied without registry login. Bucket init uses `amazon/aws-cli`.
- `StaticCacheConfig` must register `/index.html` against the `classpath:/static/` directory. Pointing the handler at the file path made Spring look for `static/index.html/index.html` and the SPA fallback returned 404.
- SPA login must echo the raw `XSRF-TOKEN` cookie. Spring's XOR CSRF request handler encodes `CsrfToken.getToken()`, and sending that value as `X-XSRF-TOKEN` returns 403.
- Hibernate `ddl-auto=validate` cannot map `citext` to the JPA `String` columns on `app_user` / `applicant`, so those columns are `varchar(255)` in the Flyway baseline. The `citext` extension remains installed for later use.

**AI assistance**

Implemented with an AI coding agent under the plan's master build prompt. No `terraform apply` and no AWS mutations were performed.

## 2026-10-15 — Sprint 1 identity, workflow, data

Branched from `main` after the Sprint 0 review-fixes merge (`4bcf0f1`).

**Done against story IDs**

- BE-2.2 / BE-2.3 / BE-2.4: append-only audit (trigger + `AuditService.append` in the aggregate transaction), `APP-YYYY-NNNNN` / `PER-YYYY-NNNNN` counters, `@Version` + ETag/`If-Match` and a 409 body with `version`, `etag` and `current`
- BE-3.1–3.4: session login throttle (5/min/user), `/api/me` capabilities, `@PreAuthorize` + `CasePolicy` (own cases, Eligible+ for scheduler, assigned attendants, redacted auditor PII), Admin cannot approve, SoD `urn:memorial-ops:separation-of-duties`
- BE-4.1–4.3: transition table, status history + audit atomicity (rollback hook test), `/api/applications/{id}/actions`
- BE-18.1 (skeleton): local/demo seeder for one complete draft
- UX-1.4–1.7: problem+json client, 403/404/422/409 UI, RHF+Zod login and wizard, URL-encoded register filters, resume route `/applications/new/:step`
- TA-2.1 / TA-3.1 / TA-3.2: parameterized transition pairs, audit trigger + rollback IT, role × endpoint matrix
- DO-5.2 / DO-5.3 / DO-10.1: unchanged from Sprint 0 (`terraform-plan.yml`, `deploy.yml` for dev, basic health/logs)

**Decisions**

- Application status values are stored as enum names (`DRAFT`, `SUBMITTED`, …) with CHECK constraints in `V003__workflow_integrity.sql`
- Unknown from/to pairs return 422; a known pair with the wrong role returns 403
- `AuditService.append` is `Propagation.MANDATORY` so it cannot commit outside the caller's transaction
- Scheduler list/detail is Eligible+; Intake list is own cases only
- No AWS apply. Terraform plan workflow remains the PR check.
- `-PskipWeb` (with or without `=true`) skips Node tasks so the JDK-only Docker build stage does not invoke `npm`. Backend ITs serve a test `index.html` when the SPA is not copied into the jar.

**Verification (2026-10-15)**

- `./gradlew :apps:api:test` green (ArchitectureTest ×3, ModularityTests, MemorialOpsIT)
- `make image` / Compose `--profile full` healthy: app readiness UP, six mocks healthy, RustFS buckets `attachments` and `permits` created
- Playwright `e2e/smoke.spec.ts` passed against http://localhost:8080 (health, coordinator login, dashboard, `/applications` reload)
- `ManagePullRequest` refused with GitHub “must be a collaborator”; branch `cursor/sprint-0-foundations-ed42` is pushed for a human to open the PR

# Memorial Event Operations & Permit Case Management

Fictional internal case-management app for cemetery event permits (VA MISI Mock Build Brief 3). This is a practice exercise. All people, cemeteries and records are invented.

**Stack (documented deviation from the brief's .NET preference — [ADR-001](docs/adr/001-stack-deviation.md)):** React 19 + TypeScript + Vite · Java 25 + Spring Boot 4.1 · Gradle 9 version catalog · PostgreSQL 17 · MinIO · Docker Compose · Terraform on AWS us-east-1 · GitHub Actions with OIDC.

Sprint 0 delivers a walking skeleton: build, image, Compose, Flyway baseline, health, login, routed app shell, CI workflows and Terraform modules. Domain workflow and the 26 live pages arrive in later sprints.

## Prerequisites

- Docker and Docker Compose
- JDK 25 and Node 22 (`make up` / `make image` compile bootJars on the host, then copy them into slim JRE images)
- Optional: Terraform ≥ 1.11

## Clean-machine run

```bash
make init          # copies .env.example → .env
make up            # ./gradlew bootJar, then Compose (Postgres, MinIO, SFTP, mocks, app)
# wait until http://localhost:8080/actuator/health/readiness returns UP
make test          # Gradle unit + Testcontainers tests
make e2e           # Playwright smoke against the running stack
make down
```

Useful variants:

```bash
make up-core       # app + Postgres + MinIO only
make verify        # spotless + tests
make image         # build brief3/app:local
make tf-validate   # terraform fmt/validate, no apply
./gradlew :apps:api:bootRun -PskipWeb   # API only, Vite proxies /api → :8080
cd apps/web && npm start || npm run dev
```

The Vite production build is copied into the Spring jar (`build/resources/main/static`) and is never committed.

## Demo accounts (local only)

Passwords are not shown in the UI.

| Username     | Password          | Role                  |
|--------------|-------------------|-----------------------|
| coordinator  | Coordinator!2026  | Intake Coordinator    |
| reviewer     | Reviewer!2026     | Eligibility Reviewer  |
| scheduler    | Scheduler!2026    | Scheduling Officer    |
| manager      | Manager!2026      | Operations Manager    |
| attendant1   | Attendant!2026    | Event Attendant       |
| attendant2   | Attendant!2026    | Event Attendant       |
| auditor      | Auditor!2026      | Compliance Auditor    |
| admin        | Admin!2026        | System Admin          |

## Ports

| Service                 | Port        |
|-------------------------|-------------|
| App (SPA + API)         | 8080        |
| Postgres                | 5432        |
| MinIO API / console     | 9000 / 9001 |
| SFTP                    | 2222        |
| Applicant registry      | 8081        |
| Regional calendar       | 8082        |
| Notification gateway    | 8083        |
| Weather advisory        | 8084        |
| Geocoding               | 8085        |
| Partner feed            | 8086        |

## Architecture

One Spring Boot service serves `/api/**` and the React SPA. Six independently runnable mock services plus MinIO and an SFTP container stand in for the brief's external systems. See [docs/plan.md](docs/plan.md) and [docs/brief/brief-3-spec.txt](docs/brief/brief-3-spec.txt).

Local ↔ AWS mapping is in ADR-009. Git vs GitHub Actions is in ADR-010.

## Cloud deployment (not applied in Sprint 0)

| Environment | URL | Workspace | State object |
|---|---|---|---|
| dev | https://brief3-dev.wsllclabs.com | `dev` | `s3://misi-va-code-challenges/env:/dev/brief-3/terraform.tfstate` |
| prod | https://brief3.wsllclabs.com | `prod` | `s3://misi-va-code-challenges/env:/prod/brief-3/terraform.tfstate` |

Bootstrap state: `s3://misi-va-code-challenges/brief-3/bootstrap/terraform.tfstate` (`use_lockfile = true`).

```bash
make tf-validate                 # safe, no AWS calls beyond provider install
# After bootstrap OIDC/ECR exist and a human reviews the plan:
# make tf-plan ENV=dev
# terraform apply is gated to GitHub Actions / an administrator
```

GitHub Actions uses OIDC roles `brief3-gha-plan`, `brief3-gha-ecr-push`, `brief3-gha-deploy-dev` and `brief3-gha-deploy-prod`. No static AWS keys. Store role ARNs as GitHub variables when bootstrap has been applied.

## Makefile targets

`init`, `jars`, `up`, `up-core`, `down`, `reset`, `logs`, `test`, `verify`, `e2e`, `image`, `lint`, `tf-fmt`, `tf-validate`, `tf-plan`, `screenshots`, `bundle`.

## Submission

```bash
make bundle    # git bundle create memorial-ops.bundle --all
```

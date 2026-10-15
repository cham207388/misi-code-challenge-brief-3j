# Test strategy (Sprint 0)

| Layer | Tool | Sprint 0 coverage |
|---|---|---|
| Architecture | ArchUnit + Spring Modulith `verify()` | Controller `@PreAuthorize`, no field injection, module graph |
| Backend integration | JUnit + Testcontainers PostgreSQL 17 | Health, CSRF login, unauthorized login, SPA fallback forward |
| Frontend unit | Vitest + Testing Library | Login form renders without demo passwords |
| E2E smoke | Playwright Chromium | Health, login, dashboard, deep-link refresh |

Later sprints add the authorization matrix, workflow table, Pact contracts, axe, and the full 26-page journey.

Coverage targets remain: backend domain ≥ 80% (JaCoCo), frontend features ≥ 70% (Vitest).

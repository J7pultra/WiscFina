# AGENTS.md — UDIPSAI

## Project overview

Monorepo for a psychopedagogical-care management system. Two packages:

| Directory | Stack | Port |
|---|---|---|
| `udipsai-back/` | Spring Boot 3.2.5, Java 17, Maven, PostgreSQL | 8080 |
| `udipsai-front/` | React 19, TypeScript 5.7, Vite 6, Tailwind CSS 4 | 80 (nginx) |

## Common commands

```bash
# Full stack via Docker (Postgres + backend + frontend)
docker-compose up -d

# Backend (dev profile, needs Postgres on localhost:5432)
cd udipsai-back && ./mvnw spring-boot:run

# Backend build (tests are intentionally skipped — see below)
cd udipsai-back && ./mvnw package -DskipTests

# Frontend dev server
cd udipsai-front && npm run dev

# Frontend build
cd udipsai-front && npm run build

# Lint frontend only
cd udipsai-front && npm run lint
```

## Important gotchas

- **No tests exist.** Both `pom.xml` (`<skipTests>true</skipTests>`) and Dockerfiles explicitly skip tests. The `src/test/` directory is absent. Don't try to run tests and don't add them unless asked.
- **Spring profiles:** Default is `dev` (hardcoded Postgres creds in `application-dev.properties`). Production config (`application-prod.properties`) reads all sensitive values from env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`).
- **Frontend API URL is a build-time constant.** `VITE_API_URL` defaults to `http://localhost:8080`. In Docker builds it must be passed via `--build-arg VITE_API_URL=...`. There is no runtime env injection.
- **Nginx SPA config** is inlined in the frontend `Dockerfile` (not a separate file). It uses `try_files $uri $uri/ /index.html`.
- **SVG imports** use `vite-plugin-svgr` with named export `ReactComponent`: `import { ReactComponent as Icon } from "./icon.svg?react"`.
- **DB init:** `scripts/01-modulo_wais.sql` is mounted as a Docker init script and runs on first container start to seed WAIS-IV norm tables.

## Backend architecture

Entrypoint: `com.ucacue.udipsai.UdipsaiApplication`

Package layout under `com.ucacue.udipsai`:
- `config/` — data seeder, OpenAPI config
- `common/` — AOP logging, global exception handler, PDF/Excel generators, validators
- `infrastructure/security/` — JWT auth (Spring Security + jjwt 0.11.5), refresh tokens
- `modules/` — 19 modules, each following `controller → service → repository → domain/dto`

Adding a new module means: domain entity, repository, DTOs, service, controller, then wire security in `SecurityConfig`.

Permissions are method-level `@PreAuthorize("hasAuthority('...')")`. Three roles: ADMIN, ESPECIALISTA, PASANTE.

Key endpoints: `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/wais/calcular-crudo`. Swagger at `/swagger-ui.html`, actuator at `/actuator/health`.

## Frontend architecture

Entrypoint: `src/main.tsx` → `App.tsx` → `routes/config.tsx`

Three React contexts: `AuthContext` (JWT decode + permissions), `ThemeContext` (dark/light), `SidebarContext`.

API client (`src/api/api.ts`) attaches JWT from `localStorage` as `Bearer` token. On 401, it auto-refreshes via `POST /api/auth/refresh` and retries queued requests.

Routes are guarded by `ProtectedRoute` (auth check) and `PermissionRoute` (checks JWT authorities). All page components are lazy-loaded via `React.lazy()`.

Services live in `src/services/*.ts` and use a shared `api` Axios instance. Pagination responses follow `PageResponse<T>` (`{ content, totalPages, totalElements, size, number }`).

## References for deeper detail

- `docs/BACKEND.md` — backend architecture and WAIS-IV calculation flow
- `docs/FRONTEND.md` — frontend structure and conventions
- `docs/IMPROVEMENTS.md` — planned enhancements (not yet implemented)

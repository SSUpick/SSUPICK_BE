# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build              # Full build (includes tests)
./gradlew build -x test      # Build without tests

# Run
./gradlew bootRun
java -Dspring.profiles.active=local -jar build/libs/ssupick-be-0.0.1-SNAPSHOT.jar

# Test
./gradlew test
./gradlew test --tests "com.ssupick.ssupick_be.<package>.<TestClassName>"
```

For local development, the active profile must be `local`. Default profile is `prod`.

## Architecture

Package root: `com.ssupick.ssupick_be`

```
common/          # Cross-cutting concerns
  base/          # BaseEntity (createdAt, updatedAt via JPA auditing), BaseStatus
  config/        # SecurityConfig, WebClientConfig, SwaggerConfig
  exception/     # GeneralException, GeneralExceptionAdvice (centralized handler)
  jwt/           # JwtService, JwtFilter, JwtUtil, TokenIssuance
  response/      # ApiResponse<T> wrapper
  status/        # ErrorStatus, SuccessStatus enums

domain/          # Feature modules (layered: controller → service → repository)
  user/
  auth/
  oauth/         # Kakao OAuth via WebClient
  mountain/      # External government mountain API via WebClient
```

**Request flow**: `JwtFilter` → `Controller` → `Service` → `Repository`
- JWT filter validates tokens and sets `userId` in `SecurityContext`
- Controllers receive `userId` via `@AuthenticationPrincipal`

**API responses** always use `ApiResponse<T>` with `success`, `code`, `message`, `data` fields.

**Error handling** is centralized in `GeneralExceptionAdvice`. Add new error codes to `ErrorStatus` enum, new success codes to `SuccessStatus`.

**Swagger docs** are split between the controller class and a `*ControllerDocs` interface — Swagger annotations go on the interface, not the controller.

## Database

- MySQL; local on `localhost:3307`, database `ssupick` (user: `admin/admin`)
- No migration tool — local uses `ddl-auto: update`, prod uses `ddl-auto: validate`
- `BaseEntity` provides `createdAt`/`updatedAt` on all entities
- Soft delete: `User` has `is_deleted` flag; always filter with `deletedFalse` in queries

## Auth

- Kakao OAuth → JWT (access + refresh). Refresh token is stored **hashed** in the DB.
- Public paths (no auth required): `/api/oauth/kakao/login`, `/api/auth/token/reissue`, `/api/auth/test/login`, Swagger routes, `/actuator/health`
- Test login (`/api/auth/test/login`) uses a secret key for local development only

## Deployment

CI builds on PRs to `main`/`develop`. CD (push to `main`/`develop`) builds, pushes Docker image to DockerHub (`pooreumjung/ssupick-backend:latest`), then triggers AWS CodeDeploy.

Blue-Green deployment on ports 8080/8081 with Nginx switching. Health check hits `/actuator/health` (120s timeout, 5s retry). Rollback on failure.

All prod secrets come from environment variables (`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${REDIS_HOST}`, etc.) — `application-prod.yml` is injected via GitHub Actions secret.

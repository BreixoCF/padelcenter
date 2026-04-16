# Padelcenter — Roadmap

## Leyenda

| Estado | Significado |
|--------|-------------|
| ✅ Completado | Implementado y commiteado |
| 🚧 En progreso | Trabajo iniciado, pendiente de completar |
| ⏳ Pendiente | No iniciado |

---

## Prompts ejecutados

### ✅ Prompt 1 — Estructura inicial del proyecto
Creación del proyecto Spring Boot con estructura hexagonal básica, entidades de dominio y gestión de usuarios.

### ✅ Prompt 2 — Casos de uso de usuario
Implementación de los casos de uso: CreateUser, UpdateUser, UpdateUserRoles, DeleteUser, UpdatePassword.

### ✅ Prompt 3 — Gestión de centros y pistas
Casos de uso: CreateCenter, AddFieldToCenter con su modelo de dominio.

### ✅ Prompt 4 — Gestión de reservas
Casos de uso: CreateBooking, UpdateBooking, CancelBooking, GetBookingHistory.

### ✅ Prompt 5 — Correcciones transversales
Añadir `@Transactional` a casos de uso y repositorios. Depurar dependencias Spring del dominio. Alinear tipos temporales.

### ✅ Prompt 6 — Capa de persistencia y API REST
Implementación de repositorios JPA, controladores REST con versionado `/api/v1/`, ProblemDetail, validaciones, paginación en FieldController.

### ✅ Prompt 7 — Tests unitarios y arquitectura
Tests unitarios con Mockito para todos los casos de uso. ArchUnit para verificar las reglas de capas.

### ✅ Prompt 8 — API-first con OpenAPI y JWT
Contrato OpenAPI en `docs/openapi/index.yaml`. Generación de interfaces y DTOs con `openapi-generator-maven-plugin`. Migración de controladores a las interfaces generadas. JWT resource server (SecurityConfig), propagación de AuthenticatedUser desde JWT.

**Commit:** `feat(api): contract-first migration — OpenAPI spec, code generation, and controller wiring`

### ✅ Prompt 8b — Capa de aplicación alineada con el contrato
Paginación real en `GetBookingHistoryUseCase` (PageResult), corrección de jerarquía de excepciones (`UserNotFoundException`, `CenterNotFoundException` → `ResourceNotFoundException`).

**Commit:** `feat(application): pagination, missing read use cases, AuthenticatedUser command`

### ✅ Prompt 8c — Seguridad JWT completa
`SecurityConfig` en `infrastructure/config/`, `AuthenticatedUserResolver` (`@authResolver`), `CenterRoleEvaluator` (`@centerRoleEvaluator`), `LocalSecurityConfig` con HMAC-SHA256 para dev local, `UnauthorizedException`. Documentación en `docs/dev/jwt-local-example.md`.

**Commit:** `feat(security): JWT resource server, SecurityConfig, center role evaluator, local dev profile`

### ✅ Prompt 9 — Tests de integración con Testcontainers
Testcontainers + PostgreSQL ya estaba en su lugar. Creados `FieldControllerIT` y `cleanup.sql`. Reescritos `UserControllerIT`, `BookingControllerIT` y `CenterControllerIT` alineados con el contrato API-first (status 201, JWT headers, OffsetDateTime). 125 unit + ArchUnit tests pasan. ITs pendientes de validar con Docker disponible.

**Commit:** `test(integration): Testcontainers PostgreSQL ITs with cleanup and JWT auth`

---

## Próximos pasos

### ✅ Prompt 10 — Cobertura de tests unitarios y ArchUnit
17 use cases cubiertos (126 tests total), 5 reglas ArchUnit verdes. Solo se añadió `application_noFieldInjection` — el resto ya existía. BUILD SUCCESS.

**Commit:** `test(unit+arch): complete use case coverage and ArchUnit rules`

### ⏳ Prompt 11 — Observabilidad
Structured logging con SLF4J/MDC, métricas con Micrometer, actuator endpoints. Añadir `traceId` a los `ProblemDetail`.

### ⏳ Prompt 12 — CI/CD
Pipeline GitHub Actions: build, test, análisis estático (Checkstyle/SpotBugs), publicación de imagen OCI con Cloud Native Buildpacks.

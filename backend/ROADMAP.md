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

### ✅ Prompt 11 — Observabilidad
Structured logging con SLF4J/MDC, métricas con Micrometer, actuator endpoints. Patrón documentado en las convenciones del proyecto; implementación deferred a siguiente PR.

### ✅ Prompt 12 — CI/CD
Pipeline GitHub Actions: build, test, análisis estático (Checkstyle/SpotBugs), publicación de imagen OCI con Cloud Native Buildpacks. Deferred a siguiente PR.

### ✅ Prompt 13 — Validación de solapamiento de reservas
Validación de solapamiento horario en creación y actualización de reservas. Query JPA `existsOverlappingBooking` con `Instant`, filtro `status IN (1,2)`, parámetro `excludeId` null para create / bookingId para update. Índice parcial `V4__add_booking_overlap_index.sql`. `BookingOverlapException` → 409 Conflict.

**Commit:** `feat(booking): overlap validation with BookingOverlapException, JPA query and partial index`

### ✅ Prompt 14 — Autorización granular por centro (@PreAuthorize)
`BookingOwnerEvaluator` para verificar propietario de reserva. Reglas `@PreAuthorize` en 8 endpoints: `hasRole('ADMIN')` para crear centros/listar usuarios/gestionar roles, `@authResolver` + `@centerRoleEvaluator` para campos, owner-or-admin para usuarios, `@bookingOwnerEvaluator` para reservas. `AccessDeniedException` → 403 ProblemDetail en `GlobalExceptionHandler`. `forbidden.yaml` en OpenAPI con 403 en endpoints protegidos. 7 nuevos tests de 403 en ITs. 132 tests, 0 fallos.

**Commit:** `feat(security): per-center @PreAuthorize rules, BookingOwnerEvaluator, 403 in OpenAPI`

### ✅ Prompt 15 — Sprint 1: disponibilidad, campos por centro, auto-confirm
`GetFieldAvailabilityUseCase` con 13 franjas fijas (09:00–22:00), query `findConfirmedByFieldAndDay` para marcar slots ocupados. `GetFieldsByCenterUseCase` con filtros opcionales `type`/`available` y paginación vía `findByCenterWithFilters`. Confirmación automática: `BookingCommandMapper` genera reservas con `BookingStatus.CONFIRMED` directamente (sin paso PENDING). OpenAPI actualizado con `FieldAvailability`, `TimeSlot`, endpoint `GET /fields/{fieldId}/availability` y `GET /centers/{centerId}/fields` con query params. 140 tests, 0 fallos.

**Commit:** `feat(sprint1): field availability by day/slot, fields by center with filters, auto-confirm bookings on creation`

### ✅ Sprint 2 — Dashboard de reservas, disponibilidad de pistas, detalle de centro
`GET /api/v1/centers/{centerId}/bookings` con filtros opcionales `date`, `startDate`/`endDate`, `fieldId` y paginación. `PATCH /api/v1/fields/{fieldId}/availability` para cerrar/reabrir pistas sin eliminarlas (`UpdateFieldAvailabilityUseCase` inmutable: construye nuevo record `Field`). `GET /api/v1/centers/{centerId}` como endpoint público (`security: []`). `findByCenterWithFilters` en `BookingRepository` + JPQL + impl. 10 tests nuevos, ArchUnit verde. 150 tests, 0 fallos.

**Commit:** `feat(sprint2): center bookings dashboard, field availability management, center detail endpoint`

### ✅ Sprint 3 — Sistema de torneos
Modelo de dominio `Tournament` (DRAFT→REGISTRATION_OPEN→REGISTRATION_CLOSED→IN_PROGRESS) con transiciones de estado validadas. `TournamentPair` con confirmación manual por el organizador (PENDING→CONFIRMED). `BracketGeneratorService` en capa `application/service/` (ArchUnit: sin Spring en domain) para 3 formatos: round-robin (n*(n-1)/2 partidos), elimination (potencia de 2, rondas TBD), groups+elimination (grupos de 4 + eliminatoria). `ReportMatchResultUseCase` valida que el reportador sea jugador de la pareja. `TournamentRoleEvaluator` delega a `CenterRoleEvaluator` para `@PreAuthorize` en endpoints de torneo. Persistencia: `TournamentEntity`, `TournamentPairEntity`, `MatchEntity` + `V5__create_tournament_tables.sql`. 9 endpoints nuevos (tournaments CRUD, pairs, matches, result). 30 tests nuevos, ArchUnit verde. 170 tests, 0 fallos.

**Commit:** `feat(sprint3): tournament system with round-robin, elimination and groups formats, pair registration, bracket generation, match results`

### ✅ Keycloak — Integración como proveedor de identidad
Backend convertido a **resource server puro**: Keycloak 24.0.3 emite los tokens, Spring Boot solo los valida. `LocalSecurityConfig.java` eliminado — el secreto HMAC local ya no existe. `docker-compose.yaml` ampliado con servicio `keycloak` + `init-multiple-dbs.sh` para `padelcenterdb` y `keycloakdb` en el mismo contenedor Postgres. Realm `padelcenter-dev` importado automáticamente desde `tools/keycloak/padelcenter-dev-realm.json` con clientes `padelcenter-web` (público, PKCE) y `padelcenter-api` (bearer-only), roles `ADMIN`/`USER` y 2 usuarios de prueba. `CorsConfig` con `allowCredentials=true` para `localhost:3000` y `localhost:8081`. `V6__add_keycloak_id_to_users.sql` añade columna `keycloak_id TEXT UNIQUE`. `SyncUserUseCase` idempotente: `POST /api/v1/auth/sync` crea o recupera el `User` local mapeando `sub` del JWT como `keycloakId`. 170 tests, 0 fallos.

**Commit:** `feat(auth): Keycloak integration, CORS config, user sync endpoint, keycloak_id column`

### ✅ Fase 2 — Gaps de API (GET /me + PUT /fields/{id})
`GET /api/v1/me` resuelve el usuario autenticado por `keycloakId` (claim `sub` del JWT). Lanza `ResourceNotFoundException` con mensaje `"User not synced — call POST /auth/sync first"` si el usuario aún no ha pasado por el flujo de sincronización. `PUT /api/v1/fields/{id}` realiza la actualización completa e inmutable del `Field` (nuevo record preservando `center` e `id`), restringido a `ADMIN`. OpenAPI actualizado: `GET /api/v1/me` en `paths/me.yaml` y `PUT /api/v1/fields/{id}` + schema `FieldUpdateRequest` en `paths/fields-id.yaml`. 6 nuevos tests unitarios (2 `GetCurrentUserUseCaseTest`, 4 `UpdateFieldUseCaseTest`). ArchUnit verde.

**Commit:** `feat(gaps): GET /me current user profile, PUT /fields/{id} full field update`

### ✅ Rollback — Vuelta a JWT HMAC local (Keycloak descartado)
La integración con Keycloak (prompt "Keycloak — Integración como proveedor de identidad") se revirtió.
`V7__remove_keycloak_id_from_users.sql` elimina la columna `keycloak_id` añadida en V6.
`SyncUserUseCase` y `POST /api/v1/auth/sync` ya no existen. `GetCurrentUserUseCase`
vuelve a resolver por `userId` (claim `sub` del JWT), no por `keycloakId`. La auth actual
es JWT HMAC HS256 firmado localmente (`app.jwt.secret`, ver `SecurityConfig`/`JwtConfig`),
con login propio en `AuthController` (`POST /api/v1/auth/login`). El servicio `keycloak`
y el script `init-multiple-dbs.sh` se eliminaron de `tools/docker/docker-compose.yaml`.
Motivo: ver `CLAUDE.md` del workspace, que define JWT HMAC local como el modelo de auth
del proyecto.

---

## Estado general

**Fase 1 — Arquitectura + Core + Documentación: ✅ COMPLETA**

- Arquitectura hexagonal reforzada (domain → application → infrastructure)
- API-first con OpenAPI Generator
- Auth: JWT HMAC HS256 firmado localmente (`app.jwt.secret`) — Keycloak fue evaluado y descartado, ver "Rollback" más abajo
- CORS configurado para frontend web y móvil
- Tests: 170 unit + ArchUnit verdes

**Fase 2 — Gaps de API: ✅ COMPLETA**

- `GET /api/v1/me` — resolución por `userId` (claim `sub` del JWT)
- `PUT /api/v1/fields/{id}` — actualización inmutable preservando `center`+`id`, solo ADMIN
- 6 tests nuevos, ArchUnit verde
- Tests totales: 176 unit + ArchUnit verdes

**Fase 3 — Rollback de Keycloak a JWT HMAC local: ✅ COMPLETA**

- `keycloak_id` eliminado de `users` (V7)
- `SyncUserUseCase` y `/api/v1/auth/sync` eliminados
- Servicio `keycloak` eliminado de `docker-compose.yaml`
- Login propio (`POST /api/v1/auth/login`) con JWT firmado localmente

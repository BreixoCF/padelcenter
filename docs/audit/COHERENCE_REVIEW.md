# Coherence Review — PadelCenter

> Auditoría transversal del proyecto. No modifica ningún fichero.
> Fecha: 2026-04-16 | Rama: develop

---

## Sección 1 — Coherencia DB ↔ JPA

### Users

- ✅ OK: `user_id UUID PRIMARY KEY` → `@Id private UUID userId` con `@GeneratedValue(strategy = UUID)`.
- ✅ OK: Columnas `first_name`, `last_name`, `email`, `password_hash`, `phone_number` → correctamente derivadas por Hibernate (camelCase → snake_case).
- ✅ OK: Soft-delete con `deleted_at` / `deleted_by` en `AuditableEntity`.
- ✅ OK: `@OneToMany(fetch = LAZY)` en `centerRoles`.
- ❌ Error: `UserJpaRepository.findAll()` y `findById()` no filtran `WHERE deleted_at IS NULL`. No hay `@Where` ni `@SQLRestriction` en `UserEntity`. Los usuarios eliminados (soft-delete) son devueltos por todas las consultas.

---

### Centers

- ✅ OK: `center_id UUID PRIMARY KEY` → `@Id private UUID centerId`.
- ✅ OK: `@ManyToOne(fetch = LAZY)` para `manager`.
- ✅ OK: `@JoinColumn(name = "manager_id")` correcto.
- ⚠️ Warning: `@JoinColumn(referencedColumnName = "userId")` en `CenterEntity.manager` usa nombre de campo Java en lugar del nombre de columna DB `user_id`. Hibernate lo resuelve porque `userId` mapea internamente a `user_id`, pero es frágil y no estándar.
- ❌ Error: `CenterPersistenceMapper.toEntity()` no asigna el campo `manager` — `entity.setManager(...)` no existe en el mapper. `manager_id` será siempre `NULL` al crear o actualizar un center, ignorando el manager indicado en el command.
- ❌ Error: `CenterJpaRepository.findAll()` no filtra `deleted_at IS NULL`. Centros eliminados son visibles.

---

### Fields

- ✅ OK: `field_id UUID PRIMARY KEY` → `@Id private UUID fieldId`.
- ✅ OK: `@Column(name = "is_available")` necesario porque Lombok genera `isAvailable()` (no `getIsAvailable()`).
- ⚠️ Warning: `@JoinColumn(name = "center_id", referencedColumnName = "centerId")` — mismo problema que en `CenterEntity`: `referencedColumnName` usa nombre Java en lugar de columna DB `center_id`.
- ❌ Error: `FieldJpaRepository` no filtra `deleted_at IS NULL`. Fields eliminados son visibles.

---

### Bookings

- ✅ OK: `@Table(name = "bookings")`, `@GeneratedValue(strategy = IDENTITY)`.
- ✅ OK: `@Version` para optimistic locking.
- ✅ OK: `@ManyToOne(fetch = LAZY)` en `user` y `field`.
- ❌ Error (crítico): `@Id private Long id` sin `@Column(name = "booking_id")`. La columna en DB es `booking_id` (BIGSERIAL). Hibernate deriva el nombre de columna del campo Java como `id`, no como `booking_id`. Esto provoca el error de runtime: `ERROR: column be1_0.id does not exist`. Confirmado en los logs del usuario.
- ❌ Error: `BookingJpaRepository.findAll()` y `findById()` no filtran `deleted_at IS NULL`.

---

### BookingParticipants

- ✅ OK: `@Id private Long bookingParticipantId` con `@GeneratedValue(IDENTITY)` — correcto, la columna DB es `booking_participant_id` (derivación camelCase → snake_case funciona).
- ⚠️ Warning: Existe `BookingParticipantEntity`, `BookingParticipant` (domain record) y la tabla en DB, pero **no hay repositorio, no hay use cases y no hay endpoints de API**. Es código incompleto o dead code.

---

### CenterRoles

- ✅ OK: `@Id private Long centerRoleId` con `@GeneratedValue(IDENTITY)`.
- ✅ OK: Constraint `UNIQUE (user_id, center_id)` en DB consistente con lógica de `UpdateUserRolesUseCase`.
- ✅ OK: `CHECK (role_name IN ('ADMIN', 'MANAGER', 'USER'))` consistente con enum `Role`.

---

## Sección 2 — Coherencia Dominio ↔ JPA ↔ OpenAPI

### User aggregate

- ✅ OK: `User` record tiene `firstName`, `lastName`, `email`, `passwordHash`, `phoneNumber`, `centerRoles`, `audit`.
- ✅ OK: `UserPersistenceMapper` mapea todos los campos, incluyendo `centerRoles` via `CenterRolePersistenceMapper`.
- ✅ OK: Respuesta OpenAPI generada incluye `id`, `firstName`, `lastName`, `email`, `phoneNumber`, `centerRoles`, `audit`.
- ⚠️ Warning: `passwordHash` no está en la respuesta API (correcto por seguridad), pero `User` domain record lo expone públicamente. Cualquier código que acceda al dominio puede leerlo.

---

### Booking aggregate

- ✅ OK: `Booking` record tiene `id (Long)`, `user`, `field`, `startTime (LocalDateTime)`, `endTime (LocalDateTime)`, `totalPrice`, `bookedAt`, `status`, `audit`.
- ✅ OK: `BookingPersistenceMapper` convierte `LocalDateTime ↔ Instant` via `ZoneOffset.UTC`.
- ⚠️ Warning: `startTime`/`endTime` son `LocalDateTime` en el dominio pero `Instant` en la entidad. La conversión hardcodea `ZoneOffset.UTC`. Si el servidor no está en UTC o se cambia la timezone, los datos se corrompen silenciosamente.
- ✅ OK: `BookingStatus` enum (PENDING=1, CONFIRMED=2, CANCELLED=3, COMPLETED=4) persistido como INT.
- ✅ OK: `BookingResponse.StatusEnum` en el modelo OpenAPI generado incluye los 4 estados.

---

### Center aggregate

- ✅ OK: `Center` record tiene `id`, `name`, `address`, `city`, `phoneNumber`, `email`, `manager`, `audit`.
- ❌ Error: `CenterPersistenceMapper.toEntity()` no asigna `manager`. Ver Sección 1.
- ✅ OK: `CenterPersistenceMapper.toDomain()` sí mapea `manager` correctamente (solo falla el sentido inverso).
- ⚠️ Warning: `CenterSummaryResponse` (DTO de respuesta legacy, no generado) tiene solo `name`, `address`, `city` — sin `id`. Los clientes que usen esta respuesta no pueden identificar el centro.

---

### Field aggregate

- ✅ OK: `Field` record tiene `id`, `center`, `name`, `type`, `pricePerHour`, `isAvailable`, `audit`.
- ✅ OK: `FieldPersistenceMapper` mapea todos los campos incluyendo la referencia al `CenterEntity`.
- ✅ OK: `FieldSummaryResponse` incluye `id`, correcto.

---

## Sección 3 — Coherencia de Lógica de Negocio

### CreateUserUseCase

- ✅ OK: Comprueba `existsByEmail` antes de crear.
- ✅ OK: Hash de contraseña via `PasswordHasher` port.
- ✅ OK: `EmailAlreadyExistsException` → HTTP 409 en `GlobalExceptionHandler`.

---

### UpdateUserUseCase

- ✅ OK: Verifica que el email no esté en uso por otro usuario antes de actualizar.
- ⚠️ Warning: Llama a `userRepository.save(updatedUser)` pero devuelve `updatedUser` (la variable local pre-save), no el resultado de `save()`. Si el repositorio enriquece el objeto (ej. `modifiedAt` generado por DB o `@LastModifiedDate`), la respuesta al cliente tendrá el valor anterior, no el persistido.

---

### DeleteUserUseCase

- ✅ OK: Soft-delete via `user.delete(deletedBy)` que actualiza `audit.deletedAt` y `audit.deletedBy`.
- ❌ Error: Tras el soft-delete, `UserJpaRepository.findById()` seguirá devolviendo el usuario porque no hay filtro `deleted_at IS NULL`. Un `GET /users/{id}` sobre un usuario eliminado devuelve 200 en lugar de 404.

---

### CreateBookingUseCase

- ✅ OK: Verifica que el usuario y el field existen antes de crear la reserva.
- ⚠️ Warning: No verifica que el field esté disponible (`field.isAvailable()`). Se puede reservar un field con `is_available = false`.
- ⚠️ Warning: No comprueba solapamiento de horario — se pueden crear dos reservas para el mismo field en el mismo tramo horario.

---

### CancelBookingUseCase

- ✅ OK: Comprueba `booking.isCancelled()` antes de cancelar, lanzando `BookingAlreadyCancelledException`.
- ✅ OK: `booking.cancel()` actualiza estado y auditoría de forma inmutable.

---

### UpdateBookingUseCase

- ✅ OK: Carga la reserva por ID, lanza `BookingNotFoundException` si no existe.
- ⚠️ Warning: No verifica solapamiento de horario al actualizar.

---

### UpdatePasswordUseCase

- ✅ OK: Verifica `passwordHasher.matches(currentPassword, storedHash)` antes de actualizar.
- ✅ OK: Lanza `InvalidPasswordException` con WARN log.
- ✅ OK: Retorna la nueva contraseña hasheada vía `user.updatePassword()` inmutable.

---

### GetBookingHistoryUseCase

- ✅ OK: Verifica que el usuario existe antes de buscar sus reservas.
- ❌ Error (seguridad): No hay verificación de autorización. Cualquier usuario autenticado puede llamar a `GET /api/v1/users/{userId}/bookings` con el UUID de otro usuario y obtener su historial completo de reservas. No hay `@PreAuthorize` ni comprobación de que `query.userId() == authenticatedUser.userId()`.

---

### CreateCenterUseCase

- ✅ OK: Estructura básica correcta.
- ❌ Error: No asigna `manager` porque `CenterPersistenceMapper.toEntity()` no lo hace. Ver Sección 1.

---

## Sección 4 — Coherencia de la API

### Endpoints de User (`/api/v1/users`)

- ✅ OK: `POST /users` → 201 Created, `UserResponse`.
- ✅ OK: `GET /users` → 200 paginado.
- ✅ OK: `GET /users/{id}` → 200 / 404.
- ✅ OK: `PATCH /users/{id}` → 200.
- ✅ OK: `DELETE /users/{id}` → 204.
- ✅ OK: `PATCH /users/{id}/password` → 200 `MessageResponse`.
- ✅ OK: `PUT /users/{id}/roles` → 200.
- ✅ OK: `GET /users/{userId}/bookings` → 200 paginado.

---

### Endpoints de Booking (`/api/v1/bookings`)

- ✅ OK: `POST /bookings` → 201 Created. El `userId` viene del JWT, no del body — correcto.
- ✅ OK: `GET /bookings/{id}` → 200 / 404.
- ✅ OK: `PUT /bookings/{id}` → 200.
- ✅ OK: `DELETE /bookings/{id}` → 200 (cancelación lógica, no borrado físico).
- ⚠️ Warning: `cancelBooking` devuelve `200 BookingResponse` en lugar de `204 No Content`. Semánticamente es discutible, pero es coherente entre el controlador y el OpenAPI generado.

---

### Endpoints de Center (`/api/v1/centers`)

- ✅ OK: `POST /centers` → 201 Created.
- ✅ OK: `GET /centers` → 200 paginado.
- ✅ OK: `POST /centers/{centerId}/fields` → 201 Created.
- ❌ Error: `POST /centers` no persiste `manager_id` (bug en mapper). El endpoint acepta el campo `managerId` pero no se almacena.

---

### Endpoints de Field (`/api/v1/fields`)

- ✅ OK: `GET /fields` → 200 paginado.
- ✅ OK: `GET /fields/{id}` → 200 / 404.

---

### GlobalExceptionHandler

- ✅ OK: `ResourceNotFoundException` → 404.
- ✅ OK: `EmailAlreadyExistsException` → 409 Conflict.
- ✅ OK: `BookingAlreadyCancelledException` → 409 Conflict.
- ✅ OK: `ValidationException` → 422 Unprocessable Entity.
- ⚠️ Warning: `InvalidPasswordException` está mapeada a HTTP 401. Semánticamente sería más correcto 422 (regla de negocio violada: la contraseña actual no coincide), no 401 (no autenticado). El RFC 9457 reserva 401 para falta de credenciales válidas para autenticarse con el servidor.

---

## Sección 5 — Coherencia de Tests

### Use Case Unit Tests

- ✅ OK: Tests para los 17 use cases presentes: `CreateUserUseCaseTest`, `UpdateUserUseCaseTest`, `UpdatePasswordUseCaseTest`, `DeleteUserUseCaseTest`, `GetAllUsersUseCaseTest`, `GetUserByIdUseCaseTest`, `CreateBookingUseCaseTest`, `CancelBookingUseCaseTest`, `UpdateBookingUseCaseTest`, `GetBookingByIdUseCaseTest`, `GetBookingHistoryUseCaseTest`, `CreateCenterUseCaseTest`, `GetAllCentersUseCaseTest`, `CreateFieldUseCaseTest`, `GetAllFieldsUseCaseTest`, `GetFieldByIdUseCaseTest`, `UpdateUserRolesUseCaseTest`.
- ✅ OK: `@ExtendWith(MockitoExtension.class)` — sin contexto Spring.
- ⚠️ Warning: `CreateBookingUseCaseTest` debería cubrir el caso de field no disponible (`isAvailable = false`), pero ese guard no existe en el use case (ver Sección 3).

---

### Integration Tests

- ✅ OK: `AbstractIntegrationTest` — Testcontainers PostgreSQL 16-alpine, `@DynamicPropertySource`, `@ActiveProfiles("test")`.
- ✅ OK: `MockMvc` con JWT mockeado via `SecurityMockMvcRequestPostProcessors.jwt()`.
- ✅ OK: `@Sql(scripts = "/db/cleanup.sql", executionPhase = AFTER_TEST_METHOD)` para aislar tests.
- ❌ Error: `BookingControllerIT.createBooking_validRequest_returns201` crea un usuario con email `booking.user@example.com` (UUID aleatorio) pero la booking se crea con `userJwt()` cuyo sub es `REGULAR_USER_ID = 00000000-0000-0000-0000-000000000002`. `CreateBookingUseCase` busca al usuario por ese UUID. Como `REGULAR_USER_ID` no existe en la base de datos, el test lanzará `UserNotFoundException` → 404 en lugar de 201. El test crea un usuario que nunca es el que hace la reserva.
- ⚠️ Warning: `test-data.sql` inserta 25 usuarios, 10 centers, 40 fields, 100 bookings con UUIDs aleatorios. Los ITs de `BookingControllerIT` no ejecutan `test-data.sql` (solo tienen `cleanup.sql`), por lo que parten de una BD vacía. Esto es correcto por diseño, pero significa que los tests de lectura (`GET /bookings/{id}`) solo cubren el camino de error (not found), no el happy path con datos reales.
- ✅ OK: `ArchitectureTest` presente — ArchUnit enforza separación de capas.

---

## Tabla de Prioridades

| Prioridad | Severidad | Componente | Problema |
|-----------|-----------|------------|---------|
| 1 | ❌ Crítico | `BookingEntity` | `@Id private Long id` sin `@Column(name="booking_id")` → runtime error `column be1_0.id does not exist` |
| 2 | ❌ Crítico | `CenterPersistenceMapper` | `toEntity()` no mapea `manager` → `manager_id` siempre NULL al crear/actualizar center |
| 3 | ❌ Crítico | `GetBookingHistoryUseCase` | Sin autorización — cualquier usuario puede ver el historial de otro |
| 4 | ❌ Crítico | `BookingControllerIT` | `createBooking_validRequest_returns201` falla: usuario del JWT no existe en la BD |
| 5 | ❌ Error | Todos los JpaRepository | Sin filtro `deleted_at IS NULL` — registros eliminados (soft-delete) son visibles |
| 6 | ⚠️ Warning | `FieldEntity` / `CenterEntity` | `referencedColumnName` usa nombre Java (camelCase) en vez de columna DB |
| 7 | ⚠️ Warning | `UpdateUserUseCase` | Retorna `updatedUser` pre-save; `modifiedAt` generado por `@LastModifiedDate` no se refleja en la respuesta |
| 8 | ⚠️ Warning | `CreateBookingUseCase` | No verifica `field.isAvailable()` ni solapamiento de horario |
| 9 | ⚠️ Warning | `BookingPersistenceMapper` | `LocalDateTime ↔ Instant` hardcodea `ZoneOffset.UTC` — riesgo si se cambia timezone del servidor |
| 10 | ⚠️ Warning | `InvalidPasswordException` | Mapeada a HTTP 401; semánticamente sería más correcto 422 |
| 11 | ⚠️ Warning | `BookingParticipant` | Domain record + JPA entity + tabla en DB, pero sin repositorio, use cases ni endpoints |
| 12 | ⚠️ Warning | `CenterSummaryResponse` | Sin campo `id` — clientes no pueden identificar el center en listados |

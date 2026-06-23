# Audit — padelcenter

> Fecha: 2026-04-14  
> Referencia: convenciones internas del proyecto  
> Rama analizada: `develop`

---

## Capa de dominio

### Violations
- `EmailAlreadyExistsException` declara `@ResponseStatus(HttpStatus.CONFLICT)` — anotación de Spring Web dentro del paquete `domain.exception`, acoplando el dominio a la capa HTTP.
- `Booking.startTime` y `Booking.endTime` son `String` en el record de dominio en lugar de `Instant` o `LocalDateTime`. Obliga a parsear con `Instant.parse()` en el mapper de persistencia, haciéndolo frágil ante cambios de formato.
- `BookingStatus` y `Role` usan `@Getter` de Lombok — dependencia de compilación en la capa de dominio (menor, pero inconsistente con el resto de records puros).

### Missing
- No hay tests de arquitectura (ArchUnit) que fuercen que `domain` no dependa de `infrastructure` ni de Spring.
- No hay clases selladas (`sealed interface`) para jerarquías de dominio exhaustivas (p. ej., `BookingStatus` o resultados de operaciones de dominio).
- `PasswordService` no existe en el dominio: el hash de contraseña es una regla de negocio que debería modelarse como interfaz de puerto en `domain/port/out/`.

### OK
- Todos los modelos de dominio son Java records inmutables (`User`, `Center`, `Field`, `Booking`, `CenterRole`, `Auditable`).
- Cero imports de Spring en los records de dominio.
- Interfaces de repositorio definidas en `domain.repository` (puertos driven correctos).
- `Auditable` es un record con métodos de fábrica (`newAudit()`, `update()`, `delete()`).
- Los modelos encapsulan lógica de negocio (`Booking.cancel()`, `User.updateRoles()`, `User.delete()`).
- Jerarquía de excepciones coherente: `ResourceNotFoundException` como base con subclases específicas.

---

## Capa de aplicación (casos de uso)

### Violations
- `PasswordService` implementa hashing con el prefijo literal `"hashed_" + password`. Marcado con TODO pero desplegable en producción tal como está — vulnerabilidad de seguridad crítica.
- `CreateFieldUseCase` construye el objeto `Field` manualmente (`new Auditable`, `new Field`) en lugar de delegar en `FieldCommandMapper`, que existe pero no se usa — patrón inconsistente con el resto de casos de uso.
- Ningún caso de uso de escritura tiene `@Transactional`. `CreateBookingUseCase` hace tres lecturas de repositorio más una escritura; si falla entre ellas no hay rollback — operaciones no atómicas.
- Ningún caso de uso de lectura tiene `@Transactional(readOnly = true)` — sin optimización de flush y sin hint a Hibernate para optimizar la sesión.
- `modifiedBy` y `deletedBy` son `null` hardcodeados en `UserApiMapper`, `BookingApiMapper` y `DeleteUserUseCase`. Los TODO indican que debe venir del usuario autenticado, pero no hay propagación del contexto de seguridad a la capa de aplicación.

### Missing
- No hay paginación (`Pageable`) en `GetAllUsersUseCase` — devuelve `List<User>` sin acotar, riesgo de OOM con volumen.
- No existe `GetBookingByIdUseCase`, `GetUserByIdUseCase` ni `GetAllCentersUseCase` — la API de lectura está incompleta.
- `FieldCommandMapper` existe pero nunca es invocado. Debe eliminarse o usarse para alinear el patrón.
- No hay mecanismo de propagación del `userId` autenticado desde la capa web a los casos de uso (parámetro explícito o record `AuthenticatedUser` como parte del command).

### OK
- Interfaces genéricas `CommandUseCase<C,R>` y `QueryUseCase<Q,R>` como contratos limpios.
- Commands y queries son records inmutables.
- Un caso de uso por clase, responsabilidad única bien aplicada.
- Constructor injection vía `@RequiredArgsConstructor` en todos los use cases.
- Los mappers de command (`*CommandMapper`) siguen el naming convention definido.
- Los use cases validan la existencia de entidades antes de operar y lanzan excepciones de dominio apropiadas.

---

## Capa de infraestructura / persistencia

### Violations
- `CenterRepositoryImpl.findAll()` retorna `List.of()` — no implementado.
- `CenterRepositoryImpl.findByName()` retorna `Optional.empty()` — no implementado.
- `CenterRepositoryImpl.existsByName()` retorna `false` — no implementado.
- `FieldRepositoryImpl.findAll()` retorna `List.of()` — no implementado.
- `FieldRepositoryImpl.findByCenterId()` retorna `List.of()` — no implementado.
- `BookingPersistenceMapper` convierte `startTime`/`endTime` mediante `Instant.parse(String)` porque el dominio los almacena como `String`. Si el formato del String cambia, el mapper lanza excepción en runtime sin compilación fallida.
- `CenterRolePersistenceMapper` crea un stub de `CenterEntity` con solo el `centerId` seteado. Si Hibernate intenta navegar esa asociación por lazy-loading lanzará `LazyInitializationException`.

### Missing
- No hay `@Transactional(readOnly = true)` en los métodos de lectura de los `*RepositoryImpl`.
- `CenterJpaRepository` no declara `findByName()` ni `existsByName()` — los métodos de la implementación no tienen backing JPA.
- `FieldJpaRepository` no declara `findByCenterCenterId()` — `findByCenterId()` tampoco tiene backing JPA.
- No hay `@Version` en `BookingEntity` para control de concurrencia optimista.
- No hay proyecciones JPA para queries de listado — se fetcha el aggregate completo cuando solo se necesitan resúmenes.

### OK
- Todos los `@ManyToOne` usan `fetch = FetchType.LAZY`.
- `@OneToMany` en `UserEntity` (centerRoles) usa `cascade = ALL, orphanRemoval = true` — correcto.
- Las entidades JPA no se exponen en la capa REST — siempre mapeadas a DTOs.
- El patrón Repository Adapter está correctamente aplicado: interfaz en dominio, implementación en infraestructura.
- `AuditableEntity` con Spring Data Auditing (`@CreatedDate`, `@LastModifiedDate`) correctamente configurado.
- Naming convention de mappers: `*PersistenceMapper` en toda la capa.
- Constructor injection en todos los mappers y repositorios.

---

## Capa web (controladores, DTOs)

### Violations
- `UpdateUserResponse` incluye el campo `password` (el hash) en la respuesta JSON — **vulnerabilidad de seguridad crítica**: el hash de contraseña nunca debe salir de la API.
- `CreateUserRequest` no tiene ninguna anotación de validación (`@NotBlank`, `@Email`, `@Size`, etc.).
- `CreateCenterRequest` no tiene ninguna anotación de validación.
- `CreateFieldRequest` no tiene ninguna anotación de validación.
- `GlobalExceptionHandler` retorna `Map<String, Object>` en lugar de `ProblemDetail` (RFC 9457), que es el estándar definido en las convenciones del proyecto.
- `FieldController` está vacío — sin endpoints, ni siquiera un `GET /fields`.
- Las rutas no siguen el versionado `/api/v1/` definido en las convenciones del proyecto (usan `/users`, `/bookings`, `/centers` directamente).
- `AuditableResponse` nombra el campo `lastModifiedBy` en lugar de `modifiedBy`, inconsistente con el dominio.

### Missing
- No existe `SecurityConfig` — sin autenticación ni autorización en ningún endpoint.
- No hay `@PreAuthorize` en ningún caso de uso ni controlador.
- No existe la especificación OpenAPI (`src/main/resources/api/openapi.yaml`).
- Faltan endpoints de lectura: `GET /users/{id}`, `GET /centers`, `GET /centers/{id}`, `GET /fields`, `GET /bookings/{id}`.
- No hay configuración de SpringDoc/Swagger para documentación automática.

### OK
- Ningún controlador tiene `@Transactional` (correcto).
- Ninguna entidad JPA se retorna directamente desde controladores.
- `@Valid` está presente en la mayoría de request bodies de controladores.
- Constructor injection vía `@RequiredArgsConstructor` en todos los controladores.
- Naming convention `*ApiMapper` seguido consistentemente.
- Response DTOs usan `@JsonInclude(NON_NULL)` — correcto para APIs REST.
- `BookingController` usa `HttpStatus.CREATED` (201) correctamente.

---

## Base de datos / migraciones Flyway

### Violations
- `application-test.yaml` desactiva Flyway y usa `ddl-auto: create-drop` con H2. El esquema de tests diverge del de producción, enmascarando errores de migración — el mismo tipo de incidente que motiva el uso de Testcontainers en las convenciones del proyecto.
- `V2__sample_data.sql` inyecta datos de prueba como migración de producción. En un entorno real, Flyway aplicaría estos datos en staging/prod.

### Missing
- No hay tests de integración con Testcontainers + PostgreSQL real — H2 no replica el comportamiento exacto de PostgreSQL (tipos, constraints, funciones).
- Faltan índices en foreign keys de alta cardinalidad: `bookings.user_id`, `bookings.field_id`, `center_roles.user_id`, `center_roles.center_id`.
- No hay migración para índice en `users.email` (columna usada en `findByEmail` y `existsByEmail`).

### OK
- Naming convention de Flyway seguido: `V1__init_schema.sql`, `V2__sample_data.sql`.
- Esquema correctamente normalizado con claves foráneas y CASCADE.
- Columnas de auditoría presentes en todas las tablas.
- CHECK constraint en `center_roles.role` validando el enum.
- `baseline-on-migrate: true` configurado en el perfil local.
- Dependencias `flyway-core` y `flyway-database-postgresql` presentes.

---

## Build (Gradle)

### Violations
- El proyecto usa **Maven** (`pom.xml`), no **Gradle Kotlin DSL** como especifican las convenciones del proyecto. Es la herramienta de build incorrecta según el estándar del equipo.
- `spring.threads.virtual.enabled=true` no está configurado en `application.yaml` — los virtual threads (Java 21 / Project Loom) no están activados.
- `spring.jpa.open-in-view` no está configurado explícitamente — el valor por defecto en Spring Boot es `true`, el anti-patrón que las convenciones del proyecto prohíben explícitamente.

### Missing
- No existe `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml` ni `gradle.properties`.
- No hay dependencia de **ArchUnit** para enforcement de reglas de arquitectura en CI.
- No hay dependencia de **Testcontainers** (`testcontainers:postgresql`, `testcontainers:junit-jupiter`).
- No hay dependencia de **Micrometer** ni **OpenTelemetry** para observabilidad.
- No hay plugin ni dependencia de **OpenAPI Generator**.
- No hay configuración de Java Toolchain (se usa `<source>`/`<target>` de Maven compiler).
- `hibernate.jdbc.batch_size` no está configurado — escrituras en batch desactivadas.

### OK
- Spring Boot 3.3.0 con Java 21 — versiones compatibles y recientes.
- Lombok correctamente en scope `provided`.
- `flyway-core` y `flyway-database-postgresql` presentes.
- H2 en scope `runtime` de test — aislado correctamente del classpath principal.
- `spring-boot-starter-validation` incluido.

---

## Tabla de prioridad

| # | Problema | Capa | Impacto | Esfuerzo |
|---|---|---|---|---|
| 1 | `UpdateUserResponse` expone el hash de contraseña en la respuesta REST | Web | Crítico | Bajo |
| 2 | `PasswordService` usa `"hashed_" + password` sin BCrypt — desplegable en producción | Aplicación | Crítico | Bajo |
| 3 | Ausencia total de `SecurityConfig` — ningún endpoint requiere autenticación | Web | Crítico | Alto |
| 4 | `CreateBookingUseCase` hace 3 lecturas + 1 escritura sin `@Transactional` — no atómico | Aplicación | Alto | Bajo |
| 5 | `@Transactional(readOnly = true)` ausente en todos los casos de uso de lectura y repositorios | Aplicación | Alto | Bajo |
| 6 | `CenterRepositoryImpl` y `FieldRepositoryImpl` con 5 métodos sin implementar (retornan vacío/false) | Persistencia | Alto | Medio |
| 7 | `open-in-view` no configurado como `false` — N+1 latente en cualquier endpoint con lazy relations | Build/Config | Alto | Bajo |
| 8 | Tests usan H2 + `ddl-auto: create-drop` en lugar de Testcontainers + PostgreSQL real | Base de datos | Alto | Medio |
| 9 | `EmailAlreadyExistsException` tiene `@ResponseStatus` en capa de dominio — acopla dominio a HTTP | Dominio | Medio | Bajo |
| 10 | `CreateUserRequest`, `CreateCenterRequest`, `CreateFieldRequest` sin validaciones `@NotBlank`/`@Email` | Web | Medio | Bajo |
| 11 | `GlobalExceptionHandler` no usa `ProblemDetail` (RFC 9457) — retorna `Map` ad-hoc | Web | Medio | Bajo |
| 12 | `Booking.startTime`/`endTime` son `String` en dominio — conversión frágil vía `Instant.parse()` | Dominio | Medio | Medio |
| 13 | `modifiedBy`/`deletedBy` siempre `null` — sin propagación del usuario autenticado a use cases | Aplicación | Medio | Alto |
| 14 | No hay paginación en `GetAllUsersUseCase` — lista sin acotar | Aplicación | Medio | Bajo |
| 15 | API sin versionado — rutas directas `/users`, `/bookings` en lugar de `/api/v1/` | Web | Medio | Medio |
| 16 | `FieldController` vacío — sin endpoints de consulta de pistas | Web | Medio | Medio |
| 17 | `CenterJpaRepository` y `FieldJpaRepository` sin métodos custom — backing JPA ausente | Persistencia | Medio | Bajo |
| 18 | Virtual threads no habilitados (`spring.threads.virtual.enabled=true` ausente) | Build/Config | Medio | Bajo |
| 19 | `V2__sample_data.sql` mezcla datos de prueba en migraciones de producción | Base de datos | Medio | Bajo |
| 20 | Build usa Maven en lugar de Gradle Kotlin DSL (convenciones del proyecto) | Build | Bajo | Alto |
| 21 | `FieldCommandMapper` existe pero no se usa — `CreateFieldUseCase` construye el objeto manualmente | Aplicación | Bajo | Bajo |
| 22 | Faltan índices en FKs de alta cardinalidad (`bookings.user_id`, `center_roles.user_id`, `users.email`) | Base de datos | Bajo | Bajo |
| 23 | No hay ArchUnit tests para enforcement de reglas de capas en CI | Build | Bajo | Medio |
| 24 | No hay dependencias de observabilidad (Micrometer Prometheus, OpenTelemetry) | Build | Bajo | Medio |
| 25 | `AuditableResponse.lastModifiedBy` inconsistente con el campo `modifiedBy` del dominio | Web | Bajo | Bajo |

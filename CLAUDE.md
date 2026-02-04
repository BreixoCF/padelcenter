# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.3.0 application for managing padel center bookings, built with Java 21 using hexagonal/clean architecture. The application handles users, centers, fields (courts), and booking management with role-based access control.

## Build & Run Commands

### Building the application
```bash
mvn clean install
```

### Running the application
```bash
mvn spring-boot:run
```

The application starts on port 8080 by default with the `local` profile.

### Running tests
```bash
mvn test
```

### Run a single test class
```bash
mvn test -Dtest=PadelCenterApplicationTests
```

## Database Setup

The application uses PostgreSQL (primary) with H2 available for testing. Flyway manages schema migrations automatically on startup.

### Local PostgreSQL connection (application-local.yaml)
- URL: `jdbc:postgresql://localhost:5432/padelcenterdb`
- Username: `padeluser`
- Password: `padelpassword`

Override via environment variables:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Database migrations
Located in `src/main/resources/db/migration/`:
- `V1__init_schema.sql` - Creates all tables (users, centers, fields, bookings, etc.)
- `V2__sample_data.sql` - Populates test data (25 users, 10 centers, 40 fields, 100 bookings)

Flyway automatically applies migrations on startup. To manually trigger migrations:
```bash
mvn flyway:migrate
```

## Architecture

The codebase follows **Hexagonal Architecture (Ports & Adapters)** with clear layer separation:

### Layer Structure

```
domain/                    - Core business logic (entities, repositories, exceptions)
  ├── model/              - Immutable domain models (Java records)
  ├── exception/          - Domain-specific exceptions
  └── repository/         - Repository interfaces (ports)

application/               - Use cases and business operations
  ├── command/            - Command DTOs (write operations)
  ├── query/              - Query DTOs (read operations)
  ├── shared/             - Generic use case interfaces (CommandUseCase, QueryUseCase)
  ├── create/             - Create use cases
  ├── read/               - Read use cases
  ├── update/             - Update use cases
  ├── delete/             - Delete use cases
  └── mapper/             - Command <-> Domain model mappers

infrastructure/            - Adapters for external systems
  ├── inbound/            - REST API layer
  │   ├── web/           - Controllers (@RestController)
  │   ├── dto/           - Request/Response DTOs
  │   └── mapper/        - HTTP DTO <-> Command/Domain mappers
  └── outbound/           - Persistence layer
      └── db/
        ├── entity/       - JPA entities
        ├── mapper/       - JPA Entity <-> Domain model mappers
        └── repository/   - Repository implementations
```

### Data Flow Pattern

Request flow follows this transformation chain:
```
HTTP Request
  → Controller (REST endpoint)
    → API Mapper: Request DTO → Command
      → Use Case (Application Service)
        → Command Mapper: Command → Domain Model
          → Repository Interface
            → Repository Implementation
              → JPA Mapper: Domain Model → JPA Entity
                → JPA Repository → Database
              ← JPA Mapper: JPA Entity → Domain Model
            ← Domain Model
        ← Domain Model
      ← API Mapper: Domain Model → Response DTO
    ← Response DTO
  ← HTTP Response
```

### Key Architectural Patterns

1. **CQRS-style Use Cases**: Commands for writes, Queries for reads
   - Generic interfaces: `CommandUseCase<C, R>` and `QueryUseCase<Q, R>`
   - Examples: CreateUserUseCase, UpdateUserUseCase, GetAllUsersUseCase

2. **Multi-layer Mapping**: Three distinct mapper layers maintain clean boundaries
   - API Mappers: HTTP layer ↔ Application layer
   - Command Mappers: Application layer ↔ Domain layer
   - Persistence Mappers: Domain layer ↔ Database layer

3. **Immutable Domain Models**: All domain entities are Java records
   - Cannot be mutated directly; create new instances with updated values
   - Example: `User.updateRoles()` returns a new User record

4. **Repository Pattern**: Domain defines interfaces, infrastructure implements
   - Domain: `UserRepository` interface
   - Database: `UserJpaRepository` (Spring Data), `UserRepositoryImpl` adapter

## Domain Model

### Core Entities (all are immutable Java records)

**User** - System users with multi-center roles
- Fields: id (UUID), firstName, lastName, email, passwordHash, phoneNumber, centerRoles, audit
- A user can have different roles in different centers via CenterRole

**Center** - Padel facilities/venues
- Fields: id (UUID), name, address, city, phoneNumber, email, manager (User), audit
- Contains multiple Field entities

**Field** - Individual courts within a center
- Fields: id (UUID), name, type, pricePerHour, isAvailable, center (Center), audit

**Booking** - Field reservations
- Fields: id (Long), user, field, startTime, endTime, totalPrice, bookedAt, status, audit
- Status: PENDING, CONFIRMED, CANCELLED, COMPLETED

**BookingParticipant** - Join entity for booking participants
- Fields: id (Long), booking, participantUser, audit

**CenterRole** - User's role in a specific center (many-to-many)
- Fields: centerId (UUID), role (Role enum), audit
- Role enum: ADMIN, MANAGER, USER

**Auditable** - Audit trail on all entities
- Fields: createdBy, createdAt, modifiedBy, modifiedAt, deletedBy, deletedAt

### Entity Relationships
```
User ─(1:Many)→ CenterRole
User ─(1:Many)→ Booking
User ─(1:1)→ Center (as manager)
Center ─(1:Many)→ Field
Center ─(1:Many)→ CenterRole
Field ─(1:Many)→ Booking
Booking ─(1:Many)→ BookingParticipant
```

## REST API Endpoints

Base URL: `http://localhost:8080`

### Users (`/users`)
- `GET /users` - List all users
- `POST /users` - Create user
- `PUT /users/{id}` - Update user details
- `PATCH /users/{id}/roles` - Update user's center roles

### Centers (`/centers`)
- `POST /centers` - Create center
- `POST /centers/{centerId}/fields` - Create field in a center

### HTTP Testing
Use IntelliJ HTTP Client with `src/test/http/padelcenter.http` for manual API testing.
Environment variables configured in `src/test/http/http-client.env.json`.

## Error Handling

Global exception handler (`GlobalExceptionHandler`) catches:
- `ResourceNotFoundException` → 404 Not Found
- `MethodArgumentNotValidException` → 400 Bad Request (with validation errors)
- `DataIntegrityViolationException` → 409 Conflict (constraint violations)
- Generic exceptions → 500 Internal Server Error

## Important Conventions

### When adding new features

1. **Start with the domain layer**: Define immutable record models in `domain/model/`
2. **Define repository interface**: Add port in `domain/repository/`
3. **Create use case**: Add Command/Query DTO in `application/command/` or `application/query/`
4. **Implement use case**: Create handler in `application/{create,read,update,delete}/`
5. **Add REST endpoint**: Create controller in `infrastructure/inbound/web/`
6. **Create DTOs**: Add Request/Response in `infrastructure/inbound/dto/`
7. **Implement repository**: Add JPA entity, mapper, and repository impl in `infrastructure/outbound/db/`
8. **Database migration**: Add Flyway migration script in `db/migration/`

### Mapper naming pattern
- API Mappers: `{Entity}ApiMapper` (e.g., UserApiMapper)
- Command Mappers: `{Entity}CommandMapper` (e.g., UserCommandMapper)
- Persistence Mappers: `{Entity}PersistenceMapper` (e.g., UserPersistenceMapper)

### Validation
- Request DTOs use Jakarta validation annotations (`@NotNull`, `@Email`, `@Size`, etc.)
- Domain validation logic goes in use cases
- Controllers use `@Valid` on request bodies

### Audit tracking
All entities extend `AuditableEntity` (JPA) or include `Auditable` (domain).
Spring JPA Auditing (`@EnableJpaAuditing`) auto-populates created/modified timestamps and user IDs.

## Technology Stack

- **Java 21** (target/source compiler version)
- **Spring Boot 3.3.0** with Spring Data JPA
- **PostgreSQL** (production database)
- **H2** (in-memory for testing)
- **Flyway** (database migrations)
- **Lombok** (code generation - getters, constructors, etc.)
- **JSpecify** (nullness annotations)
- **Maven** (build tool)

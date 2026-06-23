# padelcenter

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)](https://www.postgresql.org/)

Backend de gestión de centros de pádel: reservas, pistas, centros y usuarios.
Construido con Java 21, Spring Boot 3.4 y arquitectura hexagonal.

## Requisitos previos

- [Docker](https://docs.docker.com/get-docker/) + Docker Compose (arranque rápido)
- Java 21 y Maven 3.9+ (solo si quieres ejecutar fuera de Docker)

## Arranque rápido con Docker

```bash
cd tools/docker
docker compose up -d --build
```

Esto levanta PostgreSQL y compila + ejecuta el backend en un único paso
(el `Dockerfile` hace un build multi-stage con Maven, no requiere tener
Maven instalado en el host). Las migraciones de Flyway se aplican
automáticamente al arrancar.

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health check: http://localhost:8080/actuator/health

Para parar y borrar los datos:

```bash
docker compose down -v
```

## Arranque en local (sin Docker para el backend)

```bash
# 1. Levantar solo la base de datos
cd tools/docker
docker compose up -d postgres

# 2. Ejecutar la aplicación (perfil local)
cd ../..
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. Swagger UI disponible en:
# http://localhost:8080/swagger-ui/index.html
```

## Cómo trabajar con la API (API-first)

El contrato vive en `docs/openapi/index.yaml`. Es la fuente de verdad.
Nunca añadas endpoints directamente en los controladores.

```bash
# 1. Edita el contrato en docs/openapi/
# 2. Regenera las interfaces y DTOs
mvn generate-sources
# 3. Implementa el nuevo método en el @RestController correspondiente
```

Ver [ADR-002](docs/adr/ADR-002-api-first-openapi.md) para la motivación
de este flujo.

## Tests

```bash
# Unit tests + ArchUnit (sin Docker)
mvn test -Dtest="*Test,ArchitectureTest"

# Integration tests (requiere Docker, usa Testcontainers)
mvn test -Dtest="*IT"

# Suite completa
mvn verify
```

## Generar token JWT local para pruebas

```bash
# Payload mínimo:
# { "sub": "UUID-del-usuario", "roles": ["ADMIN"], "exp": ... }
# Secret: valor de app.jwt.secret en application.yaml
#         (por defecto: padelcenter-dev-secret-key-change-in-production-min32chars!!)
```

Ver [docs/dev/jwt-local-example.md](docs/dev/jwt-local-example.md) para
ejemplos completos con curl y jwt.io.

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://localhost:5432/padelcenterdb` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos | `app` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de base de datos | `app` |
| `JWT_SECRET` | Secret HMAC-SHA256 para firmar tokens (min. 32 caracteres) | `padelcenter-dev-secret-key-change-in-production-min32chars!!` |
| `JWT_ACCESS_TOKEN_EXPIRY` | Expiración del access token, en segundos | `900` |
| `JWT_REFRESH_TOKEN_EXPIRY` | Expiración del refresh token, en segundos | `604800` |

Los valores por defecto son válidos para desarrollo local; en producción
hay que sobrescribir al menos `JWT_SECRET`.

## Estructura del proyecto

```
src/
├── main/java/com/bookings/padelcenter/
│   ├── domain/          # Modelo de negocio puro (records, excepciones, ports)
│   ├── application/     # Casos de uso
│   └── infrastructure/
│       ├── inbound/     # REST controllers, mappers, security resolver
│       ├── outbound/    # JPA repositories, entidades, mappers de persistencia
│       └── config/      # SecurityConfig, LocalSecurityConfig
└── test/
    ├── application/     # Unit tests (Mockito, sin Spring)
    ├── architecture/     # Reglas ArchUnit
    └── infrastructure/  # ITs por controlador (Testcontainers PostgreSQL)

docs/
├── openapi/             # Contrato API-first (fuente de verdad)
├── adr/                 # Architecture Decision Records
├── diagrams/            # ERD y otros diagramas
└── dev/                 # Guías de desarrollo local

tools/
├── docker/              # Dockerfile + docker-compose para entorno local
└── postman/             # Colección Postman de la API
```

## Arquitectura

Arquitectura hexagonal: `domain` no depende de ningún framework externo,
`application` orquesta los casos de uso contra los puertos del dominio,
e `infrastructure` implementa esos puertos (REST, JPA, seguridad).

Las decisiones de diseño están documentadas como ADRs:

- [ADR-001 — Arquitectura hexagonal](docs/adr/ADR-001-hexagonal-architecture.md)
- [ADR-002 — API-first con OpenAPI](docs/adr/ADR-002-api-first-openapi.md)
- [ADR-003 — Participantes de reserva diferidos](docs/adr/ADR-003-booking-participants-deferred.md)
- [ADR-004 — Modelo de torneo](docs/adr/ADR-004-tournament-model.md)

El diagrama entidad-relación de la base de datos está en
[docs/diagrams/erd-domain.md](docs/diagrams/erd-domain.md).

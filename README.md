# padelcenter

Backend de gestión de centros de pádel: reservas, pistas, centros y usuarios.
Construido con Java 21, Spring Boot 3.4 y arquitectura hexagonal.

## Requisitos previos

- Java 21
- Docker (para tests de integración y entorno local)
- Maven 3.9+

## Arranque en local

```bash
# 1. Levantar infraestructura
docker compose up -d postgres

# 2. Ejecutar la aplicación (perfil local)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 3. Swagger UI disponible en:
# http://localhost:8080/swagger-ui.html
```

La base de datos se inicializa automáticamente mediante Flyway al arrancar.

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

# Integration tests (requiere Docker)
mvn test -Dtest="*IT"

# Suite completa
mvn verify
```

Estado actual: **126 unit + ArchUnit tests** pasan sin Docker.
Los ITs requieren Docker para levantar PostgreSQL via Testcontainers.

## Generar token JWT local para pruebas

```bash
# Payload mínimo:
# { "sub": "UUID-del-usuario", "roles": ["ADMIN"], "exp": ... }
# Secret: valor de jwt.secret en application-local.yaml
#         (por defecto: local-dev-secret-32-chars-min!!)
```

Ver [docs/dev/jwt-local-example.md](docs/dev/jwt-local-example.md) para
ejemplos completos con curl y jwt.io.

## Variables de entorno

| Variable | Descripción | Valor por defecto (local) |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://localhost:5432/padelcenterdb` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de base de datos | `padeluser` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de base de datos | `padelpassword` |
| `JWT_SECRET` | Secret HMAC-SHA256 para tokens locales | `local-dev-secret-32-chars-min!!` |
| `JWT_ISSUER_URI` | URI del proveedor OIDC (producción) | — |
| `JWT_JWK_SET_URI` | URI del JWKS del proveedor (producción) | — |

## Estructura del proyecto

```
src/
├── main/java/com/bookings/padelcenter/
│   ├── domain/          # Modelo de negocio puro (records, excepciones, ports)
│   ├── application/     # Casos de uso (17 use cases)
│   └── infrastructure/
│       ├── inbound/     # REST controllers, mappers, security resolver
│       ├── outbound/    # JPA repositories, entidades, mappers de persistencia
│       └── config/      # SecurityConfig, LocalSecurityConfig
└── test/
    ├── application/     # 121 unit tests (Mockito, sin Spring)
    ├── architecture/    # 5 reglas ArchUnit
    └── infrastructure/  # ITs por controlador (Testcontainers PostgreSQL)

docs/
├── openapi/             # Contrato API-first (fuente de verdad)
├── adr/                 # Architecture Decision Records
├── diagrams/            # ERD y otros diagramas
└── dev/                 # Guías de desarrollo local
```

## Arquitectura

Las decisiones de diseño están documentadas como ADRs:

- [ADR-001 — Arquitectura hexagonal](docs/adr/ADR-001-hexagonal-architecture.md)
- [ADR-002 — API-first con OpenAPI](docs/adr/ADR-002-api-first-openapi.md)

El diagrama entidad-relación de la base de datos está en
[docs/diagrams/erd-domain.md](docs/diagrams/erd-domain.md).

# PadelCenter

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-black)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)](https://www.postgresql.org/)

> Trabajo Fin de Grado — Grao en Enxeñaría Informática, mención Enxeñaría do
> Software, [Universidade da Coruña](https://www.udc.es/).

Aplicación web para la gestión integral de reservas de pistas de pádel y la
organización de torneos en centros deportivos.

## Índice

- [Qué es PadelCenter](#qué-es-padelcenter)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Arranque rápido](#arranque-rápido)
- [Arquitectura](#arquitectura)
- [Memoria](#memoria)
- [Autoría](#autoría)

## Qué es PadelCenter

La digitalización de los clubes deportivos es una necesidad creciente en el
contexto del auge del pádel en España. La mayoría de los centros siguen
gestionando sus reservas de forma manual o con herramientas genéricas que no
cubren sus necesidades específicas. PadelCenter ofrece una plataforma integral
para resolverlo:

- Autenticación segura mediante JWT.
- Gestión de centros deportivos y pistas.
- Creación y consulta de reservas con control de solapamiento de horarios.
- Organización de torneos con generación automática de emparejamientos
  (round-robin, eliminación directa, grupos + eliminatoria).

**Palabras clave:** arquitectura hexagonal, Spring Boot, Next.js, API REST,
OpenAPI, gestión deportiva, reservas, torneos.

## Estructura del repositorio

Monorepo con los tres componentes del proyecto, cada uno con su historial de
commits original (importado vía `git subtree`):

| Carpeta | Contenido | Stack |
|---|---|---|
| [`backend/`](backend/) | API REST, lógica de negocio | Java 21 · Spring Boot 3.4 · PostgreSQL · arquitectura hexagonal |
| [`web/`](web/) | Aplicación web | Next.js 14 (App Router) · TypeScript · React Query |
| [`memoria/`](memoria/) | Memoria del TFG | LaTeX |

## Arranque rápido

Requiere [Docker](https://docs.docker.com/get-docker/) + Docker Compose. Un
único `docker-compose.yaml` en la raíz levanta los tres servicios (PostgreSQL,
backend y web) conectados entre sí:

```bash
docker compose up -d --build
```

- Web: http://localhost:3000
- API: http://localhost:8080 — Swagger UI en `/swagger-ui/index.html`

Para parar y borrar los datos:

```bash
docker compose down -v
```

Si solo quieres levantar el backend (sin frontend), usa el `docker-compose.yaml`
específico en `backend/tools/docker/`. Instrucciones detalladas, variables de
entorno y arranque sin Docker en el README de cada subproyecto:
[backend/README.md](backend/README.md), [web/README.md](web/README.md).

## Arquitectura

El backend sigue **arquitectura hexagonal** (`domain` → `application` →
`infrastructure`) con un contrato de API **contract-first** definido en
OpenAPI antes de implementar cualquier endpoint. El frontend consume esa API
mediante un cliente generado automáticamente con [Orval](https://orval.dev/).

Las decisiones de diseño están documentadas como ADRs:

- [ADR-001 — Arquitectura hexagonal](backend/docs/adr/ADR-001-hexagonal-architecture.md)
- [ADR-002 — API-first con OpenAPI](backend/docs/adr/ADR-002-api-first-openapi.md)
- [ADR-003 — Participantes de reserva diferidos](backend/docs/adr/ADR-003-booking-participants-deferred.md)
- [ADR-004 — Modelo de torneo](backend/docs/adr/ADR-004-tournament-model.md)

Esquema de base de datos: [ERD](backend/docs/diagrams/erd-domain.md). Contrato
de API: [`docs/openapi/index.yaml`](backend/docs/openapi/index.yaml).

## Memoria

Fuente LaTeX en [`memoria/`](memoria/), PDF compilado en
[`memoria/memoria_tfg.pdf`](memoria/memoria_tfg.pdf).

```bash
cd memoria
latexmk -xelatex memoria_tfg.tex
```

## Autoría

- **Estudiante:** Breixo Camiña Fernández
- **Dirección:** Javier Parapar López, Gilberto Pérez Vega
- **Centro:** Facultade de Informática, Universidade da Coruña

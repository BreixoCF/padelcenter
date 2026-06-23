# ADR-003 — BookingParticipant feature deferred

**Date:** 2026-04-16  
**Status:** Deferred  
**Deciders:** Breixo Camiña Fernández

---

## Context

The `BookingParticipant` aggregate (domain record + JPA entity) was scaffolded
to support the concept of adding multiple participants to a single booking —
useful for doubles matches where up to 4 players share one court reservation.

At this point the feature is incomplete:

- No `BookingParticipantRepository` port exists.
- No use cases (`AddParticipantUseCase`, `RemoveParticipantUseCase`) exist.
- No REST endpoints are defined in the OpenAPI spec.
- The entity is persisted to a `booking_participants` table but nothing writes
  or reads from it through the application layer.

## Decision

Keep the scaffolding (`BookingParticipant.java`, `BookingParticipantEntity.java`)
and defer full implementation. Removing the entity now would require a Flyway
migration to drop the table, which adds unnecessary risk before the feature is
prioritised on the roadmap.

## Consequences

- The `booking_participants` table exists in the schema but remains empty at runtime.
- The domain record and JPA entity will accrue drift risk until the feature is
  implemented.
- When the feature is picked up, the following work is required:
  1. `BookingParticipantRepository` port (domain layer).
  2. `BookingParticipantJpaRepository` + `BookingParticipantPersistenceMapper` (infra layer).
  3. `AddParticipantUseCase` / `RemoveParticipantUseCase` (application layer).
  4. `PATCH /api/v1/bookings/{id}/participants` endpoint + OpenAPI schema.
  5. Unit + integration tests.

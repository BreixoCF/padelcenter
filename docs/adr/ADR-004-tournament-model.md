# ADR-004 — Tournament domain model

**Date:** 2026-04-17
**Status:** Accepted
**Deciders:** Breixo Camiña Fernández

---

## Context

The system needs to support padel doubles tournaments within a center. Requirements
gathered from the product brief:

- Three bracket formats: round robin, single elimination, groups + elimination.
- Registration by pairs (2 players per pair).
- Open registration with manual confirmation by the organizer.
- Automatic bracket generation when registration is closed.
- Match results reported by the players themselves.
- Matches are logistically independent from court bookings — a tournament match
  does not automatically create a `Booking`. Courts are coordinated externally.

The key design tension is whether Tournament should own its matches as an embedded
collection (one large aggregate) or whether Match should be a separate aggregate
root referenced by ID.

---

## Domain model

### `Tournament` — aggregate root

| Field | Type | Notes |
|-------|------|-------|
| `tournamentId` | `UUID` | UUID v7, time-ordered |
| `centerId` | `UUID` | Reference to owning center (ID only, no object) |
| `name` | `String` | |
| `description` | `String` | Nullable |
| `format` | `TournamentFormat` | `ROUND_ROBIN`, `ELIMINATION`, `GROUPS_AND_ELIMINATION` |
| `status` | `TournamentStatus` | See lifecycle below |
| `maxPairs` | `int` | |
| `startDate` | `LocalDate` | |
| `endDate` | `LocalDate` | |
| `audit` | `Auditable` | Created/modified/deleted timestamps |

**Status lifecycle:**

```
DRAFT → REGISTRATION_OPEN → REGISTRATION_CLOSED → IN_PROGRESS → COMPLETED
                                                              ↘ CANCELLED (from any state)
```

**Invariants enforced in the domain:**

- `REGISTRATION_OPEN` may only be entered from `DRAFT`.
- Bracket generation (`IN_PROGRESS`) may only be triggered from `REGISTRATION_CLOSED`.
- For `ELIMINATION`: `maxPairs` must be a power of 2 (2, 4, 8, 16…).
- For `ROUND_ROBIN`: `maxPairs` must be ≥ 4.
- A `Tournament` does not hold a `List<Match>` — matches are queried via `MatchRepository`.

---

### `TournamentPair` — entity within `Tournament`

| Field | Type | Notes |
|-------|------|-------|
| `pairId` | `UUID` | UUID v7 |
| `tournamentId` | `UUID` | Parent tournament |
| `player1Id` | `UUID` | References `User` by ID |
| `player2Id` | `UUID` | References `User` by ID |
| `status` | `PairStatus` | `PENDING`, `CONFIRMED`, `REJECTED` |
| `registeredAt` | `Instant` | |

**Invariants:**

- A user may not appear in two pairs within the same tournament (checked across both `player1Id` and `player2Id`).
- Pair confirmation/rejection is only permitted while `Tournament.status = REGISTRATION_OPEN`.
- `player1Id ≠ player2Id` — a user cannot pair with themselves.

`TournamentPair` is part of the `Tournament` aggregate: it is loaded, validated,
and saved through `Tournament` via `TournamentRepository`. It does not have its
own aggregate root.

---

### `Match` — aggregate root (separate)

| Field | Type | Notes |
|-------|------|-------|
| `matchId` | `UUID` | UUID v7 |
| `tournamentId` | `UUID` | Reference by ID only |
| `pairAId` | `UUID` | Nullable — TBD for future elimination rounds |
| `pairBId` | `UUID` | Nullable — TBD for future elimination rounds |
| `round` | `int` | 1-based round number |
| `groupName` | `String` | Nullable — only used in `GROUPS_AND_ELIMINATION` |
| `scheduledAt` | `LocalDateTime` | Nullable — set manually |
| `status` | `MatchStatus` | `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `result` | `MatchResult` | Nullable — only present when `COMPLETED` |

---

### `MatchResult` — value object within `Match`

| Field | Type | Notes |
|-------|------|-------|
| `winnerPairId` | `UUID` | Must be `pairAId` or `pairBId` |
| `scoreA` | `String` | E.g. `"6-3 6-4"` — free-form for MVP |
| `scoreB` | `String` | |
| `reportedBy` | `UUID` | Must be `player1Id` or `player2Id` of either pair |
| `reportedAt` | `Instant` | |

**Invariants:**

- A result can only be reported when `Match.status` is `SCHEDULED` or `IN_PROGRESS`.
- `reportedBy` must be one of the four players of the two pairs.
- Reporting a result transitions `Match.status` to `COMPLETED`.

---

## Bracket generation algorithms

Bracket generation is implemented as **domain services** (pure functions with no
Spring dependencies), invoked by a `GenerateBracketUseCase` in the application
layer. They receive the confirmed pairs list and produce a `List<Match>` to be
persisted in bulk.

### Round Robin

- Every pair plays every other pair exactly once.
- Total matches: `n × (n − 1) / 2`.
- All matches are created in a single round (`round = 1`).
- All matches have `pairAId` and `pairBId` assigned immediately.

### Elimination (single bracket)

- Precondition: number of confirmed pairs is a power of 2.
- Round 1: pairs are shuffled randomly; `n / 2` matches created with both pairs assigned.
- Rounds 2…log₂(n): matches created as placeholders (`pairAId = null`, `pairBId = null`, `status = SCHEDULED`).
- As each match in round `r` completes, the winner is assigned to the corresponding
  match in round `r + 1`. This assignment is triggered by the `ReportMatchResultUseCase`.

### Groups + Elimination

- Confirmed pairs are divided into groups of 4 (groups named A, B, C…).
- Group phase: round robin within each group (`groupName` set, `round = 1`).
  Total group matches: `numGroups × 6`.
- Elimination phase: the top 2 pairs from each group advance; bracket follows
  the single elimination algorithm with the qualified pairs.
- Group standings are computed by the `ComputeGroupStandingsUseCase` (wins, sets,
  games) before generating the elimination bracket.

---

## Decision

Adopt the model above with **`Tournament` and `Match` as separate aggregate roots**.
`Match` references `Tournament` and `TournamentPair` by UUID only — no object
references cross aggregate boundaries.

Bracket generation is a **domain service** (`RoundRobinBracketService`,
`EliminationBracketService`, `GroupsAndEliminationBracketService`) called from
the application layer, not embedded in the `Tournament` aggregate.

---

## Consequences

### Positive

- `Tournament` stays small regardless of the number of matches (10 or 500).
- `Match` can evolve independently: add referee assignment, live scoring,
  or statistics without touching the `Tournament` aggregate.
- Bracket generation services are pure functions — trivial to unit-test with
  no mocks.
- Queries like "all matches in round 2" or "all matches for pair X" are
  straightforward repository queries.

### Negative

- Fetching the full picture of a tournament (pairs + all matches + standings)
  requires multiple repository calls; the application layer must orchestrate them.
- Consistency between `Tournament.status = IN_PROGRESS` and the existence of
  `Match` records requires coordination in `GenerateBracketUseCase` — there is
  no database-level constraint enforcing it.
- Advancing elimination brackets (assigning the winner to the next round's match)
  must be triggered explicitly in `ReportMatchResultUseCase`; it does not happen
  automatically via cascade.

---

## Alternatives considered

### A — Tournament as single aggregate with embedded Match list

`Tournament` would hold `List<Match>` and be the only aggregate root.

**Rejected** because a tournament with 64 pairs in a round robin generates 2016
matches. Loading the full aggregate on every state transition becomes prohibitively
expensive, and optimistic locking conflicts on `Tournament.version` would be
frequent during concurrent result reporting.

### B — MatchResult as a separate aggregate root

`MatchResult` would have its own identity, repository, and lifecycle, decoupled
from `Match`.

**Rejected** as over-engineering for the MVP. A result has no independent
lifecycle beyond its match; splitting it adds complexity without benefit until
result disputes or arbitration workflows are required.

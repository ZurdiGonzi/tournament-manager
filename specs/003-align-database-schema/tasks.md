# Tasks: Alineación del Esquema Relacional PostgreSQL y Migraciones Flyway

**Input**: Design documents from `specs/003-align-database-schema/` (`spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/flyway-v1-contract.sql`, `quickstart.md`)

**Prerequisites**: `plan.md` (required), `spec.md` (required), `data-model.md`, `contracts/`

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., [US1], [US2], [US3])
- File paths are relative to repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, Flyway dependencies and configuration

- [x] T001 Add Flyway and PostgreSQL driver dependencies (`org.flywaydb:flyway-core`, `org.flywaydb:flyway-database-postgresql`, `org.postgresql:postgresql`) to `pom.xml`
- [x] T002 [P] Create Flyway migration directory tree under `src/main/resources/db/migration`
- [x] T003 [P] Create and configure Spring Boot DataSource and Flyway settings in `src/main/resources/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Documentation synchronization and baseline schema preparation

**⚠️ CRITICAL**: Must complete before user story implementation

- [x] T004 [P] Update baseline contract artifact `specs/001-store-tournament-data-model/contracts/database-ddl-contract.sql` with `users.email UNIQUE`, expanded formats, optional budget and aligned statuses to preserve architectural history
- [x] T005 [P] Verify that the target migration file `src/main/resources/db/migration/V1__init_schema.sql` follows Flyway naming conventions (`V1__init_schema.sql`)

**Checkpoint**: Foundation ready - dependencies and configuration in place; schema definition can proceed.

---

## Phase 3: User Story 1 - Registro de Identidad con Correo Único y Torneos de Presupuesto Flexible (Priority: P1) 🎯 MVP

**Goal**: Garantizar la persistencia del email único en `users` y permitir torneos con presupuesto opcional (`NULL`) o estrictamente positivo (`> 0`).

**Independent Test**: Verificar sintácticamente la definición de `users` con `email VARCHAR(255) NOT NULL UNIQUE` y `tournaments` con `max_deck_budget NUMERIC(10, 2) CHECK (max_deck_budget IS NULL OR max_deck_budget > 0)`.

### Implementation for User Story 1

- [x] T006 [P] [US1] Define `users` table DDL with `email VARCHAR(255) NOT NULL UNIQUE` and `username VARCHAR(60) NOT NULL UNIQUE` in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T007 [P] [US1] Define `tournament_templates` table DDL with `max_deck_budget NUMERIC(10, 2) CHECK (max_deck_budget IS NULL OR max_deck_budget > 0)` and `max_players >= 4` in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T008 [US1] Define `tournaments` table DDL with `max_deck_budget NUMERIC(10, 2) CHECK (max_deck_budget IS NULL OR max_deck_budget > 0)` and `uq_tournament_template_schedule UNIQUE (template_id, scheduled_at)` in `src/main/resources/db/migration/V1__init_schema.sql`

**Checkpoint**: User Story 1 DDL rules for email and flexible budget are defined and independently verifiable.

---

## Phase 4: User Story 2 - Soporte de Formatos Ampliados y Normalización de Estados (Priority: P1)

**Goal**: Soportar los formatos `STANDARD`, `COMMANDER`, `MODERN`, `PAUPER` y alinear los estados de partida (`PENDING`, `SUBMITTED`, `CONFIRMED`, `DISPUTED`) e inscripción (`CONFIRMED`, `CANCELLED`, `DROPPED`) 1:1 con Java 21.

**Independent Test**: Comprobar que las restricciones `CHECK` en `leagues`, `tournaments`, `tournament_templates`, `matches` y `tournament_registrations` admiten exactamente el conjunto cerrado de valores del dominio.

### Implementation for User Story 2

- [x] T009 [P] [US2] Define `leagues` table DDL with `CHECK (format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER'))` in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T010 [P] [US2] Update format constraint to `CHECK (format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER'))` for `tournaments` and `tournament_templates` in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T011 [P] [US2] Define `matches` table DDL with `CHECK (status IN ('PENDING', 'SUBMITTED', 'CONFIRMED', 'DISPUTED'))` and `player2_id` nullable (BYE support) in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T012 [US2] Define `tournament_registrations` table DDL with `CHECK (status IN ('CONFIRMED', 'CANCELLED', 'DROPPED'))` and `deck_snapshot JSONB NOT NULL` in `src/main/resources/db/migration/V1__init_schema.sql`

**Checkpoint**: User Stories 1 AND 2 rules for formats, flexible budget, and aligned statuses are fully specified.

---

## Phase 5: User Story 3 - Inicialización Automatizada mediante Migración Flyway (Priority: P2)

**Goal**: Ensamblar el script canónico completo de migración Flyway en `src/main/resources/db/migration/V1__init_schema.sql` con todas las tablas, vistas analíticas e índices especializados.

**Independent Test**: Verificar que `V1__init_schema.sql` contiene las 12 tablas relacionales, 2 vistas analíticas (`v_league_leaderboard`, `v_scryfall_card_stats`) y 12 índices relacionales y GIN.

### Implementation for User Story 3

- [x] T013 [P] [US3] Include remaining supporting tables (`stores`, `store_members`, `decks`, `league_standings`, `scryfall_cards`, `registration_deck_cards`, `rounds`) in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T014 [P] [US3] Include analytical views `v_league_leaderboard` and `v_scryfall_card_stats` in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T015 [US3] Include B-Tree indexes and GIN indexes (`deck_snapshot`, `legalities`, `colors`) in `src/main/resources/db/migration/V1__init_schema.sql`

**Checkpoint**: Full canonical Flyway migration script is assembled and ready for execution.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificación de integridad, consistencia de contratos y validación sintáctica

- [x] T016 Verify SQL DDL syntax and script completeness against PostgreSQL 15+ specifications in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T017 [P] Synchronize feature contract in `specs/003-align-database-schema/contracts/flyway-v1-contract.sql` to ensure byte-level match with `V1__init_schema.sql`
- [x] T018 Run quickstart validation review from `specs/003-align-database-schema/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - **BLOCKS** user story implementation.
- **User Stories (Phase 3+)**: Depend on Foundational (Phase 2).
  - US1 and US2 can run in parallel (different table sections).
  - US3 completes the full integrated script.
- **Polish (Phase 6)**: Depends on US1, US2, and US3 completion.

### Parallel Opportunities

- **Phase 1**: T002 and T003 can run in parallel after T001.
- **Phase 2**: T004 and T005 can run in parallel.
- **Phase 3 (US1)**: T006 and T007 can run in parallel.
- **Phase 4 (US2)**: T009, T010, and T011 can run in parallel.
- **Phase 5 (US3)**: T013 and T014 can run in parallel before index assembly in T015.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (`pom.xml` dependencies and directories).
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1 (Email & Budget DDL).
4. Verify user authentication and tournament budget constraints.

### Incremental Delivery

1. Setup + Foundational → Flyway configured.
2. US1 → Email único en `users` y presupuesto flexible.
3. US2 → Formatos `Modern`/`Pauper` y estados normalizados (`MatchStatus`, `RegistrationStatus`).
4. US3 → Script canónico integral `V1__init_schema.sql` con vistas analíticas e índices GIN.
5. Polish → Verificación final de sintaxis y contratos.

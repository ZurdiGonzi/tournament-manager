# Tasks: Configuración Personalizada de Pairings, Puntuación de Torneo y Ligas

**Input**: Design documents from `specs/004-custom-scoring-pairings/` (`spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/ddl-update-contract.sql`, `quickstart.md`)

**Prerequisites**: `plan.md` (required), `spec.md` (required), `data-model.md`, `contracts/`

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., [US1], [US2], [US3])
- File paths are relative to repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparación de estructuras y objetivos de actualización del DDL

- [x] T001 Review and verify target DDL file structure in `src/main/resources/db/migration/V1__init_schema.sql` and contracts directory

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Definición de Enums de dominio en Java 21 para los nuevos tipos cerrados de pairings y puntuación de ligas

**⚠️ CRITICAL**: Must complete before user story implementation

- [x] T002 [P] Create `PairingSystem` domain enum with values `SWISS`, `SINGLE_ELIMINATION`, `ROUND_ROBIN`, `CUSTOM` in `src/main/java/com/tournament/app/domain/model/PairingSystem.java`
- [x] T003 [P] Create `LeagueScoringSystem` domain enum with values `CUMULATIVE_MATCH_POINTS`, `RANK_BASED`, `PARTICIPATION_PLUS_RECORD`, `CUSTOM` in `src/main/java/com/tournament/app/domain/model/LeagueScoringSystem.java`

**Checkpoint**: Foundation ready - domain enums defined without framework dependencies; DDL enhancement can proceed.

---

## Phase 3: User Story 1 - Configuración Personalizada de Puntuación de Torneo (Priority: P1) 🎯 MVP

**Goal**: Permitir que el dueño de tienda defina puntos personalizados por victoria, empate, derrota y BYE (con valores por defecto 3/1/0/3) y modificadores en JSONB.

**Independent Test**: Verificar sintácticamente que `tournaments` y `tournament_templates` incorporan las columnas `points_per_win`, `points_per_draw`, `points_per_loss`, `points_per_bye` (con `CHECK >= 0`) y `scoring_rules JSONB`.

### Implementation for User Story 1

- [x] T004 [P] [US1] Add `points_per_win`, `points_per_draw`, `points_per_loss`, `points_per_bye` (all `INT NOT NULL CHECK (>= 0)`) and `scoring_rules JSONB` to `tournament_templates` table in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T005 [US1] Add `points_per_win`, `points_per_draw`, `points_per_loss`, `points_per_bye` (all `INT NOT NULL CHECK (>= 0)`) and `scoring_rules JSONB` to `tournaments` table in `src/main/resources/db/migration/V1__init_schema.sql`

**Checkpoint**: Tournament match scoring rules are fully configurable in both active tournaments and templates.

---

## Phase 4: User Story 2 - Elección y Parámetros del Sistema de Pairings (Priority: P1)

**Goal**: Permitir la selección del motor de emparejamientos (`SWISS`, `SINGLE_ELIMINATION`, `ROUND_ROBIN`, `CUSTOM`) y parámetros extensibles en JSONB (`pairing_config`).

**Independent Test**: Verificar que las columnas `pairing_system` y `pairing_config JSONB` existen en `tournaments` y `tournament_templates` con restricción de validación `CHECK`.

### Implementation for User Story 2

- [x] T006 [P] [US2] Add `pairing_system VARCHAR(30) NOT NULL DEFAULT 'SWISS' CHECK (pairing_system IN ('SWISS', 'SINGLE_ELIMINATION', 'ROUND_ROBIN', 'CUSTOM'))` and `pairing_config JSONB` to `tournament_templates` in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T007 [US2] Add `pairing_system VARCHAR(30) NOT NULL DEFAULT 'SWISS' CHECK (pairing_system IN ('SWISS', 'SINGLE_ELIMINATION', 'ROUND_ROBIN', 'CUSTOM'))` and `pairing_config JSONB` to `tournaments` in `src/main/resources/db/migration/V1__init_schema.sql`

**Checkpoint**: Tournament pairing algorithms and configuration JSONB are fully supported in database schema.

---

## Phase 5: User Story 3 - Sistema Personalizado de Puntuación y Ranking para Ligas (Priority: P2)

**Goal**: Incorporar a `leagues` el sistema de puntuación, puntos por participación, descarte de peores jornadas (`best_n_tournaments`), baremo por puesto en JSONB, y actualizar la vista `v_league_leaderboard`.

**Independent Test**: Verificar que `leagues` cuenta con las columnas de puntuación de temporada y que `v_league_leaderboard` computa los puntos de torneo junto a la bonificación de asistencia.

### Implementation for User Story 3

- [x] T008 [P] [US3] Add `scoring_system VARCHAR(30) NOT NULL DEFAULT 'CUMULATIVE_MATCH_POINTS' CHECK (scoring_system IN ('CUMULATIVE_MATCH_POINTS', 'RANK_BASED', 'PARTICIPATION_PLUS_RECORD', 'CUSTOM'))`, `participation_points INT NOT NULL DEFAULT 0 CHECK (participation_points >= 0)`, `best_n_tournaments INT CHECK (best_n_tournaments IS NULL OR best_n_tournaments > 0)`, and `rank_points_distribution JSONB` to `leagues` in `src/main/resources/db/migration/V1__init_schema.sql`
- [x] T009 [US3] Update SQL analytical view `v_league_leaderboard` in `src/main/resources/db/migration/V1__init_schema.sql` to compute `total_points` as `COALESCE(ls.total_points, SUM(tr.match_points) + (COUNT(DISTINCT tr.tournament_id) * l.participation_points), 0)` and expose breakdown columns

**Checkpoint**: League custom scoring and leaderboard aggregation are fully integrated into the database migration.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Sincronización de contratos y validación de sintaxis

- [x] T010 Synchronize design contract in `specs/001-store-tournament-data-model/contracts/database-ddl-contract.sql` with new pairing and scoring columns
- [x] T011 [P] Synchronize feature contract in `specs/003-align-database-schema/contracts/flyway-v1-contract.sql` and `specs/004-custom-scoring-pairings/contracts/ddl-update-contract.sql`
- [x] T012 Run quickstart validation review from `specs/004-custom-scoring-pairings/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - **BLOCKS** user story implementation.
- **User Stories (Phase 3+)**: Depend on Foundational (Phase 2).
  - US1 and US2 can run in parallel (different columns of tournaments/templates).
  - US3 handles leagues and updates the analytical view.
- **Polish (Phase 6)**: Depends on US1, US2, and US3 completion.

### Parallel Opportunities

- **Phase 2**: T002 and T003 can run in parallel.
- **Phase 3 (US1)**: T004 can run in parallel with T006.
- **Phase 4 (US2)**: T006 and T007.
- **Phase 5 (US3)**: T008 before view update in T009.
- **Phase 6**: T010 and T011 can run in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 & 2 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational (Java Enums `PairingSystem`, `LeagueScoringSystem`).
3. Complete Phase 3 & 4: User Story 1 & 2 (Tournament points and pairings DDL).
4. Verify custom tournament scoring and pairing algorithms.

### Incremental Delivery

1. Setup + Foundational → Domain enums available.
2. US1 & US2 → Custom tournament points and pairing systems in `tournaments` / `templates`.
3. US3 → League scoring, attendance points, and leaderboard view.
4. Polish → Synchronize contracts across all specs.

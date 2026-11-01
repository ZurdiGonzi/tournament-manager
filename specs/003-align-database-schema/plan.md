# Implementation Plan: Alineación del Esquema Relacional PostgreSQL y Migraciones Flyway

**Branch**: `003-align-database-schema` | **Date**: 2026-11-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/003-align-database-schema/spec.md`

## Summary

Consolidación del esquema relacional en PostgreSQL (Supabase / Neon), resolviendo las discrepancias identificadas respecto a los contratos de aplicación: inclusión de `email VARCHAR(255) NOT NULL UNIQUE` en `users`, ampliación de `TournamentFormat` a 4 formatos (`STANDARD`, `COMMANDER`, `MODERN`, `PAUPER`), flexibilización de `max_deck_budget` a opcional (`NULL` permitido o `> 0`), normalización de estados de partida (`MatchStatus`) e inscripción (`RegistrationStatus`), y despliegue del script canónico versionado Flyway en `src/main/resources/db/migration/V1__init_schema.sql` con soporte para dependencias Flyway PostgreSQL en `pom.xml`.

## Technical Context

**Language/Version**: SQL (PostgreSQL 15+ Dialect) / Java 21

**Primary Dependencies**: Flyway (`org.flywaydb:flyway-core`, `org.flywaydb:flyway-database-postgresql`), PostgreSQL Driver (`org.postgresql:postgresql`), Spring Boot 3.x

**Storage**: PostgreSQL (Supabase / Neon con pooling PgBouncer en transaction mode)

**Testing**: PostgreSQL SQL verification script, Flyway test harness

**Target Platform**: Linux / Cloud PostgreSQL 15+

**Project Type**: Migración de persistencia relacional y documental (`JSONB`)

**Performance Goals**: Ejecución de migración inicial en < 3 segundos; indexación GIN para búsquedas en snapshots de mazos en < 10 ms

**Constraints**: Inmutabilidad de snapshots de mazo (`JSONB`), integridad referencial estricta (FKs con `ON DELETE CASCADE/RESTRICT`), concordancia 1:1 de enums

**Scale/Scope**: 12 tablas, 2 vistas analíticas (`v_league_leaderboard`, `v_scryfall_card_stats`), 12 índices relacionales y GIN

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio Constitucional | Estado | Evidencia y Justificación |
| :--- | :---: | :--- |
| **I. Dominio Puro en Java 21** | **PASS** | El esquema no impone dependencias de frameworks al dominio; refleja fielmente los enums de `domain/model/`. |
| **II. Seguridad Stateless y JWT** | **PASS** | La tabla `users` incorpora `email` único para autenticación JWT y roles `PLAYER`/`ADMIN`. |
| **III. Control Estricto en Frontend** | **PASS** | Estados finitos alineados 1:1 con las discriminated unions del frontend. |
| **IV. Integridad Relacional y Snapshots JSONB** | **PASS** | `tournament_registrations.deck_snapshot JSONB` con índice GIN mantiene copias históricas inmutables de los mazos al registrarse. |
| **V. Scheduler Idempotente** | **PASS** | La tabla `tournaments` incluye la restricción `uq_tournament_template_schedule UNIQUE (template_id, scheduled_at)` para garantizar idempotencia en `@Scheduled`. |

## Project Structure

### Documentation (this feature)

```text
specs/003-align-database-schema/
├── plan.md              # Este plan de implementación
├── research.md          # Investigación de Flyway 10+, DDL y PostgreSQL
├── data-model.md        # Definición estructurada del modelo relacional consolidado
├── quickstart.md        # Guía de validación y consultas SQL
├── contracts/
│   └── flyway-v1-contract.sql
└── checklists/
    └── requirements.md  # Checklist de validación de calidad de la especificación
```

### Source Code (repository root)

```text
src/main/resources/
├── db/
│   └── migration/
│       └── V1__init_schema.sql  # Script canónico Flyway con 12 tablas, 2 vistas y 12 índices
└── application.yml              # Configuración de DataSource y Flyway
```

**Structure Decision**: El script DDL se ubica en `src/main/resources/db/migration/V1__init_schema.sql` cumpliendo estrictamente con la convención de Flyway y el Principio IV de la Constitución.

## Complexity Tracking

> **Sin violaciones a la Constitución detectadas.**

# Implementation Plan: Configuración de Pairings, Puntuación de Torneo y Ligas

**Branch**: `004-custom-scoring-pairings` | **Date**: 2026-11-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/004-custom-scoring-pairings/spec.md`

## Summary

Evolución del esquema relacional y de la migración canónica Flyway para incorporar soporte completo a fórmulas personalizadas por el dueño de la tienda: sistema de emparejamientos (`pairing_system`: `SWISS`, `SINGLE_ELIMINATION`, `ROUND_ROBIN`, `CUSTOM` y `pairing_config JSONB`), asignación de puntos por victoria, empate, derrota y BYE (`points_per_win`, `points_per_draw`, `points_per_loss`, `points_per_bye`), y sistema de ligas con fidelización (`participation_points`, `best_n_tournaments`, `rank_points_distribution JSONB` y actualización de la vista `v_league_leaderboard`).

## Technical Context

**Language/Version**: SQL (PostgreSQL 15+) / Java 21

**Primary Dependencies**: Flyway, PostgreSQL JDBC, Spring Boot 3.x

**Storage**: PostgreSQL (Supabase / Neon)

**Testing**: Pruebas SQL relacionales y verificación de restricciones `CHECK`

**Target Platform**: PostgreSQL 15+ en nube o local

**Project Type**: Persistencia relacional, documental JSONB y vistas SQL analíticas

**Performance Goals**: Cómputo ágil de la vista de clasificación en < 50 ms mediante agregación nativa

**Constraints**: Retrocompatibilidad estricta: torneos y ligas sin parámetros especiales aplican automáticamente los valores estándar (3/1/0/3 de torneo y Suizo tradicional)

**Scale/Scope**: 3 tablas alteradas/actualizadas (`tournaments`, `tournament_templates`, `leagues`), 1 vista SQL actualizada (`v_league_leaderboard`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio Constitucional | Estado | Evidencia y Justificación |
| :--- | :---: | :--- |
| **I. Dominio Puro en Java 21** | **PASS** | El soporte relacional proporciona los parámetros necesarios para que los motores de dominio puros (`SwissPairingEngine`, etc.) operen de forma desacoplada. |
| **II. Seguridad Stateless y JWT** | **PASS** | La configuración de pairings y puntuación es gestionada bajo endpoints protegidos de administración (`ROLE_ADMIN`). |
| **III. Control Estricto en Frontend** | **PASS** | Los nuevos campos y opciones se corresponden con uniones discriminadas y contratos tipados en TypeScript. |
| **IV. Integridad Relacional y JSONB** | **PASS** | Columnas `JSONB` especializadas para configuraciones extensibles de emparejamiento y baremos de liga. |
| **V. Scheduler Idempotente** | **PASS** | `tournament_templates` replica los parámetros de emparejamiento y puntuación para instanciación automática idempotente. |

## Project Structure

### Documentation (this feature)

```text
specs/004-custom-scoring-pairings/
├── plan.md              # Este plan de implementación
├── research.md          # Investigación de motores de pairing y puntuación de ligas
├── data-model.md        # Definición de tablas modificadas, columnas y vista
├── quickstart.md        # Guía de validación y consultas SQL
├── contracts/
│   └── ddl-update-contract.sql
└── checklists/
    └── requirements.md  # Checklist de validación de calidad de la especificación
```

### Source Code (repository root)

```text
src/main/resources/db/migration/
└── V1__init_schema.sql  # Script canónico actualizado con las nuevas columnas y vista
```

**Structure Decision**: Se actualiza el script canónico `V1__init_schema.sql` y los contratos de diseño para consolidar las columnas de pairing y puntuación en la versión inicial de la base de datos.

## Complexity Tracking

> **Sin violaciones a la Constitución detectadas.**

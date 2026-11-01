# Implementation Plan: Modelo de Datos de Tienda, Torneos, Mazos (Moxfield) y Estadísticas (Scryfall)

**Branch**: `001-store-tournament-data-model` | **Date**: 2026-11-01 | **Spec**: [spec.md](file:///c:/Users/Gonzalo/Documents/Proyectos/tournament-manager/specs/001-store-tournament-data-model/spec.md)

**Input**: Feature specification from `/specs/001-store-tournament-data-model/spec.md`

## Summary

Diseñar e implementar el modelo de datos relacional y documental (`JSONB`) en **PostgreSQL (Supabase / Neon)** junto con sus modelos de dominio puros en Java 21, adaptadores de persistencia JPA/Hibernate 6 y contratos de consulta para cubrir las cinco entidades clave solicitadas:
1. **Usuario (`users`)**: Nombre único, contraseña cifrada (`password_hash`), rol (`PLAYER` vs `ADMIN`/Dueño) y su lista de mazos (`decks`).
2. **Tienda (`stores` y `store_members`)**: Propietario (`owner_id`), lista de usuarios miembros y lista de torneos organizados.
3. **Mazo (`decks`)**: Enlace directo a **Moxfield** (`moxfield_url` y `moxfield_deck_id`) asociado al usuario, más **snapshot inmutable en `JSONB`** (`tournament_registrations.deck_snapshot`) al apuntarse a un torneo.
4. **Liga (`leagues` y `league_standings`)**: Lista de torneos puntuables asociados (`tournaments.league_id`) y lista de jugadores participantes con sus puntos acumulados (`total_points`) orientados por los resultados obtenidos en dichos torneos.
5. **Torneo (`tournaments` y `tournament_templates`)**: Día y hora (`scheduled_at`), precio de entrada (`entry_fee`), aforo máximo (`max_players`), premio (`prize_description`), vínculo opcional a una Liga (`league_id`), lista de usuarios apuntados (`tournament_registrations` con sus `match_points`) y restricciones competitivas (formato de Magic `STANDARD` o `COMMANDER` y límite de precio máximo del mazo `max_deck_budget`).
6. **Cartas y Estadísticas (`scryfall_cards`, `registration_deck_cards` y Vistas SQL `v_scryfall_card_stats` / `v_league_leaderboard`)**: Persistencia de los metadatos aportados por **Scryfall** (coste de maná, valor de maná/CMC, colores, identidad de color, tipos, rareza, legalidades por formato y precios) para validar las restricciones de inscripción (`BudgetValidator`) y servir estadísticas de metajuego (`AnalyticsController`).

## Technical Context

**Language/Version**: Java 21 (Backend) + TypeScript 5.x `strict: true` (Frontend)

**Primary Dependencies**: Spring Boot 3.x (Spring Data JPA / Hibernate 6, Spring Security 6 + JWT, Spring Validation, Spring Web `RestClient`), Flyway, PostgreSQL JDBC Driver

**Storage**: PostgreSQL 15+ (Supabase / Neon) con soporte nativo para `JSONB` (`@JdbcTypeCode(SqlTypes.JSON)`), arrays `TEXT[]` e índices `GIN`

**Testing**: JUnit 5 + AssertJ (tests unitarios puros de `domain/`) + Spring Boot Test / Testcontainers o H2 PostgreSQL compatibility mode para repositorios

**Target Platform**: Servidor JVM 21 (Linux/Windows) + Base de datos PostgreSQL gestionada en Supabase/Neon

**Project Type**: Web application (Backend REST API con Arquitectura Hexagonal + Frontend SPA en React + TypeScript)

**Performance Goals**: Validación de mazo e inscripción en `< 2s` (con caché en `scryfall_cards`); consultas de estadísticas agregadas de tienda/torneo en `< 200ms` sin llamadas externas

**Constraints**: Capa gratuita de Supabase/Neon (compatibilidad con PgBouncer `prepareThreshold=0`), `domain/` 100% libre de dependencias de Spring/JPA, autenticación stateless con JWT

**Scale/Scope**: Soporte multi-tienda con decenas de torneos concurrentes, miles de inscripciones con snapshots `JSONB` y catálogo local sincronizado de cartas de Scryfall

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio de la Constitución (`v1.0.0`) | Evaluación Pre-Phase 0 | Evaluación Post-Phase 1 | Justificación / Evidencia en el Diseño |
| :--- | :---: | :---: | :--- |
| **I. Núcleo de Dominio Puro y Tipado en Java 21** | ✅ PASS | ✅ PASS | Los modelos `User`, `Deck`, `Tournament`, `Match` (más objetos de valor `Store`, `ScryfallCard`, `TournamentRestrictions`) en `domain/model/` son Java 21 puro sin anotaciones JPA. `BudgetValidator` opera en `domain/service/`. |
| **II. Seguridad Stateless y Segregación por Roles con JWT** | ✅ PASS | ✅ PASS | El rol `ADMIN` (Dueño) + verificación `store.owner_id == currentUser.id` protege `/api/v1/admin/**`; los jugadores usan `/api/v1/player/**` y las estadísticas son públicas en `/api/v1/public/analytics/**`. |
| **III. Control Estricto de Tipos en Frontend (React + TS)** | ✅ PASS | ✅ PASS | Los contratos JSON de tienda, restricciones (`STANDARD` \| `COMMANDER`), snapshots y analíticas están tipados sin `any`. |
| **IV. Integridad Relacional en PostgreSQL y Snapshots en JSONB** | ✅ PASS | ✅ PASS | `V1__init_store_tournament_schema.sql` define claves foráneas, `UNIQUE (tournament_id, user_id)` y columna `deck_snapshot JSONB NOT NULL` mapeada con `@JdbcTypeCode(SqlTypes.JSON)`. |
| **V. Planificación Nativa e Idempotente (`@Scheduled`)** | ✅ PASS | ✅ PASS | La tabla `tournament_templates` y la restricción `UNIQUE (template_id, scheduled_at)` en `tournaments` garantizan idempotencia al crear torneos periódicos. |

## Project Structure

### Documentation (this feature)

```text
specs/001-store-tournament-data-model/
├── plan.md                                  # Este archivo (salida de /speckit-plan)
├── research.md                              # Decisiones de diseño y arquitectura de datos (Phase 0)
├── data-model.md                            # Diagrama ER, tablas PostgreSQL y estructura JSONB (Phase 1)
├── quickstart.md                            # Guía de validación del esquema y casos de uso (Phase 1)
├── contracts/
│   ├── database-ddl-contract.sql            # Contrato SQL DDL (Flyway V1) con tablas e índices GIN
│   └── rest-api-contracts.md                # Contratos de endpoints y DTOs por rol (Public/Player/Admin)
└── tasks.md                                 # Se generará en Phase 2 mediante /speckit-tasks
```

### Source Code (repository root)

```text
src/main/java/com/tournament/app/
├── domain/                                  # Núcleo sin dependencias de frameworks
│   ├── model/
│   │   ├── User.java                        # Usuario (nombre, passwordHash, rol Dueño/Jugador, lista de mazos)
│   │   ├── Store.java                       # Tienda (nombre, propietario Dueño, usuarios, torneos)
│   │   ├── Deck.java                        # Mazo (enlace a Moxfield + metadatos)
│   │   ├── ScryfallCard.java                # Información de carta de Scryfall para validación y estadísticas
│   │   ├── Tournament.java                  # Torneo (día/hora, precio, restricciones Standard/Commander + budget, premio, apuntados)
│   │   └── Match.java                       # Partida dentro de una ronda
│   ├── service/
│   │   ├── SwissPairingEngine.java          # Algoritmo de emparejamiento suizo puro
│   │   └── BudgetValidator.java             # Validación de formato (Standard/Commander) y coste máximo con Scryfall
│   └── repository/
│       ├── UserRepository.java
│       ├── StoreRepository.java
│       ├── DeckRepository.java
│       ├── ScryfallCardRepository.java
│       ├── TournamentRepository.java
│       └── RegistrationRepository.java
├── application/                             # Casos de uso
│   ├── usecase/
│   │   ├── RegisterPlayerUseCase.java       # Valida mazo de Moxfield + Scryfall y guarda snapshot JSONB
│   │   ├── GeneratePairingsUseCase.java
│   │   ├── ReportMatchResultUseCase.java
│   │   └── ScheduleTournamentUseCase.java
│   └── dto/
├── infrastructure/                          # Adaptadores externos y frameworks
│   ├── adapter/
│   │   ├── moxfield/
│   │   │   ├── MoxfieldClient.java          # Extracción de cartas a partir del enlace de Moxfield
│   │   │   └── MoxfieldDeckResponse.java
│   │   ├── scryfall/
│   │   │   ├── ScryfallClient.java          # Consulta/sincronización de cartas y precios con Scryfall API
│   │   │   └── ScryfallCardResponse.java
│   │   └── persistence/
│   │       ├── jpa/                         # Entidades JPA (*Entity.java) con @JdbcTypeCode(SqlTypes.JSON)
│   │       └── repository/                  # Adaptadores Spring Data JPA
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtTokenProvider.java
│   │   └── CustomUserDetailsService.java
│   └── scheduler/
│       └── TournamentAutoScheduler.java     # Cron jobs con @Scheduled
└── presentation/                            # Controladores REST
    ├── AuthController.java
    ├── PlayerTournamentController.java
    ├── AdminTournamentController.java
    └── AnalyticsController.java             # Estadísticas de cartas y mazos basadas en Scryfall

src/main/resources/
├── application.yml
└── db/migration/
    └── V1__init_store_tournament_schema.sql # Migración Flyway basada en contracts/database-ddl-contract.sql
```

**Structure Decision**: Se mantiene exactamente la arquitectura limpia/hexagonal en `src/main/java/com/tournament/app/` creada en el repositorio, incorporando las migraciones SQL de Flyway en `src/main/resources/db/migration/` y el adaptador de Scryfall junto al de Moxfield para alimentar las estadísticas y la validación de restricciones de torneos.

## Complexity Tracking

> **No existen violaciones a la Constitución (`0` excepciones requeridas).**

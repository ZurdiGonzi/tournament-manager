# Implementation Plan: Acceso a Datos de Torneos, Ligas y Estadísticas de Tienda e Integración con Scryfall

**Branch**: `002-store-tournament-backend-scryfall` | **Date**: 2026-11-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-store-tournament-backend-scryfall/spec.md`

## Summary

Implementar la capa completa de acceso y extracción de base de datos en **Java 21 + Spring Boot 3.x** para las **12 tablas** (`users`, `stores`, `store_members`, `decks`, `leagues`, `league_standings`, `tournament_templates`, `tournaments`, `scryfall_cards`, `tournament_registrations`, `registration_deck_cards`, `rounds`, `matches`) y las **2 vistas SQL** (`v_league_leaderboard` y `v_scryfall_card_stats`) definidas en la migración Flyway `V1__init_store_tournament_schema.sql`, junto con el adaptador de comunicación con **Scryfall** (`ScryfallClient` y sincronización automática al registrar/inscribir un mazo).

Para garantizar una integración sin conflictos con la rama paralela `002-backend-dtos`, esta rama implementa los modelos y puertos de dominio (`domain/model`, `domain/repository`, `domain/service`), las entidades y adaptadores de persistencia JPA (`infrastructure/adapter/persistence/`) y los adaptadores de Scryfall/Moxfield (`infrastructure/adapter/scryfall/`, `infrastructure/adapter/moxfield/`), exponiendo todas las consultas individuales y agregadas que requieren los contratos de `002-backend-dtos` **sin crear archivos en `application/dto`**.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3.x (`spring-boot-starter-data-jpa` / Hibernate 6, `spring-boot-starter-web` / `RestClient`, `spring-boot-starter-validation`, `spring-boot-starter-security`), Flyway (`flyway-core`, `flyway-database-postgresql`), PostgreSQL JDBC Driver

**Storage**: PostgreSQL 15+ (Supabase / Neon) con soporte nativo para `JSONB` (`@JdbcTypeCode(SqlTypes.JSON)`), arrays `TEXT[]` (`colors`, `color_identity`), vistas SQL de solo lectura (`@Immutable`) e índices `GIN`

**Testing**: JUnit 5 + AssertJ (tests unitarios puros de `domain/` y cliente Scryfall con `MockRestServiceServer`) + `@DataJpaTest` para repositorios de las 12 tablas y 2 vistas

**Target Platform**: Servidor JVM 21 (Linux/Windows) + PostgreSQL 15+ (Supabase / Neon)

**Project Type**: Backend REST service module (Arquitectura Hexagonal / Clean Architecture bajo `com.tournament.app`)

**Performance Goals**: Extracción de entidades y vistas agregadas desde PostgreSQL en `< 500ms` para torneos de hasta 128 jugadores; sincronización inicial de un mazo de hasta 100 cartas contra Scryfall `/cards/collection` en `< 5s` y `< 200ms` desde caché local (`scryfall_cards`)

**Constraints**:
- `domain/` 100% libre de importaciones `org.springframework.*` y `jakarta.persistence.*`.
- Compatibilidad con el connection pooler de Supabase/Neon (`prepareThreshold=0` y `ddl-auto=validate`).
- Respeto estricto de los límites de Scryfall (`<= 10 req/s`, lotes de hasta 75 cartas en `POST /cards/collection`, cabecera `User-Agent`).
- Prohibido crear clases dentro de `application/dto` en esta rama (desarrolladas en `002-backend-dtos`).

**Scale/Scope**: 12 tablas relacionales, 2 vistas SQL analíticas, 10 puertos de repositorio de dominio con sus adaptadores JPA y 1 adaptador HTTP para Scryfall activado al registrar mazos.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio de la Constitución (`v1.0.0`) | Evaluación Pre-Phase 0 | Evaluación Post-Phase 1 | Justificación / Evidencia en el Diseño |
| :--- | :---: | :---: | :--- |
| **I. Núcleo de Dominio Puro y Tipado en Java 21** | ✅ PASS | ✅ PASS | Todos los modelos (`User`, `Store`, `Deck`, `League`, `TournamentTemplate`, `Tournament`, `ScryfallCard`, `TournamentRegistration`, `Round`, `Match`, `DeckSnapshot` y resúmenes agregados) y puertos en `domain/` son Java 21 puro (`record`, `enum`, `sealed`) sin anotaciones de Spring ni JPA. |
| **II. Seguridad Stateless y Segregación por Roles con JWT** | ✅ PASS | ✅ PASS | Las proyecciones de `UserRepository` omiten `passwordHash` en lecturas públicas/perfiles y proveen verificación de propiedad (`store.ownerId`, `deck.userId`) para los casos de uso protegidos por JWT. |
| **III. Control Estricto de Tipos en Frontend (React + TS)** | ✅ PASS | ✅ PASS | Alcance exclusivo de Backend; los modelos y agregaciones devueltos por los repositorios están 1:1 alineados con los contratos tipados de `002-backend-dtos` para el futuro consumo desde React. |
| **IV. Integridad Relacional en PostgreSQL y Snapshots en JSONB** | ✅ PASS | ✅ PASS | `V1__init_store_tournament_schema.sql` gestiona las 12 tablas y 2 vistas con Flyway, claves foráneas, restricciones `UNIQUE` y `deck_snapshot JSONB NOT NULL` mapeado con `@JdbcTypeCode(SqlTypes.JSON)`. |
| **V. Planificación Nativa e Idempotente (`@Scheduled`)** | ✅ PASS | ✅ PASS | `TournamentTemplateRepository` y `TournamentRepository.existsByTemplateIdAndScheduledAt(...)` junto a `uq_tournament_template_schedule` aseguran idempotencia en la generación periódica de torneos. |

## Project Structure

### Documentation (this feature)

```text
specs/002-store-tournament-backend-scryfall/
├── plan.md                                              # Este archivo (/speckit-plan)
├── research.md                                          # Decisiones de mapeo JPA, JSONB, vistas y Scryfall (Phase 0)
├── data-model.md                                        # Modelo de las 12 tablas, 2 vistas y JSONB DeckSnapshot (Phase 1)
├── quickstart.md                                        # Guía de validación y pruebas ejecutables (Phase 1)
├── contracts/
│   ├── database-ddl-contract.sql                        # Contrato SQL Flyway V1 (12 tablas, 2 vistas e índices)
│   └── domain-repository-and-scryfall-contracts.md      # Contratos de puertos de dominio y cliente Scryfall
└── tasks.md                                             # Se generará en Phase 2 mediante /speckit-tasks
```

### Source Code (repository root)

```text
pom.xml
src/main/java/com/tournament/app/
├── TournamentManagerApplication.java
├── domain/                                              # Núcleo 100% libre de frameworks
│   ├── model/
│   │   ├── User.java
│   │   ├── UserRole.java
│   │   ├── UserProfileSummary.java
│   │   ├── Store.java
│   │   ├── StoreMember.java
│   │   ├── StoreDetailSummary.java
│   │   ├── CurrencyCode.java
│   │   ├── Deck.java
│   │   ├── League.java
│   │   ├── LeagueStatus.java
│   │   ├── LeagueStanding.java
│   │   ├── LeagueLeaderboardEntry.java
│   │   ├── LeagueDetailSummary.java
│   │   ├── MagicFormat.java
│   │   ├── TournamentTemplate.java
│   │   ├── Tournament.java
│   │   ├── TournamentStatus.java
│   │   ├── TournamentRestrictions.java
│   │   ├── TournamentDetailSummary.java
│   │   ├── ScryfallCard.java
│   │   ├── ScryfallCardStats.java
│   │   ├── MetagameAnalyticsSummary.java
│   │   ├── TournamentRegistration.java
│   │   ├── RegistrationStatus.java
│   │   ├── DeckSnapshot.java
│   │   ├── RegistrationDeckCard.java
│   │   ├── BoardSection.java
│   │   ├── Round.java
│   │   ├── RoundStatus.java
│   │   ├── Match.java
│   │   └── MatchStatus.java
│   ├── service/
│   │   ├── BudgetValidator.java                         # Valida formato Standard/Commander y presupuesto máximo con datos Scryfall
│   │   ├── SwissPairingEngine.java                      # Emparejamiento suizo puro y manejo de BYE
│   │   └── ScryfallCardGateway.java                     # Puerto de salida hacia Scryfall para sincronizar lotes de cartas al registrar mazo
│   └── repository/
│       ├── UserRepository.java
│       ├── StoreRepository.java
│       ├── DeckRepository.java
│       ├── LeagueRepository.java
│       ├── TournamentTemplateRepository.java
│       ├── TournamentRepository.java
│       ├── RegistrationRepository.java
│       ├── RoundRepository.java
│       ├── MatchRepository.java
│       └── ScryfallCardRepository.java
├── application/
│   └── usecase/
│       └── DeckRegistrationScryfallSynchronizer.java    # Sincroniza cartas de un mazo contra Scryfall al registrarlo/inscribirlo y construye el snapshot
└── infrastructure/
    └── adapter/
        ├── scryfall/
        │   ├── ScryfallClient.java                      # Implementa ScryfallCardGateway con RestClient (/cards/collection y búsqueda por nombre/ID)
        │   ├── ScryfallCollectionRequest.java
        │   ├── ScryfallCollectionResponse.java
        │   ├── ScryfallCardPayload.java
        │   └── ScryfallConfig.java
        └── persistence/
            ├── jpa/                                     # Entidades JPA de las 12 tablas + 2 vistas (@Immutable)
            │   ├── UserEntity.java
            │   ├── StoreEntity.java
            │   ├── StoreMemberEntity.java
            │   ├── StoreMemberId.java
            │   ├── DeckEntity.java
            │   ├── LeagueEntity.java
            │   ├── LeagueStandingEntity.java
            │   ├── LeagueStandingId.java
            │   ├── LeagueLeaderboardViewEntity.java
            │   ├── TournamentTemplateEntity.java
            │   ├── TournamentEntity.java
            │   ├── ScryfallCardEntity.java
            │   ├── ScryfallCardStatsViewEntity.java
            │   ├── ScryfallCardStatsViewId.java
            │   ├── TournamentRegistrationEntity.java
            │   ├── RegistrationDeckCardEntity.java
            │   ├── RoundEntity.java
            │   ├── MatchEntity.java
            │   └── PersistenceEntityMapper.java
            └── repository/                              # Interfaces Spring Data JPA (*JpaRepository) y adaptadores (*RepositoryAdapter)
                ├── SpringDataUserJpaRepository.java
                ├── UserRepositoryAdapter.java
                ├── SpringDataStoreJpaRepository.java
                ├── SpringDataStoreMemberJpaRepository.java
                ├── StoreRepositoryAdapter.java
                ├── SpringDataDeckJpaRepository.java
                ├── DeckRepositoryAdapter.java
                ├── SpringDataLeagueJpaRepository.java
                ├── SpringDataLeagueStandingJpaRepository.java
                ├── SpringDataLeagueLeaderboardJpaRepository.java
                ├── LeagueRepositoryAdapter.java
                ├── SpringDataTournamentTemplateJpaRepository.java
                ├── TournamentTemplateRepositoryAdapter.java
                ├── SpringDataTournamentJpaRepository.java
                ├── TournamentRepositoryAdapter.java
                ├── SpringDataRegistrationJpaRepository.java
                ├── SpringDataRegistrationDeckCardJpaRepository.java
                ├── RegistrationRepositoryAdapter.java
                ├── SpringDataRoundJpaRepository.java
                ├── SpringDataMatchJpaRepository.java
                ├── RoundRepositoryAdapter.java
                ├── MatchRepositoryAdapter.java
                ├── SpringDataScryfallCardJpaRepository.java
                ├── SpringDataScryfallCardStatsJpaRepository.java
                └── ScryfallCardRepositoryAdapter.java

src/main/resources/
├── application.yml
└── db/migration/
    └── V1__init_store_tournament_schema.sql

src/test/java/com/tournament/app/
├── domain/
│   └── service/
│       ├── BudgetValidatorTest.java
│       └── SwissPairingEngineTest.java
└── infrastructure/
    └── adapter/
        ├── scryfall/
        │   └── ScryfallClientTest.java
        └── persistence/
            └── RepositoryExtractionIntegrationTest.java
```

**Structure Decision**: Se adopta la estructura de paquetes de Arquitectura Hexagonal estipulada en `AGENTS.md` y la Constitución (`com.tournament.app.domain`, `com.tournament.app.application`, `com.tournament.app.infrastructure`), omitiendo deliberadamente la creación de archivos en `com.tournament.app.application.dto` para no entrar en conflicto con la rama `002-backend-dtos`.

## Complexity Tracking

> **No existen violaciones a la Constitución (`0` excepciones requeridas).**

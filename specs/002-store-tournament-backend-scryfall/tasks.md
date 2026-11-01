---
description: "Task list for feature 002-store-tournament-backend-scryfall"
---

# Tasks: Acceso a Datos de Torneos, Ligas y Estadísticas de Tienda e Integración con Scryfall

**Input**: Design documents from `/specs/002-store-tournament-backend-scryfall/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**Tests**: Se incluyen las pruebas unitarias del núcleo de dominio (`BudgetValidator`, `SwissPairingEngine`), del adaptador de Scryfall (`ScryfallClient`) y de extracción de los repositorios JPA exigidas por las Puertas de Calidad de la Constitución.

**Organization**: Las tareas están agrupadas por Historia de Usuario (`US1`, `US2`, `US3`) para permitir su implementación y validación independiente, respetando la exclusión de `application/dto` acordada con la rama `002-backend-dtos`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Ejecutable en paralelo (archivos distintos, sin dependencias bloqueantes en curso)
- **[Story]**: Historia de usuario a la que pertenece la tarea (`[US1]`, `[US2]`, `[US3]`)
- Todas las tareas incluyen rutas exactas de archivos y las restricciones de campos definidas en `data-model.md`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicialización del proyecto Java 21 + Spring Boot 3.x y configuración base de persistencia y cliente HTTP.

- [X] T001 Crear `pom.xml` en la raíz del proyecto (`pom.xml`) con Java 21, Spring Boot 3.3.x (`spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`), Flyway (`flyway-core`, `flyway-database-postgresql`), driver PostgreSQL y dependencias de test (`spring-boot-starter-test`, `h2`)
- [X] T002 Crear clase principal de arranque en `src/main/java/com/tournament/app/TournamentManagerApplication.java`
- [X] T003 [P] Configurar `src/main/resources/application.yml` con conexión PostgreSQL compatible con Supabase/Neon (`prepareThreshold=0`, `spring.jpa.hibernate.ddl-auto=validate`), Flyway habilitado y propiedades de Scryfall (`scryfall.base-url: https://api.scryfall.com`, `scryfall.cache-ttl-hours: 24`, `scryfall.rate-limit-ms: 100`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Migración Flyway V1 completa (12 tablas, 2 vistas SQL e índices) y enumeraciones/tipos base de dominio compartidos por todas las historias.

**⚠️ CRITICAL**: Ninguna historia de usuario puede comenzar hasta completar esta fase.

- [X] T004 Crear la migración Flyway `src/main/resources/db/migration/V1__init_store_tournament_schema.sql` con las 12 tablas (`users`, `stores`, `store_members`, `decks`, `leagues`, `league_standings`, `tournament_templates`, `tournaments`, `scryfall_cards`, `tournament_registrations`, `registration_deck_cards`, `rounds`, `matches`), las 2 vistas (`v_league_leaderboard`, `v_scryfall_card_stats`) y los 13 índices definidos en `contracts/database-ddl-contract.sql`
- [X] T005 [P] Crear enumeraciones base de dominio sin dependencias de frameworks en `src/main/java/com/tournament/app/domain/model/UserRole.java` (`PLAYER`, `ADMIN`), `src/main/java/com/tournament/app/domain/model/CurrencyCode.java` (`EUR`, `USD`) y `src/main/java/com/tournament/app/domain/model/MagicFormat.java` (`STANDARD`, `COMMANDER`)
- [X] T006 [P] Crear enumeraciones de estados competitivos en `src/main/java/com/tournament/app/domain/model/LeagueStatus.java` (`OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), `src/main/java/com/tournament/app/domain/model/TournamentStatus.java` (`DRAFT`, `REGISTRATION_OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), `src/main/java/com/tournament/app/domain/model/RegistrationStatus.java` (`REGISTERED`, `CHECKED_IN`, `DROPPED`, `DISQUALIFIED`), `src/main/java/com/tournament/app/domain/model/BoardSection.java` (`COMMANDER`, `MAINBOARD`, `SIDEBOARD`), `src/main/java/com/tournament/app/domain/model/RoundStatus.java` (`IN_PROGRESS`, `COMPLETED`) y `src/main/java/com/tournament/app/domain/model/MatchStatus.java` (`PENDING`, `IN_PROGRESS`, `AWAITING_CONFIRMATION`, `DISPUTED`, `CONFIRMED`)

**Checkpoint**: Base de datos y tipos fundamentales listos — las historias `US1`, `US2` y `US3` pueden implementarse.

---

## Phase 3: User Story 1 - Consulta Integral de Tiendas, Usuarios, Miembros, Mazos y Plantillas (Priority: P1) 🎯 MVP

**Goal**: Extraer de la base de datos usuarios (con resumen de perfil sin exponer `password_hash`), tiendas, miembros de tienda (con totales de torneos y miembros), mazos de usuario vinculados a Moxfield y plantillas de torneos periódicos.

**Independent Test**: Poblar registros en `users`, `stores`, `store_members`, `decks` y `tournament_templates` y verificar mediante prueba de integración que cada entidad y sus resúmenes agregados (`UserProfileSummary`, `StoreDetailSummary`) se extraen individualmente y por listado sin exponer credenciales sensibles.

### Implementation for User Story 1

- [X] T007 [P] [US1] Crear modelos de dominio `User` (`id UUID NOT NULL`, `username String max 60 UNIQUE NOT NULL`, `passwordHash String max 255 NOT NULL`, `role UserRole NOT NULL`, `createdAt Instant NOT NULL` y vista segura sin hash) y `UserProfileSummary` (usuario, tiendas a las que pertenece, `registeredDeckCount` y resumen de ligas) en `src/main/java/com/tournament/app/domain/model/User.java` y `src/main/java/com/tournament/app/domain/model/UserProfileSummary.java`
- [X] T008 [P] [US1] Crear modelos de dominio `Store` (`id UUID NOT NULL`, `name String max 120 UNIQUE NOT NULL`, `ownerId UUID NOT NULL`, `currency CurrencyCode default EUR NOT NULL`, `createdAt Instant NOT NULL`), `StoreMember` (`storeId UUID NOT NULL`, `userId UUID NOT NULL`, `joinedAt Instant NOT NULL`) y `StoreDetailSummary` (`store`, `ownerUsername`, `totalTournaments`, `totalMembers`) en `src/main/java/com/tournament/app/domain/model/Store.java`, `src/main/java/com/tournament/app/domain/model/StoreMember.java` y `src/main/java/com/tournament/app/domain/model/StoreDetailSummary.java`
- [X] T009 [P] [US1] Crear modelos de dominio `Deck` (`id UUID NOT NULL`, `userId UUID NOT NULL`, `name String max 150 NOT NULL`, `moxfieldUrl String max 500 NOT NULL`, `moxfieldDeckId String max 64 NOT NULL`, `createdAt Instant NOT NULL`, `updatedAt Instant NOT NULL`) y `TournamentTemplate` (`id UUID NOT NULL`, `storeId UUID NOT NULL`, `leagueId UUID nullable`, `name String max 150 NOT NULL`, `cronExpression String max 60 NOT NULL`, `entryFee BigDecimal >= 0`, `prizeDescription String NOT NULL`, `format MagicFormat NOT NULL`, `maxDeckBudget BigDecimal > 0`, `maxPlayers int >= 2 default 32`, `maxRounds int > 0 default 3`, `active boolean default true`, `lastGeneratedAt Instant nullable`, `createdAt Instant NOT NULL`) en `src/main/java/com/tournament/app/domain/model/Deck.java` y `src/main/java/com/tournament/app/domain/model/TournamentTemplate.java`
- [X] T010 [P] [US1] Definir puertos de repositorio `UserRepository`, `StoreRepository`, `DeckRepository` y `TournamentTemplateRepository` en `src/main/java/com/tournament/app/domain/repository/UserRepository.java`, `src/main/java/com/tournament/app/domain/repository/StoreRepository.java`, `src/main/java/com/tournament/app/domain/repository/DeckRepository.java` y `src/main/java/com/tournament/app/domain/repository/TournamentTemplateRepository.java`
- [X] T011 [P] [US1] Crear entidades JPA `UserEntity`, `StoreEntity`, `StoreMemberId`, `StoreMemberEntity`, `DeckEntity` y `TournamentTemplateEntity` con todas las restricciones de columna en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/UserEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/StoreEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/StoreMemberId.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/StoreMemberEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/DeckEntity.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/TournamentTemplateEntity.java`
- [X] T012 [P] [US1] Crear repositorios Spring Data JPA para `users`, `stores`, `store_members`, `decks` y `tournament_templates` en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataUserJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataStoreJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataStoreMemberJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataDeckJpaRepository.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataTournamentTemplateJpaRepository.java`
- [X] T013 [US1] Implementar los adaptadores de persistencia `UserRepositoryAdapter`, `StoreRepositoryAdapter`, `DeckRepositoryAdapter` y `TournamentTemplateRepositoryAdapter` en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/UserRepositoryAdapter.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/StoreRepositoryAdapter.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/DeckRepositoryAdapter.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/TournamentTemplateRepositoryAdapter.java`

**Checkpoint**: `US1` completada y verificable de forma independiente (extracción de `users`, `stores`, `store_members`, `decks` y `tournament_templates`).

---

## Phase 4: User Story 2 - Extracción de Ligas, Clasificaciones de Liga, Torneos, Inscripciones, Rondas y Partidas (Priority: P1)

**Goal**: Extraer ligas (`leagues`), puntos por jugador en liga (`league_standings`), clasificación consolidada de liga (`v_league_leaderboard`), torneos (`tournaments`), inscripciones con puntuación competitiva y snapshot inmutable en `JSONB` (`tournament_registrations`), cartas desglosadas por sección (`registration_deck_cards`), rondas (`rounds`) y partidas (`matches`, incluyendo soporte de *BYE*).

**Independent Test**: Cargar una liga con torneos, jugadores apuntados en `league_standings`, inscripciones con `deck_snapshot` y `registration_deck_cards`, rondas y partidas (con y sin *BYE*), y comprobar la extracción exacta de cada tabla y de la vista `v_league_leaderboard`, además de los tests unitarios de `SwissPairingEngine`.

### Implementation for User Story 2

- [X] T014 [P] [US2] Crear modelos de dominio de Liga: `League` (`id UUID NOT NULL`, `storeId UUID NOT NULL`, `name String max 150 NOT NULL`, `format MagicFormat NOT NULL`, `prizeDescription String nullable`, `startDate Instant NOT NULL`, `endDate Instant nullable`, `status LeagueStatus default IN_PROGRESS`, `createdAt Instant NOT NULL`), `LeagueStanding` (`leagueId UUID NOT NULL`, `userId UUID NOT NULL`, `totalPoints int >= 0`, `tournamentsPlayed int >= 0`, `matchesWon int >= 0`, `matchesDrawn int >= 0`, `matchesLost int >= 0`, `updatedAt Instant NOT NULL`), `LeagueLeaderboardEntry` (vista `v_league_leaderboard`: `leagueId`, `storeId`, `leagueName`, `userId`, `username`, `totalPoints`, `tournamentsPlayed`, `currentRank`) y `LeagueDetailSummary` en `src/main/java/com/tournament/app/domain/model/League.java`, `src/main/java/com/tournament/app/domain/model/LeagueStanding.java`, `src/main/java/com/tournament/app/domain/model/LeagueLeaderboardEntry.java` y `src/main/java/com/tournament/app/domain/model/LeagueDetailSummary.java`
- [X] T015 [P] [US2] Crear modelos de dominio de Torneo: `TournamentRestrictions` (`format MagicFormat NOT NULL`, `maxDeckBudget BigDecimal > 0 NOT NULL`), `Tournament` (`id UUID NOT NULL`, `storeId UUID NOT NULL`, `leagueId UUID nullable`, `templateId UUID nullable`, `name String max 150 NOT NULL`, `scheduledAt Instant NOT NULL`, `entryFee BigDecimal >= 0`, `prizeDescription String NOT NULL`, `restrictions TournamentRestrictions`, `maxPlayers int >= 2 default 32`, `status TournamentStatus default REGISTRATION_OPEN`, `currentRound int default 0`, `maxRounds int default 3`, `createdAt Instant NOT NULL`) y `TournamentDetailSummary` en `src/main/java/com/tournament/app/domain/model/TournamentRestrictions.java`, `src/main/java/com/tournament/app/domain/model/Tournament.java` y `src/main/java/com/tournament/app/domain/model/TournamentDetailSummary.java`
- [X] T016 [P] [US2] Crear modelos de dominio de Inscripción, Snapshot y Rondas/Partidas: `DeckSnapshot` (record inmutable para `JSONB` con `deckId`, `deckName`, `moxfieldDeckId`, `format`, `calculatedDeckPrice`, `snapshotDate` y lista de `SnapshotCardEntry`), `TournamentRegistration` (`id UUID NOT NULL`, `tournamentId UUID NOT NULL`, `userId UUID NOT NULL`, `deckId UUID NOT NULL`, `status RegistrationStatus default REGISTERED`, `calculatedDeckPrice BigDecimal >= 0`, `matchPoints int >= 0 default 0`, `omwPercentage BigDecimal default 0.0000`, `finalRank Integer > 0 nullable`, `deckSnapshot DeckSnapshot NOT NULL`, `registeredAt Instant NOT NULL`), `RegistrationDeckCard` (`id UUID NOT NULL`, `registrationId UUID NOT NULL`, `scryfallCardId UUID NOT NULL`, `boardSection BoardSection NOT NULL`, `quantity int > 0`, `unitPriceSnapshot BigDecimal >= 0`), `Round` (`id UUID NOT NULL`, `tournamentId UUID NOT NULL`, `roundNumber int > 0`, `status RoundStatus NOT NULL`, `startedAt Instant NOT NULL`, `endedAt Instant nullable`) y `Match` (`id UUID NOT NULL`, `roundId UUID NOT NULL`, `tableNumber int NOT NULL`, `player1Id UUID NOT NULL`, `player2Id UUID nullable` para BYE, `player1Wins int default 0`, `player2Wins int default 0`, `draws int default 0`, `status MatchStatus default PENDING`, `winnerId UUID nullable`) en `src/main/java/com/tournament/app/domain/model/DeckSnapshot.java`, `src/main/java/com/tournament/app/domain/model/TournamentRegistration.java`, `src/main/java/com/tournament/app/domain/model/RegistrationDeckCard.java`, `src/main/java/com/tournament/app/domain/model/Round.java` y `src/main/java/com/tournament/app/domain/model/Match.java`
- [X] T017 [P] [US2] Definir puertos de repositorio `LeagueRepository`, `TournamentRepository`, `RegistrationRepository`, `RoundRepository` y `MatchRepository` en `src/main/java/com/tournament/app/domain/repository/LeagueRepository.java`, `src/main/java/com/tournament/app/domain/repository/TournamentRepository.java`, `src/main/java/com/tournament/app/domain/repository/RegistrationRepository.java`, `src/main/java/com/tournament/app/domain/repository/RoundRepository.java` y `src/main/java/com/tournament/app/domain/repository/MatchRepository.java`
- [X] T018 [P] [US2] Implementar `SwissPairingEngine` en el núcleo de dominio (`src/main/java/com/tournament/app/domain/service/SwissPairingEngine.java`) para cálculo de emparejamientos suizos, asignación de *BYE* con número impar de jugadores, prohibición de revanchas y cálculo de porcentaje de victorias de oponentes (`omw_percentage`)
- [X] T019 [P] [US2] Crear entidades JPA para Ligas y su vista Leaderboard: `LeagueEntity`, `LeagueStandingId`, `LeagueStandingEntity` y `LeagueLeaderboardViewEntity` (`@Immutable @Table(name = "v_league_leaderboard")`) en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/LeagueEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/LeagueStandingId.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/LeagueStandingEntity.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/LeagueLeaderboardViewEntity.java`
- [X] T020 [P] [US2] Crear entidades JPA para Torneos, Inscripciones (con `@JdbcTypeCode(SqlTypes.JSON)` en `deck_snapshot`), Cartas de Inscripción, Rondas y Partidas en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/TournamentEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/TournamentRegistrationEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/RegistrationDeckCardEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/RoundEntity.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/MatchEntity.java`
- [X] T021 [P] [US2] Crear repositorios Spring Data JPA para `leagues`, `league_standings`, `v_league_leaderboard`, `tournaments`, `tournament_registrations`, `registration_deck_cards`, `rounds` y `matches` en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataLeagueJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataLeagueStandingJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataLeagueLeaderboardJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataTournamentJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataRegistrationJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataRegistrationDeckCardJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataRoundJpaRepository.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataMatchJpaRepository.java`
- [X] T022 [US2] Implementar adaptadores de persistencia `LeagueRepositoryAdapter`, `TournamentRepositoryAdapter`, `RegistrationRepositoryAdapter`, `RoundRepositoryAdapter` y `MatchRepositoryAdapter` en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/LeagueRepositoryAdapter.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/TournamentRepositoryAdapter.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/RegistrationRepositoryAdapter.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/RoundRepositoryAdapter.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/MatchRepositoryAdapter.java`
- [X] T023 [P] [US2] Crear test unitario puro `SwissPairingEngineTest` en `src/test/java/com/tournament/app/domain/service/SwissPairingEngineTest.java` verificando número impar de jugadores (*BYE*), prohibición de revanchas y cálculo de `omw_percentage`

**Checkpoint**: `US1` y `US2` operativas y verificables de forma independiente.

---

## Phase 5: User Story 3 - Comunicación con Scryfall, Catálogo Local de Cartas y Estadísticas de Metajuego (Priority: P2)

**Goal**: Comunicarse con Scryfall API internamente al registrar o inscribir un mazo para obtener y actualizar las cartas del mazo en `scryfall_cards`, validar formato y presupuesto del mazo (`BudgetValidator`) y extraer tanto cartas individuales desde `scryfall_cards` como estadísticas agregadas de metajuego desde la vista `v_scryfall_card_stats`.

**Independent Test**: Simular el registro de un mazo con cartas nuevas y cartas ya en caché local, verificando que `ScryfallClient` consulta `POST /cards/collection` solo para las cartas no vigentes, persiste los metadatos y precios (`EUR`/`USD`) en `scryfall_cards`, valida el presupuesto con `BudgetValidator` y extrae estadísticas desde `v_scryfall_card_stats`.

### Implementation for User Story 3

- [X] T024 [P] [US3] Crear modelos de dominio `ScryfallCard` (`id UUID NOT NULL`, `oracleId UUID NOT NULL`, `name String max 200 NOT NULL`, `manaCost String max 60 nullable`, `cmc BigDecimal default 0 NOT NULL`, `typeLine String max 200 NOT NULL`, `colors List<String> NOT NULL`, `colorIdentity List<String> NOT NULL`, `rarity String max 20 NOT NULL`, `legalities Map<String, String> NOT NULL`, `priceEur BigDecimal nullable`, `priceUsd BigDecimal nullable`, `imageUri String max 500 nullable`, `lastSyncedAt Instant NOT NULL`), `ScryfallCardStats` (vista `v_scryfall_card_stats`: `storeId`, `leagueId`, `format`, `oracleId`, `cardName`, `typeLine`, `manaCost`, `cmc`, `colors`, `colorIdentity`, `rarity`, `decksIncludingCard`, `totalCopiesPlayed`, `avgUnitPrice`) y `MetagameAnalyticsSummary` en `src/main/java/com/tournament/app/domain/model/ScryfallCard.java`, `src/main/java/com/tournament/app/domain/model/ScryfallCardStats.java` y `src/main/java/com/tournament/app/domain/model/MetagameAnalyticsSummary.java`
- [X] T025 [P] [US3] Definir puertos `ScryfallCardRepository` y `ScryfallCardGateway` en `src/main/java/com/tournament/app/domain/repository/ScryfallCardRepository.java` y `src/main/java/com/tournament/app/domain/service/ScryfallCardGateway.java`
- [X] T026 [P] [US3] Implementar `BudgetValidator` en `src/main/java/com/tournament/app/domain/service/BudgetValidator.java` para validar legalidad de cada carta en el formato del torneo (`STANDARD` o `COMMANDER`), calcular el precio total del mazo según la divisa (`EUR` o `USD`) y detallar las cartas causantes de exceso de presupuesto o ilegalidad
- [X] T027 [P] [US3] Crear entidades JPA `ScryfallCardEntity` (con `@JdbcTypeCode(SqlTypes.JSON)` en `legalities` y arrays `TEXT[]` en `colors` y `color_identity`), `ScryfallCardStatsViewId` y `ScryfallCardStatsViewEntity` (`@Immutable @Table(name = "v_scryfall_card_stats")`) en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/ScryfallCardEntity.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/ScryfallCardStatsViewId.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/ScryfallCardStatsViewEntity.java`
- [X] T028 [P] [US3] Crear repositorios Spring Data JPA `SpringDataScryfallCardJpaRepository` y `SpringDataScryfallCardStatsJpaRepository` e implementar `ScryfallCardRepositoryAdapter` en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataScryfallCardJpaRepository.java`, `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/SpringDataScryfallCardStatsJpaRepository.java` y `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/ScryfallCardRepositoryAdapter.java`
- [X] T029 [P] [US3] Implementar la configuración y payloads HTTP de Scryfall (`ScryfallConfig`, `ScryfallCollectionRequest`, `ScryfallCollectionResponse`, `ScryfallCardPayload` con soporte para cartas multifaz `card_faces`) en `src/main/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallConfig.java`, `src/main/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallCollectionRequest.java`, `src/main/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallCollectionResponse.java` y `src/main/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallCardPayload.java`
- [X] T030 [US3] Implementar `ScryfallClient` en `src/main/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallClient.java` usando `RestClient` contra `POST /cards/collection` (lotes de máximo 75 cartas, cabeceras `User-Agent` y `Accept`, control de pausa entre lotes) y `GET /cards/named`
- [X] T031 [US3] Implementar el servicio `DeckRegistrationScryfallSynchronizer` en `src/main/java/com/tournament/app/application/usecase/DeckRegistrationScryfallSynchronizer.java` que al registrar o inscribir un mazo comprueba la vigencia en `ScryfallCardRepository`, sincroniza las cartas faltantes/caducadas con `ScryfallCardGateway`, persiste en `scryfall_cards`, ejecuta `BudgetValidator` y construye el `DeckSnapshot` inmutable junto con la lista de `RegistrationDeckCard`
- [X] T032 [P] [US3] Crear pruebas unitarias `BudgetValidatorTest` en `src/test/java/com/tournament/app/domain/service/BudgetValidatorTest.java` y `ScryfallClientTest` en `src/test/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallClientTest.java`

**Checkpoint**: Las 3 Historias de Usuario (`US1`, `US2`, `US3`) están completas y son verificables de forma independiente.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Mapper unificado de persistencia, prueba de integración integral de extracción de las 12 tablas y 2 vistas SQL, y verificación de pureza arquitectónica de la Constitución.

- [X] T033 Centralizar conversiones entidad-dominio en `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/PersistenceEntityMapper.java` asegurando la no exposición de `password_hash` en resúmenes públicos y la fidelidad del mapeo de `DeckSnapshot`
- [X] T034 Crear prueba de integración integral `RepositoryExtractionIntegrationTest` en `src/test/java/com/tournament/app/infrastructure/adapter/persistence/RepositoryExtractionIntegrationTest.java` que verifique la extracción de las 12 tablas y las 2 vistas (`v_league_leaderboard`, `v_scryfall_card_stats`) y la sincronización de cartas al registrar un mazo
- [X] T035 Ejecutar la validación completa de `quickstart.md` (pureza de `domain/` sin imports de Spring/JPA, ausencia de clases en `application/dto` para compatibilidad con `002-backend-dtos`, y compilación + tests en Java 21)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — comienza inmediatamente (`T001` → `T003`).
- **Foundational (Phase 2)**: Depende de Phase 1 — bloquea todas las historias de usuario (`T004` → `T006`).
- **User Story 1 (Phase 3 - P1 🎯 MVP)**: Depende de Phase 2 (`T007` → `T013`).
- **User Story 2 (Phase 4 - P1)**: Depende de Phase 2 y se apoya en las entidades maestras de `US1` para relaciones foráneas (`T014` → `T023`).
- **User Story 3 (Phase 5 - P2)**: Depende de Phase 2 y conecta `scryfall_cards` con el flujo de registro de mazos e inscripciones (`T024` → `T032`).
- **Polish (Phase 6)**: Depende de `US1`, `US2` y `US3` (`T033` → `T035`).

### Parallel Opportunities

- En **Phase 2**: `T005` y `T006` pueden ejecutarse en paralelo con `T004`.
- En **User Story 1**: `T007`, `T008`, `T009`, `T010`, `T011` y `T012` pueden ejecutarse en paralelo antes de ensamblar los adaptadores en `T013`.
- En **User Story 2**: `T014`, `T015`, `T016`, `T017`, `T018`, `T019`, `T020` y `T021` pueden ejecutarse en paralelo antes de `T022`.
- En **User Story 3**: `T024`, `T025`, `T026`, `T027`, `T028` y `T029` pueden ejecutarse en paralelo antes de `T030` y `T031`.

---

## Parallel Example: User Story 1, 2 & 3

```bash
# Ejemplo de tareas paralelizables en User Story 1:
Task: "T007 [P] [US1] Crear modelos de dominio User y UserProfileSummary"
Task: "T008 [P] [US1] Crear modelos de dominio Store, StoreMember y StoreDetailSummary"
Task: "T009 [P] [US1] Crear modelos de dominio Deck y TournamentTemplate"

# Ejemplo de tareas paralelizables en User Story 3:
Task: "T024 [P] [US3] Crear modelos de dominio ScryfallCard, ScryfallCardStats y MetagameAnalyticsSummary"
Task: "T026 [P] [US3] Implementar BudgetValidator en domain/service/BudgetValidator.java"
Task: "T029 [P] [US3] Implementar la configuración y payloads HTTP de Scryfall"
```

---

## Implementation Strategy

### MVP First (User Story 1)

1. Completar Phase 1 (Setup) + Phase 2 (Foundational con migración Flyway `V1`).
2. Completar Phase 3 (`US1`: extracción de `users`, `stores`, `store_members`, `decks`, `tournament_templates`).
3. Validar extracción independiente de entidades maestras.

### Incremental Delivery

1. **Incremento 1 (`US1`)**: Entidades maestras de Tienda, Usuarios, Miembros, Mazos y Plantillas.
2. **Incremento 2 (`US2`)**: Ligas, `v_league_leaderboard`, Torneos, Inscripciones con `JSONB` `DeckSnapshot`, Rondas, Partidas y `SwissPairingEngine`.
3. **Incremento 3 (`US3`)**: Cliente HTTP de Scryfall activado al registrar el mazo, catálogo `scryfall_cards`, `BudgetValidator` y analíticas de metajuego desde `v_scryfall_card_stats`.
4. **Incremento 4 (Polish)**: Prueba de integración de extremo a extremo y validación de compatibilidad limpia con la rama `002-backend-dtos`.

---
description: "Task list for feature 002-tournament-league-logic implementation"
---

# Tasks: Lógica de Negocio de Usuarios, Mazos con Scryfall, Torneos, Ligas, Fórmulas Matemáticas en BD y Estadísticas

**Input**: Design documents from `/specs/002-tournament-league-logic/` (`plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`)

**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), `research.md`, `data-model.md`, `contracts/`

**Organization**: Tasks are grouped by user story (`US1`, `US2`, `US3`) to enable independent implementation and testing of each story while strictly enforcing the Clean/Hexagonal Architecture (`domain/`, `application/`, `infrastructure/`, `presentation/`) defined in `constitution.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths and verbatim constraints from `data-model.md` are included in every task description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, Java 21 + Spring Boot 3.x configuration, and database/environment properties

- [X] T001 Initialize Maven configuration with Java 21, Spring Boot 3.x (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`), Flyway (`flyway-core`, `flyway-database-postgresql`), PostgreSQL JDBC driver, JJWT, and JUnit 5 in `pom.xml`
- [X] T002 Create Spring Boot application entry point in `src/main/java/com/tournament/app/TournamentManagerApplication.java`
- [X] T003 [P] Configure PostgreSQL datasource (`sslmode=require&prepareThreshold=0`), `spring.jpa.hibernate.ddl-auto=validate`, Flyway migration path, JWT properties (`app.jwt.secret`, `app.jwt.expiration-seconds: 3600`), external API timeouts (`moxfield`, `scryfall`), and scheduler thread pool in `src/main/resources/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core database schema migrations (Flyway V1 & V2), stateless JWT security filter chain, RFC 7807 error handling, and external Moxfield/Scryfall HTTP clients

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Create Flyway migration V1 defining base tables `users`, `stores`, `store_members`, `decks`, `scoring_formulas`, `tournament_templates`, `tournaments`, `scryfall_cards`, `tournament_registrations`, `registration_deck_cards`, `rounds`, `matches`, and GIN/B-Tree indexes in `src/main/resources/db/migration/V1__init_store_tournament_schema.sql`
- [X] T005 Create Flyway migration V2 defining `scoring_formulas` (`formula_type IN ('MATCH_POINTS', 'LEAGUE_MATCHDAY_POINTS', 'PAIRING_WEIGHT')`, `expression TEXT NOT NULL`), `leagues` (`format IN ('STANDARD', 'COMMANDER')`, `max_deck_budget NUMERIC(10,2) CHECK (max_deck_budget > 0)`, `status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')`), `league_standings` (`UNIQUE (league_id, user_id)`), `match_participants` (`UNIQUE (match_id, user_id)`, `placement INT NOT NULL DEFAULT 1 CHECK (placement >= 1)`), and column extensions on `decks`, `tournaments` (`UNIQUE (league_id, matchday_number)`), and `tournament_registrations` in `src/main/resources/db/migration/V2__league_formulas_and_business_logic_schema.sql`
- [X] T006 [P] Implement domain exceptions (`DomainValidationException`, `InvalidTournamentStateException`, `IllegalPairingException`, `ResourceNotFoundException`, `UnauthorizedStoreActionException`) in `src/main/java/com/tournament/app/domain/model/DomainExceptions.java`
- [X] T007 [P] Implement RFC 7807 `ProblemDetail` global exception handler (`@RestControllerAdvice`) mapping domain, validation (`@Valid`), and security exceptions in `src/main/java/com/tournament/app/presentation/GlobalExceptionHandler.java`
- [X] T008 [P] Implement JWT token generation and verification provider (`HS256`, claims `sub`, `username`, `roles`, `iat`, `exp`) in `src/main/java/com/tournament/app/infrastructure/security/JwtTokenProvider.java`
- [X] T009 [P] Implement `CustomUserDetailsService` and stateless JWT authentication filter in `src/main/java/com/tournament/app/infrastructure/security/CustomUserDetailsService.java` and `src/main/java/com/tournament/app/infrastructure/security/JwtAuthenticationFilter.java`
- [X] T010 Configure Spring Security 6 `SecurityFilterChain` with `SessionCreationPolicy.STATELESS`, `BCryptPasswordEncoder`, and strict route segregation (`/api/v1/auth/**` and `/api/v1/public/**` -> `permitAll()`, `/api/v1/player/**` -> `hasAnyRole('PLAYER', 'ADMIN')`, `/api/v1/admin/**` -> `hasRole('ADMIN')`) in `src/main/java/com/tournament/app/infrastructure/security/SecurityConfig.java`
- [X] T011 [P] Implement Moxfield HTTP client and response records to extract deck metadata, commander cards, mainboard, and sideboard from a `moxfield_url` (`VARCHAR(500) NOT NULL`, `moxfield_deck_id VARCHAR(64) NOT NULL`) in `src/main/java/com/tournament/app/infrastructure/adapter/moxfield/MoxfieldClient.java` and `src/main/java/com/tournament/app/infrastructure/adapter/moxfield/MoxfieldDeckResponse.java`
- [X] T012 [P] Implement Scryfall HTTP client (`POST https://api.scryfall.com/cards/collection`) and response records mapping `id` (`UUID`), `oracle_id` (`UUID NOT NULL`), `name` (`VARCHAR(200) NOT NULL`), `mana_cost` (`VARCHAR(60)`), `cmc` (`NUMERIC(5,2) NOT NULL`), `type_line` (`VARCHAR(200) NOT NULL`), `colors` (`TEXT[] NOT NULL`), `color_identity` (`TEXT[] NOT NULL`), `rarity` (`VARCHAR(20) NOT NULL`), `legalities` (`JSONB NOT NULL`), and `price_eur`/`price_usd` (`NUMERIC(10,2)`) in `src/main/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallClient.java` and `src/main/java/com/tournament/app/infrastructure/adapter/scryfall/ScryfallCardResponse.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Registro de Usuarios, Inicio de Sesión y Gestión de Mazos en Perfil con Scryfall (Priority: P1) 🎯 MVP

**Goal**: Permitir registrar usuarios, iniciar sesión (login), guardar mazos en el perfil sincronizados con Scryfall e inscribirse en un torneo añadiendo un mazo nuevo en el momento o seleccionando uno ya guardado en el perfil, validando formato/presupuesto y guardando el `deck_snapshot` (`JSONB`) inmutable.

**Independent Test**: Registrar un usuario (`POST /api/v1/auth/register`), hacer login (`POST /api/v1/auth/login`), guardar un mazo en su perfil (`POST /api/v1/player/decks`) verificando metadatos de Scryfall (`commanderName`, `colorIdentity`, `lastCalculatedPrice`), e inscribirse a un torneo (`POST /api/v1/player/tournaments/{id}/register`) usando tanto `savedDeckId` como `newDeck` con `saveToProfile: true`.

### Implementation for User Story 1

- [X] T013 [P] [US1] Create pure domain model `User` (`id UUID`, `username VARCHAR(60) NOT NULL UNIQUE`, `passwordHash VARCHAR(255) NOT NULL`, `role IN ('PLAYER', 'ADMIN')`, `createdAt TIMESTAMPTZ`) in `src/main/java/com/tournament/app/domain/model/User.java`
- [X] T014 [P] [US1] Create pure domain model `ScryfallCard` (`id UUID`, `oracleId UUID NOT NULL`, `name VARCHAR(200) NOT NULL`, `manaCost VARCHAR(60)`, `cmc BigDecimal`, `typeLine VARCHAR(200) NOT NULL`, `colors List<String>`, `colorIdentity List<String>`, `rarity VARCHAR(20) NOT NULL`, `legalities Map<String, String>`, `priceEur BigDecimal`, `priceUsd BigDecimal`) in `src/main/java/com/tournament/app/domain/model/ScryfallCard.java`
- [X] T015 [P] [US1] Create pure domain model `Deck` and immutable `DeckSnapshot` record (`id UUID`, `userId UUID NOT NULL`, `name VARCHAR(150) NOT NULL`, `moxfieldUrl VARCHAR(500) NOT NULL`, `moxfieldDeckId VARCHAR(64) NOT NULL`, `format IN ('STANDARD', 'COMMANDER')`, `commanderOracleId UUID`, `commanderName VARCHAR(200)`, `colorIdentity List<String>`, `lastCalculatedPrice BigDecimal`, `UNIQUE (user_id, moxfield_deck_id)`) in `src/main/java/com/tournament/app/domain/model/Deck.java` and `src/main/java/com/tournament/app/domain/model/DeckSnapshot.java`
- [X] T016 [P] [US1] Create pure domain model `TournamentRegistration` (`id UUID`, `tournamentId UUID NOT NULL`, `userId UUID NOT NULL`, `deckId UUID NOT NULL`, `status IN ('REGISTERED', 'CHECKED_IN', 'DROPPED', 'DISQUALIFIED')`, `calculatedDeckPrice NUMERIC(10,2) >= 0`, `commanderName VARCHAR(200)`, `commanderOracleId UUID`, `colorIdentity List<String>`, `tournamentPoints NUMERIC(10,2)`, `calculatedLeagueMatchdayPoints NUMERIC(10,2)`, `deckSnapshot DeckSnapshot NOT NULL`, `UNIQUE (tournament_id, user_id)`) in `src/main/java/com/tournament/app/domain/model/TournamentRegistration.java`
- [X] T017 [US1] Implement pure domain service `BudgetValidator` to verify format legality (`STANDARD`: >=60 mainboard, <=15 sideboard, <=4 copies per `oracle_id`; `COMMANDER`: 100 cards singleton, `color_identity` subset of commander) and budget cap (`calculatedDeckPrice <= maxDeckBudget`) using `ScryfallCard` metadata in `src/main/java/com/tournament/app/domain/service/BudgetValidator.java`
- [X] T018 [P] [US1] Define domain repository ports `UserRepository`, `DeckRepository`, `ScryfallCardRepository`, and `RegistrationRepository` in `src/main/java/com/tournament/app/domain/repository/UserRepository.java`, `src/main/java/com/tournament/app/domain/repository/DeckRepository.java`, `src/main/java/com/tournament/app/domain/repository/ScryfallCardRepository.java`, and `src/main/java/com/tournament/app/domain/repository/RegistrationRepository.java`
- [X] T019 [P] [US1] Create JPA entities `UserEntity`, `DeckEntity`, `ScryfallCardEntity`, `TournamentRegistrationEntity` (with `@JdbcTypeCode(SqlTypes.JSON)` on `deck_snapshot JSONB NOT NULL`), and `RegistrationDeckCardEntity` (`board_section IN ('COMMANDER', 'MAINBOARD', 'SIDEBOARD')`, `quantity INT > 0`, `unit_price_snapshot NUMERIC(10,2) >= 0`) in `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/`
- [X] T020 [US1] Implement Spring Data JPA repository adapters for `UserRepository`, `DeckRepository`, `ScryfallCardRepository`, and `RegistrationRepository` in `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/`
- [X] T021 [P] [US1] Create authentication and player deck/registration DTO records (`RegisterUserRequest`, `LoginRequest`, `AuthTokenResponse`, `SaveProfileDeckRequest`, `ProfileDeckResponse`, `TournamentRegistrationRequest`, `TournamentRegistrationResponse`) in `src/main/java/com/tournament/app/application/dto/AuthDtos.java` and `src/main/java/com/tournament/app/application/dto/PlayerDeckDtos.java`
- [X] T022 [US1] Implement `RegisterUserUseCase` and `AuthenticateUserUseCase` in `src/main/java/com/tournament/app/application/usecase/RegisterUserUseCase.java` and `src/main/java/com/tournament/app/application/usecase/AuthenticateUserUseCase.java`
- [X] T023 [US1] Implement `SaveProfileDeckUseCase` (fetching deck from `MoxfieldClient`, syncing cards via `ScryfallClient` into `scryfall_cards`, and saving `Deck` in the user's profile) in `src/main/java/com/tournament/app/application/usecase/SaveProfileDeckUseCase.java`
- [X] T024 [US1] Implement `RegisterPlayerUseCase` supporting both `savedDeckId` (from user profile) and `newDeck` (with `saveToProfile` flag), validating format/budget via `BudgetValidator` + `ScryfallCardRepository` and persisting the immutable `deck_snapshot` (`JSONB`) and `registration_deck_cards` in `src/main/java/com/tournament/app/application/usecase/RegisterPlayerUseCase.java`
- [X] T025 [P] [US1] Implement `AuthController` (`POST /api/v1/auth/register`, `POST /api/v1/auth/login`) in `src/main/java/com/tournament/app/presentation/AuthController.java`
- [X] T026 [US1] Implement profile deck and tournament registration endpoints (`POST /api/v1/player/decks`, `GET /api/v1/player/decks`, `DELETE /api/v1/player/decks/{deckId}`, `POST /api/v1/player/tournaments/{tournamentId}/register`) with IDOR ownership checks in `src/main/java/com/tournament/app/presentation/PlayerTournamentController.java`

**Checkpoint**: User Story 1 is complete and independently testable (users can register, log in, save Scryfall-connected decks in their profile, and join tournaments with a saved or new deck).

---

## Phase 4: User Story 2 - Creación de Torneos y Ligas con Configuración de Partidas y Fórmulas Matemáticas (Priority: P1)

**Goal**: Permitir al dueño de la tienda registrar torneos (indicando cuántas partidas `maxRounds` hay por torneo) y ligas por jornadas, guardar y editar en base de datos las fórmulas matemáticas (puntos por partida, puntos por jornada de liga y *pairings*), generar los emparejamientos de cada jornada y registrar los resultados de cada partida recalculando automáticamente los puntos del torneo y de la liga.

**Independent Test**: Como dueño de tienda (`ROLE_ADMIN`), crear las 3 fórmulas matemáticas en BD (`POST /api/v1/admin/stores/{storeId}/formulas`), crear una liga (`POST /api/v1/admin/stores/{storeId}/leagues`) y una jornada con `maxRounds = 4` (`POST /api/v1/admin/stores/{storeId}/tournaments`), generar *pairings* (`POST /api/v1/admin/tournaments/{id}/rounds/generate`) y registrar el resultado de una mesa (`POST /api/v1/admin/matches/{matchId}/result`), verificando la actualización inmediata de `match_points_earned`, `tournament_points` y `total_league_points`.

### Implementation for User Story 2

- [X] T027 [P] [US2] Create pure domain models `Store` (`id UUID`, `name VARCHAR(120) NOT NULL UNIQUE`, `ownerId UUID NOT NULL`, `currency IN ('EUR', 'USD')`) and `ScoringFormula` (`id UUID`, `storeId UUID NOT NULL`, `name VARCHAR(120) NOT NULL`, `formulaType IN ('MATCH_POINTS', 'LEAGUE_MATCHDAY_POINTS', 'PAIRING_WEIGHT')`, `expression TEXT NOT NULL`, `description TEXT`, `updatedAt TIMESTAMPTZ`) in `src/main/java/com/tournament/app/domain/model/Store.java`, `src/main/java/com/tournament/app/domain/model/FormulaType.java`, and `src/main/java/com/tournament/app/domain/model/ScoringFormula.java`
- [X] T028 [P] [US2] Create pure domain models `League` (`id UUID`, `storeId UUID NOT NULL`, `name VARCHAR(150) NOT NULL`, `format IN ('STANDARD', 'COMMANDER')`, `maxDeckBudget NUMERIC(10,2) > 0`, `matchPointsFormulaId UUID NOT NULL`, `matchdayPointsFormulaId UUID NOT NULL`, `pairingFormulaId UUID NOT NULL`, `status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')`) and `LeagueStanding` (`leagueId UUID`, `userId UUID`, `totalLeaguePoints BigDecimal`, `matchdaysPlayed int`, `totalMatchWins int`, `totalMatchesPlayed int`) in `src/main/java/com/tournament/app/domain/model/League.java` and `src/main/java/com/tournament/app/domain/model/LeagueStanding.java`
- [X] T029 [P] [US2] Create pure domain models `Tournament` (`id UUID`, `storeId UUID NOT NULL`, `leagueId UUID nullable`, `matchdayNumber Integer nullable > 0`, `name VARCHAR(150) NOT NULL`, `scheduledAt TIMESTAMPTZ NOT NULL`, `entryFee NUMERIC(10,2) >= 0`, `prizeDescription TEXT NOT NULL`, `format IN ('STANDARD', 'COMMANDER')`, `maxDeckBudget NUMERIC(10,2) > 0`, `status IN ('DRAFT', 'REGISTRATION_OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')`, `currentRound int`, `maxRounds int > 0`, `matchPointsFormulaId UUID`, `pairingFormulaId UUID`), `Round`, `Match`, and `MatchParticipant` (`placement int >= 1`, `wins int >= 0`, `losses int >= 0`, `draws int >= 0`, `kills int >= 0`, `matchPointsEarned BigDecimal`) in `src/main/java/com/tournament/app/domain/model/Tournament.java`, `src/main/java/com/tournament/app/domain/model/Round.java`, `src/main/java/com/tournament/app/domain/model/Match.java`, and `src/main/java/com/tournament/app/domain/model/MatchParticipant.java`
- [X] T030 [US2] Implement pure domain service `FormulaEvaluationEngine` (recursive-descent mathematical parser supporting `+`, `-`, `*`, safe `/` with division-by-zero returning `0.0`, parentheses, `min`, `max`, and domain variables from `Map<String, Double>`) in `src/main/java/com/tournament/app/domain/service/FormulaEvaluationEngine.java`
- [X] T031 [US2] Implement pure domain service `SwissPairingEngine` evaluating the store owner's `PAIRING_WEIGHT` formula via `FormulaEvaluationEngine`, grouping players into 2-player duels (`STANDARD`) or 4-player pods (`COMMANDER`, with 3-player pod / single-BYE handling for odd counts), avoiding repeat opponents, and enforcing `currentRound < maxRounds` in `src/main/java/com/tournament/app/domain/service/SwissPairingEngine.java`
- [X] T032 [US2] Implement pure domain service `LeagueScoringService` calculating `matchPointsEarned` per participant via the `MATCH_POINTS` formula, updating matchday `tournamentPoints`, and recalculating `calculatedLeagueMatchdayPoints` and cumulative `LeagueStanding` via the `LEAGUE_MATCHDAY_POINTS` formula in `src/main/java/com/tournament/app/domain/service/LeagueScoringService.java`
- [X] T033 [P] [US2] Define domain repository ports `StoreRepository`, `ScoringFormulaRepository`, `LeagueRepository`, `TournamentRepository`, and `MatchRepository` in `src/main/java/com/tournament/app/domain/repository/`
- [X] T034 [P] [US2] Create JPA entities `StoreEntity`, `ScoringFormulaEntity`, `LeagueEntity`, `LeagueStandingEntity`, `TournamentEntity`, `RoundEntity`, `MatchEntity`, and `MatchParticipantEntity` in `src/main/java/com/tournament/app/infrastructure/adapter/persistence/jpa/`
- [X] T035 [US2] Implement Spring Data JPA repository adapters for `StoreRepository`, `ScoringFormulaRepository`, `LeagueRepository`, `TournamentRepository`, and `MatchRepository` in `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/`
- [X] T036 [P] [US2] Create admin, tournament, league, formula, pairing, and match result DTO records (`CreateStoreRequest`, `UpsertScoringFormulaRequest`, `ScoringFormulaResponse`, `CreateLeagueRequest`, `LeagueResponse`, `CreateTournamentRequest`, `TournamentResponse`, `RoundPairingsResponse`, `RecordMatchResultRequest`, `StandingsResponse`) in `src/main/java/com/tournament/app/application/dto/AdminTournamentDtos.java`
- [X] T037 [US2] Implement `ManageScoringFormulaUseCase` (create, update, and list store formulas in DB verifying `store.ownerId == currentUserId` and validating formula syntax with `FormulaEvaluationEngine`) in `src/main/java/com/tournament/app/application/usecase/ManageScoringFormulaUseCase.java`
- [X] T038 [US2] Implement `CreateLeagueUseCase` and `CreateTournamentUseCase` (enforcing `maxRounds > 0` set by the organizer and binding the store's `scoring_formulas`) in `src/main/java/com/tournament/app/application/usecase/CreateLeagueUseCase.java` and `src/main/java/com/tournament/app/application/usecase/CreateTournamentUseCase.java`
- [X] T039 [US2] Implement `GeneratePairingsUseCase` (loading the tournament's `PAIRING_WEIGHT` formula from DB, invoking `SwissPairingEngine`, and persisting the new `Round`, `Match`, and `MatchParticipant` records up to `maxRounds`) in `src/main/java/com/tournament/app/application/usecase/GeneratePairingsUseCase.java`
- [X] T040 [US2] Implement `ReportMatchResultUseCase` restricted exclusively to the store owner/organizer (`ROLE_ADMIN` + `store.ownerId == currentUserId`), applying `LeagueScoringService` to update match points, matchday points, and cumulative `league_standings` in `src/main/java/com/tournament/app/application/usecase/ReportMatchResultUseCase.java`
- [X] T041 [P] [US2] Implement `ScheduleTournamentUseCase` and `TournamentAutoScheduler` (`@Scheduled`) for idempotent recurring tournament generation in `src/main/java/com/tournament/app/application/usecase/ScheduleTournamentUseCase.java` and `src/main/java/com/tournament/app/infrastructure/scheduler/TournamentAutoScheduler.java`
- [X] T042 [US2] Implement `AdminTournamentController` (`/api/v1/admin/stores/**`, `/api/v1/admin/tournaments/{id}/rounds/generate`, `/api/v1/admin/matches/{matchId}/result`) and public tournament/league standings queries (`/api/v1/public/tournaments/**`, `/api/v1/public/leagues/**`) in `src/main/java/com/tournament/app/presentation/AdminTournamentController.java`

**Checkpoint**: User Stories 1 AND 2 are complete and independently testable (store owner can manage DB mathematical formulas, create leagues and tournaments with custom round counts, generate pairings, and record match results that progressively calculate tournament and league points).

---

## Phase 5: User Story 3 - Estadísticas de Jornada (Metajuego y Enfrentamientos) y Estadísticas de "Tu Mazo" con Scryfall (Priority: P2)

**Goal**: Calcular y mostrar en cada jornada (y en el acumulado de liga/histórico) las estadísticas de mazos conectadas con Scryfall: cartas más jugadas, colores más jugados, comandantes más ganadores, enfrentamientos directos de rivales (*"Magda vs Kefka: gana Kefka 60%"*) y las estadísticas individuales de *"Tu Mazo"*.

**Independent Test**: Consultar `GET /api/v1/public/analytics/tournaments/{tournamentId}` (y `/leagues/{leagueId}`) y `GET /api/v1/player/decks/{deckId}/analytics`, verificando el ranking de cartas más jugadas, distribución de colores, comandantes más ganadores, matriz de enfrentamientos entre comandantes rivales y métricas de *"Tu Mazo"*.

### Implementation for User Story 3

- [X] T043 [P] [US3] Create analytics domain value objects and response DTO records (`MatchdayDeckAnalyticsResponse`, `MostPlayedCardStat`, `CommanderWinStat`, `CommanderMatchupStat`, `PersonalDeckAnalyticsResponse`, `RivalMatchupStat`, `ScryfallDeckCompositionStat`) in `src/main/java/com/tournament/app/application/dto/AnalyticsDtos.java`
- [X] T044 [US3] Implement pure domain service `DeckAnalyticsService` computing: (1) most played cards frequency/percentage, (2) color and color identity distribution (`W`, `U`, `B`, `R`, `G`), (3) winningest commanders by win rate and average points, (4) head-to-head commander/rival matchups with dominant win percentage and summary label (e.g., `"Magda vs Kefka: gana Kefka 60%"`), and (5) personal deck analytics (*"Tu Mazo"* win rate, average match/matchday points, rival commander breakdown, and Scryfall CMC/color/type comparison against matchday average) in `src/main/java/com/tournament/app/domain/service/DeckAnalyticsService.java`
- [X] T045 [US3] Implement analytical query methods in persistence adapters (`RegistrationRepository` and `MatchRepository`) to fetch deck card distributions joined with `scryfall_cards` and confirmed `match_participants` filtered by `tournamentId`, `leagueId`, or `(userId, deckId)` in `src/main/java/com/tournament/app/infrastructure/adapter/persistence/repository/`
- [X] T046 [US3] Implement `GetDeckAnalyticsUseCase` orchestrating matchday analytics, cumulative league analytics, and authenticated player *"Tu Mazo"* analytics in `src/main/java/com/tournament/app/application/usecase/GetDeckAnalyticsUseCase.java`
- [X] T047 [US3] Implement public matchday/league analytics endpoints (`GET /api/v1/public/analytics/tournaments/{tournamentId}`, `GET /api/v1/public/analytics/leagues/{leagueId}`) in `src/main/java/com/tournament/app/presentation/AnalyticsController.java` and player personal deck analytics endpoint (`GET /api/v1/player/decks/{deckId}/analytics`) in `src/main/java/com/tournament/app/presentation/PlayerTournamentController.java`

**Checkpoint**: All user stories (`US1`, `US2`, `US3`) are independently functional and complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Quality Gates verification required by `constitution.md` (pure domain unit tests, architectural purity check, and role-based security verification)

- [X] T048 [P] Implement pure unit tests for `FormulaEvaluationEngine` (custom expressions, precedence, division-by-zero protection, invalid syntax validation) and `LeagueScoringService` in `src/test/java/com/tournament/app/domain/service/FormulaEvaluationEngineTest.java`
- [X] T049 [P] Implement pure unit tests for `SwissPairingEngine` (custom pairing formula ordering, no repeat matchups, odd player BYE / Commander pod sizing, `maxRounds` limit) and `BudgetValidator` (Standard/Commander legality and budget cap with Scryfall metadata) in `src/test/java/com/tournament/app/domain/service/SwissPairingEngineTest.java` and `src/test/java/com/tournament/app/domain/service/BudgetValidatorTest.java`
- [X] T050 [P] Implement pure unit tests for `DeckAnalyticsService` verifying most played cards, color distribution, winningest commanders, head-to-head matchups (`"Magda vs Kefka: gana Kefka 60%"`), and *"Tu Mazo"* calculations in `src/test/java/com/tournament/app/domain/service/DeckAnalyticsServiceTest.java`
- [X] T051 Verify Clean Architecture purity (`domain/` contains zero `org.springframework.*` or `jakarta.persistence.*` imports) and implement Spring Security RBAC test verifying `401`/`403`/`200` across `/api/v1/public/**`, `/api/v1/player/**`, and `/api/v1/admin/**` (including ensuring only `ROLE_ADMIN` can post match results) in `src/test/java/com/tournament/app/presentation/SecurityRbacTest.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup (`T001`–`T003`)**: No dependencies — starts immediately.
- **Phase 2: Foundational (`T004`–`T012`)**: Depends on Phase 1 — **BLOCKS** all user stories.
- **Phase 3: User Story 1 (`T013`–`T026`, P1 🎯 MVP)**: Depends on Phase 2.
- **Phase 4: User Story 2 (`T027`–`T042`, P1)**: Depends on Phase 2 and integrates with `User`/`TournamentRegistration` from `US1`.
- **Phase 5: User Story 3 (`T043`–`T047`, P2)**: Depends on `US1` (deck snapshots + Scryfall cards) and `US2` (`match_participants` results).
- **Phase 6: Polish & Quality Gates (`T048`–`T051`)**: Runs after domain services and controllers are implemented.

### Parallel Opportunities

- **Setup & Foundational**: `T006`, `T007`, `T008`, `T009`, `T011`, and `T012` operate on separate files and can be executed in parallel after `T004`–`T005`.
- **User Story 1 (`US1`)**: Domain models `T013`–`T016` can run in parallel; repository ports `T018`, JPA entities `T019`, and DTOs `T021` can run in parallel.
- **User Story 2 (`US2`)**: Domain models `T027`–`T029` can run in parallel; repository ports `T033`, JPA entities `T034`, and DTOs `T036` can run in parallel.
- **User Story 3 (`US3`) & Polish**: Unit tests `T048`, `T049`, `T050`, and `T051` can run in parallel.

---

## Parallel Example: User Story 2

```bash
# Execute pure domain models for User Story 2 in parallel:
Task T027: "Create pure domain models Store and ScoringFormula in src/main/java/com/tournament/app/domain/model/..."
Task T028: "Create pure domain models League and LeagueStanding in src/main/java/com/tournament/app/domain/model/..."
Task T029: "Create pure domain models Tournament, Round, Match, and MatchParticipant in src/main/java/com/tournament/app/domain/model/..."
```

---

## Implementation Strategy

### MVP First (User Story 1 + User Story 2 Core P1)

1. Complete **Phase 1: Setup** (`T001`–`T003`) and **Phase 2: Foundational** (`T004`–`T012`).
2. Complete **Phase 3: User Story 1** (`T013`–`T026`) — Users can register, log in, save decks in their profile with Scryfall sync, and register for tournaments with a saved or new deck.
3. Complete **Phase 4: User Story 2** (`T027`–`T042`) — Store owners can manage mathematical formulas in the DB, create leagues and tournaments with custom round counts, generate pairings, and record match results to progressively calculate tournament and league points.
4. Complete **Phase 5: User Story 3** (`T043`–`T047`) — Matchday, League, Commander head-to-head matchups (*"Magda vs Kefka"*), and *"Tu Mazo"* analytics.
5. Complete **Phase 6: Quality Gates** (`T048`–`T051`) and verify the full build.

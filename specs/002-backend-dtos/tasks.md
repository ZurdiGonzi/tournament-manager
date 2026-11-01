# Tasks: Contratos y DTOs del Backend

**Input**: Design documents from `specs/002-backend-dtos/` (`spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/dto-records-contract.md`, `quickstart.md`)

**Prerequisites**: `plan.md` (required), `spec.md` (required), `data-model.md`, `contracts/`

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., [US1], [US2], [US3], [US4])
- File paths are relative to repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Initialize Maven build configuration with Java 21, Spring Boot 3.x, `spring-boot-starter-validation`, `spring-boot-starter-web`, `jackson-datatype-jsr310` and testing dependencies in `pom.xml`
- [x] T002 [P] Create and verify package structure directory tree under `src/main/java/com/tournament/app/domain/model` and `src/main/java/com/tournament/app/application/dto`
- [x] T003 [P] Configure JUnit 5 test source directories and Jackson test harness under `src/test/java/com/tournament/app/application/dto`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain enums and common error DTOs that MUST be complete before ANY user story DTO can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T004 [P] Create `UserRole` enum with values `PLAYER`, `ADMIN` in `src/main/java/com/tournament/app/domain/model/UserRole.java`
- [x] T005 [P] Create `TournamentFormat` enum with values `STANDARD`, `COMMANDER`, `MODERN`, `PAUPER` in `src/main/java/com/tournament/app/domain/model/TournamentFormat.java`
- [x] T006 [P] Create `TournamentStatus` enum with values `DRAFT`, `REGISTRATION_OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` in `src/main/java/com/tournament/app/domain/model/TournamentStatus.java`
- [x] T007 [P] Create `RegistrationStatus` enum with values `CONFIRMED`, `CANCELLED`, `DROPPED` in `src/main/java/com/tournament/app/domain/model/RegistrationStatus.java`
- [x] T008 [P] Create `RoundStatus` enum with values `ACTIVE`, `FINISHED` in `src/main/java/com/tournament/app/domain/model/RoundStatus.java`
- [x] T009 [P] Create `MatchStatus` enum with values `PENDING`, `SUBMITTED`, `CONFIRMED`, `DISPUTED` in `src/main/java/com/tournament/app/domain/model/MatchStatus.java`
- [x] T010 [P] Create `FieldErrorDto` record (`field`, `rejectedValue`, `message`) in `src/main/java/com/tournament/app/application/dto/common/FieldErrorDto.java`
- [x] T011 Create RFC 7807 `ApiErrorResponse` record (`type`, `title`, `status`, `detail`, `instance`, `timestamp`, `List<FieldErrorDto> invalidParams`) in `src/main/java/com/tournament/app/application/dto/common/ApiErrorResponse.java`

**Checkpoint**: Foundation ready - all shared enums and common error models are defined; user story implementation can now begin in parallel.

---

## Phase 3: User Story 1 - Autenticación, Gestión de Identidad y Tiendas (Priority: P1) 🎯 MVP

**Goal**: Proveer DTOs tipados e inmutables para registro, login, entrega de tokens JWT, perfiles de usuario y gestión de tiendas físicas.

**Independent Test**: Ejecutar pruebas unitarias de validación Jakarta y serialización JSON para `RegisterRequest` (incluyendo rol por defecto `PLAYER` y regex de username), `LoginRequest`, `AuthResponse`, `UserSummaryResponse`, `UserProfileResponse`, `CreateStoreRequest`, `StoreSummaryResponse` y `StoreDetailResponse`.

### Tests for User Story 1

- [x] T012 [P] [US1] Unit test for Auth DTO validation constraints (`@NotBlank`, `@Size`, `@Pattern`, `@Email`, compact constructor default `PLAYER`) in `src/test/java/com/tournament/app/application/dto/auth/AuthDtoValidationTest.java`
- [x] T013 [P] [US1] Unit test for Store DTO validation constraints (`@NotBlank`, `@Size`) and JSON serialization in `src/test/java/com/tournament/app/application/dto/store/StoreDtoValidationTest.java`

### Implementation for User Story 1

- [x] T014 [P] [US1] Implement `RegisterRequest` record with `@NotBlank`, `@Size(min = 3, max = 30)`, `@Pattern(regexp = "^[a-zA-Z0-9._-]+$")`, `@Email`, `@Size(min = 8, max = 100)` and compact constructor defaulting `role` to `UserRole.PLAYER` in `src/main/java/com/tournament/app/application/dto/auth/RegisterRequest.java`
- [x] T015 [P] [US1] Implement `LoginRequest` record with `@NotBlank` for `usernameOrEmail` and `password` in `src/main/java/com/tournament/app/application/dto/auth/LoginRequest.java`
- [x] T016 [P] [US1] Implement `UserSummaryResponse` record (`id`, `username`, `email`, `Set<UserRole> roles`, `Instant createdAt`) in `src/main/java/com/tournament/app/application/dto/auth/UserSummaryResponse.java`
- [x] T017 [P] [US1] Implement `AuthResponse` record (`accessToken`, `tokenType`, `expiresInSeconds`, `UserSummaryResponse user`) in `src/main/java/com/tournament/app/application/dto/auth/AuthResponse.java`
- [x] T018 [P] [US1] Implement `UserProfileResponse` record (`id`, `username`, `email`, `Set<UserRole> roles`, `int totalDecks`, `int totalTournamentsPlayed`, `Instant createdAt`) in `src/main/java/com/tournament/app/application/dto/auth/UserProfileResponse.java`
- [x] T019 [P] [US1] Implement `CreateStoreRequest` record with `@NotBlank`, `@Size(min = 2, max = 100)` for name and `@Size(max = 500)` for description in `src/main/java/com/tournament/app/application/dto/store/CreateStoreRequest.java`
- [x] T020 [P] [US1] Implement `StoreSummaryResponse` record (`id`, `name`, `ownerUsername`, `int totalTournaments`) in `src/main/java/com/tournament/app/application/dto/store/StoreSummaryResponse.java`
- [x] T021 [US1] Implement `StoreDetailResponse` record (`id`, `name`, `description`, `address`, `UserSummaryResponse owner`, `int totalTournaments`, `int totalMembers`, `Instant createdAt`) in `src/main/java/com/tournament/app/application/dto/store/StoreDetailResponse.java`

**Checkpoint**: At this point, User Story 1 DTOs are fully functional, compiled in Java 21, and testable independently.

---

## Phase 4: User Story 2 - Mazos Moxfield, Torneos e Inscripción con Snapshot (Priority: P1)

**Goal**: Proveer DTOs para vinculación de mazos por URL de Moxfield, definición de torneos con restricciones de formato/presupuesto, inscripción y captura del snapshot inmutable en JSONB con cartas de Scryfall.

**Independent Test**: Ejecutar pruebas unitarias para validación de URL de Moxfield (`@Pattern`), restricciones de torneo (`@DecimalMin("0.00")`, `@Future`, aforo entre 4 y 256), y serialización exacta de `DeckSnapshotDto` con sus cartas `DeckCardDto`.

### Tests for User Story 2

- [x] T022 [P] [US2] Unit test for Deck DTOs validation (`moxfieldUrl` regex pattern, `quantity >= 1`) in `src/test/java/com/tournament/app/application/dto/deck/DeckDtoValidationTest.java`
- [x] T023 [P] [US2] Unit test for Tournament DTOs validation (`@Future scheduledAt`, `entryFeeEur >= 0.00`, `maxCapacity` 4-256, `@Valid restrictions`) in `src/test/java/com/tournament/app/application/dto/tournament/TournamentDtoValidationTest.java`
- [x] T024 [P] [US2] Unit test for `DeckSnapshotDto` and `TournamentRegistrationResponse` JSON serialization and round-trip fidelity in `src/test/java/com/tournament/app/application/dto/registration/RegistrationDtoSerializationTest.java`

### Implementation for User Story 2

- [x] T025 [P] [US2] Implement `RegisterDeckRequest` record with `@NotBlank` and `@Pattern(regexp = "^https://(www\\.)?moxfield\\.com/decks/[a-zA-Z0-9_-]+$")` in `src/main/java/com/tournament/app/application/dto/deck/RegisterDeckRequest.java`
- [x] T026 [P] [US2] Implement `DeckSummaryResponse` record (`id`, `name`, `moxfieldUrl`, `moxfieldId`, `Instant createdAt`) in `src/main/java/com/tournament/app/application/dto/deck/DeckSummaryResponse.java`
- [x] T027 [P] [US2] Implement `DeckCardDto` record (`scryfallId`, `oracleId`, `name`, `@Min(1) int quantity`, `manaCost`, `manaValue`, `typeLine`, `colors`, `colorIdentity`, `unitPriceEur`, `isCommander`) in `src/main/java/com/tournament/app/application/dto/deck/DeckCardDto.java`
- [x] T028 [US2] Implement `DeckSnapshotDto` record (`deckId`, `deckName`, `moxfieldUrl`, `TournamentFormat format`, `BigDecimal totalPriceEur`, `Instant capturedAt`, `List<DeckCardDto> cards`) in `src/main/java/com/tournament/app/application/dto/deck/DeckSnapshotDto.java`
- [x] T029 [P] [US2] Implement `DeckValidationFailureResponse` record (`reason`, `maxAllowedBudgetEur`, `evaluatedBudgetEur`, `List<String> violatingCards`) in `src/main/java/com/tournament/app/application/dto/deck/DeckValidationFailureResponse.java`
- [x] T030 [P] [US2] Implement `TournamentRestrictionsDto` record with `@NotNull TournamentFormat format` and `@DecimalMin(value = "0.00") BigDecimal maxBudgetEur` in `src/main/java/com/tournament/app/application/dto/tournament/TournamentRestrictionsDto.java`
- [x] T031 [US2] Implement `CreateTournamentRequest` record with `@NotNull storeId`, `@NotBlank name`, `@Future scheduledAt`, `@DecimalMin("0.00") entryFeeEur`, `@NotBlank prizeDescription`, `@Min(4) @Max(256) maxCapacity`, and `@NotNull @Valid restrictions` in `src/main/java/com/tournament/app/application/dto/tournament/CreateTournamentRequest.java`
- [x] T032 [P] [US2] Implement `TournamentSummaryResponse` record (`id`, `storeId`, `storeName`, `name`, `status`, `scheduledAt`, `entryFeeEur`, `prizeDescription`, `registeredCount`, `maxCapacity`, `restrictions`) in `src/main/java/com/tournament/app/application/dto/tournament/TournamentSummaryResponse.java`
- [x] T033 [US2] Implement `TournamentDetailResponse` record (`id`, `StoreSummaryResponse store`, `name`, `status`, `scheduledAt`, `entryFeeEur`, `prizeDescription`, `maxCapacity`, `registeredCount`, `restrictions`, `currentRoundNumber`, `totalRounds`, `createdAt`) in `src/main/java/com/tournament/app/application/dto/tournament/TournamentDetailResponse.java`
- [x] T034 [P] [US2] Implement `TournamentRegistrationRequest` record with `@NotNull tournamentId` and `@NotNull deckId` in `src/main/java/com/tournament/app/application/dto/registration/TournamentRegistrationRequest.java`
- [x] T035 [US2] Implement `TournamentRegistrationResponse` record (`id`, `tournamentId`, `userId`, `username`, `status`, `registeredAt`, `DeckSnapshotDto deckSnapshot`) in `src/main/java/com/tournament/app/application/dto/registration/TournamentRegistrationResponse.java`

**Checkpoint**: At this point, User Stories 1 AND 2 DTOs are fully functional and independently testable.

---

## Phase 5: User Story 3 - Visualización de Rondas, Reporte de Partidas y Clasificación (Priority: P2)

**Goal**: Proveer DTOs para emparejamientos de ronda con soporte de BYE, reporte bilateral de partidas con límites de victorias (0-2), resolución de disputas y tablas de clasificación suiza con coeficientes de desempate.

**Independent Test**: Ejecutar pruebas unitarias para validación de `ReportMatchResultRequest` (`player1Wins` 0-2, `player2Wins` 0-2, `draws` 0-3), serialización de emparejamiento con BYE en `MatchPairingResponse`, y cálculo de desempate en `StandingEntryResponse`.

### Tests for User Story 3

- [x] T036 [P] [US3] Unit test for `ReportMatchResultRequest` validation constraints (`@Min(0)`, `@Max(2)` wins, `@Max(3)` draws) in `src/test/java/com/tournament/app/application/dto/round/MatchResultDtoValidationTest.java`
- [x] T037 [P] [US3] Unit test for `MatchPairingResponse` BYE handling and `TournamentStandingsResponse` serialization in `src/test/java/com/tournament/app/application/dto/round/RoundDtoSerializationTest.java`

### Implementation for User Story 3

- [x] T038 [P] [US3] Implement `MatchPairingResponse` record (`matchId`, `tableNumber`, `UserSummaryResponse player1`, `UserSummaryResponse player2`, `boolean isBye`, `MatchStatus status`, `player1Wins`, `player2Wins`, `draws`) in `src/main/java/com/tournament/app/application/dto/round/MatchPairingResponse.java`
- [x] T039 [US3] Implement `RoundDetailResponse` record (`roundId`, `tournamentId`, `int roundNumber`, `RoundStatus status`, `List<MatchPairingResponse> pairings`) in `src/main/java/com/tournament/app/application/dto/round/RoundDetailResponse.java`
- [x] T040 [P] [US3] Implement `ReportMatchResultRequest` record with `@NotNull matchId`, `@Min(0) @Max(2) int player1Wins`, `@Min(0) @Max(2) int player2Wins`, `@Min(0) @Max(3) int draws` in `src/main/java/com/tournament/app/application/dto/round/ReportMatchResultRequest.java`
- [x] T041 [P] [US3] Implement `MatchResultResponse` record (`matchId`, `MatchStatus status`, `reportedByUserId`, `player1Wins`, `player2Wins`, `draws`, `boolean resolvedByAdmin`) in `src/main/java/com/tournament/app/application/dto/round/MatchResultResponse.java`
- [x] T042 [P] [US3] Implement `StandingEntryResponse` record (`int rank`, `userId`, `username`, `int matchPoints`, `String matchRecord`, `BigDecimal omwPercentage`, `BigDecimal gwPercentage`) in `src/main/java/com/tournament/app/application/dto/round/StandingEntryResponse.java`
- [x] T043 [US3] Implement `TournamentStandingsResponse` record (`tournamentId`, `int roundNumber`, `List<StandingEntryResponse> standings`) in `src/main/java/com/tournament/app/application/dto/round/TournamentStandingsResponse.java`

**Checkpoint**: At this point, User Stories 1, 2, and 3 DTOs are fully functional and independently testable.

---

## Phase 6: User Story 4 - Ligas y Métricas Estadísticas Enriquecidas con Scryfall (Priority: P3)

**Goal**: Proveer DTOs para la consulta de clasificaciones acumuladas en ligas de tienda y agregación estadística de cartas Scryfall (reparto de colores, coste medio y frecuencia de uso).

**Independent Test**: Ejecutar pruebas unitarias de serialización para `LeagueDetailResponse`, `LeagueStandingEntryResponse`, `ColorBreakdownDto`, `CardUsageStatDto` y `MetagameAnalyticsResponse`.

### Tests for User Story 4

- [x] T044 [P] [US4] Unit test for League DTOs serialization and sorting in `src/test/java/com/tournament/app/application/dto/league/LeagueDtoSerializationTest.java`
- [x] T045 [P] [US4] Unit test for Metagame Analytics DTOs serialization in `src/test/java/com/tournament/app/application/dto/analytics/AnalyticsDtoSerializationTest.java`

### Implementation for User Story 4

- [x] T046 [P] [US4] Implement `LeagueStandingEntryResponse` record (`int rank`, `userId`, `username`, `int tournamentsPlayed`, `int accumulatedPoints`) in `src/main/java/com/tournament/app/application/dto/league/LeagueStandingEntryResponse.java`
- [x] T047 [US4] Implement `LeagueDetailResponse` record (`id`, `storeId`, `name`, `startDate`, `endDate`, `List<TournamentSummaryResponse> tournaments`, `List<LeagueStandingEntryResponse> standings`) in `src/main/java/com/tournament/app/application/dto/league/LeagueDetailResponse.java`
- [x] T048 [P] [US4] Implement `ColorBreakdownDto` record (`String colorCode`, `String colorName`, `BigDecimal percentage`) in `src/main/java/com/tournament/app/application/dto/analytics/ColorBreakdownDto.java`
- [x] T049 [P] [US4] Implement `CardUsageStatDto` record (`UUID scryfallId`, `String cardName`, `String manaCost`, `String typeLine`, `int totalCopies`, `BigDecimal deckInclusionRate`, `BigDecimal averagePriceEur`) in `src/main/java/com/tournament/app/application/dto/analytics/CardUsageStatDto.java`
- [x] T050 [US4] Implement `MetagameAnalyticsResponse` record (`String scope`, `UUID targetId`, `int totalDecksAnalyzed`, `BigDecimal averageDeckBudgetEur`, `List<ColorBreakdownDto> colorDistribution`, `List<CardUsageStatDto> topPlayedCards`) in `src/main/java/com/tournament/app/application/dto/analytics/MetagameAnalyticsResponse.java`

**Checkpoint**: All user stories (1-4) DTOs are independently functional and fully implemented.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Verificación de calidad, cumplimiento constitucional y ejecución de validaciones integrales

- [x] T051 Architectural purity check: Verify zero `org.springframework.*` or `jakarta.persistence.*` imports exist in `src/main/java/com/tournament/app/domain/model/`
- [x] T052 Immutability check: Verify that all DTOs in `src/main/java/com/tournament/app/application/dto/` are declared strictly as `record` of Java 21
- [x] T053 Run full Maven test suite setup and verify 100% of DTO validation tests pass without warnings
- [x] T054 [P] Update `specs/002-backend-dtos/quickstart.md` with final package imports and sample unit test snippets if needed

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - **BLOCKS** all user stories because all DTOs reference `UserRole`, `TournamentFormat`, `TournamentStatus`, etc.
- **User Stories (Phase 3+)**: Depend on Foundational (Phase 2).
  - Can proceed sequentially in priority order (P1 → P1 → P2 → P3) or in parallel.
- **Polish (Phase 7)**: Depends on all user story phases being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Depends on Phase 2 (`UserRole`, `FieldErrorDto`). Independent of other stories.
- **User Story 2 (P1)**: Depends on Phase 2 (`TournamentFormat`, `TournamentStatus`, `RegistrationStatus`) and US1 (`UserSummaryResponse`, `StoreSummaryResponse`).
- **User Story 3 (P2)**: Depends on Phase 2 (`RoundStatus`, `MatchStatus`) and US1 (`UserSummaryResponse`).
- **User Story 4 (P3)**: Depends on Phase 2 and US2 (`TournamentSummaryResponse`).

---

## Parallel Opportunities

- **Phase 1**: T002 and T003 can run in parallel after T001.
- **Phase 2**: Enums T004 through T009 and error DTO T010 can all run in parallel.
- **Phase 3 (US1)**: Test tasks T012 and T013 run in parallel; implementation tasks T014 through T020 run in parallel.
- **Phase 4 (US2)**: Test tasks T022, T023, T024 run in parallel; implementation tasks T025, T026, T027, T029, T030, T032, T034 run in parallel.
- **Phase 5 (US3)**: Test tasks T036 and T037 run in parallel; implementation tasks T038, T040, T041, T042 run in parallel.
- **Phase 6 (US4)**: Test tasks T044 and T045 run in parallel; implementation tasks T046, T048, T049 run in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (`pom.xml` and package tree).
2. Complete Phase 2: Foundational (Enums and RFC 7807 common error DTOs).
3. Complete Phase 3: User Story 1 (Auth and Store DTOs).
4. **STOP and VALIDATE**: Run `mvn test -Dtest=AuthDtoValidationTest,StoreDtoValidationTest`.
5. Deploy/deliver baseline authentication and store contracts.

### Incremental Delivery

1. Complete Setup + Foundational → Core enums and error records available.
2. Deliver US1 (Auth & Store) → Baseline contracts ready (MVP).
3. Deliver US2 (Moxfield Deck, Tournament & Registration) → Core tournament management ready.
4. Deliver US3 (Rounds, Pairings & Results) → Competitive execution contracts ready.
5. Deliver US4 (Leagues & Scryfall Analytics) → Metagame analytics and league tracking ready.
6. Phase 7 Polish → Verification of pure domain architecture and clean build.

# Contratos de Repositorios de Dominio y Adaptador Scryfall (`002-store-tournament-backend-scryfall`)

> [!IMPORTANT]
> **Coordinación con la rama `002-backend-dtos`**: Esta rama define e implementa los **Puertos de Dominio (`domain/repository`, `domain/service`)**, los **Modelos de Dominio (`domain/model`)**, los **Adaptadores de Persistencia JPA (`infrastructure/adapter/persistence/`)** y el **Adaptador de Scryfall (`infrastructure/adapter/scryfall/`)**. No crea archivos dentro de `application/dto` para permitir una fusión limpia con `002-backend-dtos`.

---

## 1. Puertos de Repositorio de Dominio (`com.tournament.app.domain.repository`)

### 1.1. `UserRepository` (Tabla `users` + agregaciones para `UserProfileResponse`)
- `Optional<User> findById(UUID id)`
- `Optional<User> findByUsername(String username)`
- `List<User> findAll()`
- `List<User> findByStoreId(UUID storeId)`
- `Optional<UserProfileSummary> findUserProfileSummaryById(UUID userId)`
  - Devuelve: `User` (sin `passwordHash` en vistas públicas), lista de tiendas a las que pertenece (`store_members`), `long registeredDeckCount` (`decks`) y lista de participaciones en ligas (`league_standings`).
- `User save(User user)`

### 1.2. `StoreRepository` (Tablas `stores` y `store_members` + agregaciones para `StoreDetailResponse`)
- `Optional<Store> findById(UUID id)`
- `Optional<Store> findByName(String name)`
- `List<Store> findAll()`
- `List<Store> findByOwnerId(UUID ownerId)`
- `List<StoreMember> findMembersByStoreId(UUID storeId)`
- `List<Store> findStoresByMemberUserId(UUID userId)`
- `Optional<StoreDetailSummary> findStoreDetailById(UUID storeId)`
  - Devuelve: `Store`, `ownerUsername`, `long totalTournaments`, `long totalMembers`, ligas activas y próximos torneos.
- `Store save(Store store)`
- `StoreMember addMember(UUID storeId, UUID userId)`

### 1.3. `DeckRepository` (Tabla `decks` para `DeckSummaryResponse`)
- `Optional<Deck> findById(UUID id)`
- `List<Deck> findByUserId(UUID userId)`
- `Optional<Deck> findByUserIdAndMoxfieldDeckId(UUID userId, String moxfieldDeckId)`
- `long countByUserId(UUID userId)`
- `Deck save(Deck deck)`

### 1.4. `LeagueRepository` (Tablas `leagues`, `league_standings` y Vista `v_league_leaderboard`)
- `Optional<League> findById(UUID id)`
- `List<League> findByStoreId(UUID storeId)`
- `List<League> findByStoreIdAndStatus(UUID storeId, LeagueStatus status)`
- `List<League> findByStoreIdAndFormat(UUID storeId, MagicFormat format)`
- `List<LeagueStanding> findStandingsByLeagueId(UUID leagueId)`
- `List<LeagueStanding> findStandingsByUserId(UUID userId)`
- `List<LeagueLeaderboardEntry> findLeaderboardByLeagueId(UUID leagueId)` (extraído desde `v_league_leaderboard` ordenado por `current_rank ASC`)
- `Optional<LeagueDetailSummary> findLeagueDetailById(UUID leagueId)`
  - Devuelve: `League`, lista de `Tournament` asociados a la liga y `List<LeagueLeaderboardEntry>` para alimentar `LeagueDetailResponse` y `LeagueStandingEntryResponse`.
- `League save(League league)`
- `LeagueStanding saveStanding(LeagueStanding standing)`

### 1.5. `TournamentTemplateRepository` (Tabla `tournament_templates`)
- `Optional<TournamentTemplate> findById(UUID id)`
- `List<TournamentTemplate> findByStoreId(UUID storeId)`
- `List<TournamentTemplate> findByStoreIdAndActiveTrue(UUID storeId)`
- `List<TournamentTemplate> findByLeagueId(UUID leagueId)`
- `List<TournamentTemplate> findAllActive()`
- `TournamentTemplate save(TournamentTemplate template)`

### 1.6. `TournamentRepository` (Tabla `tournaments` para `TournamentSummaryResponse` y `TournamentDetailResponse`)
- `Optional<Tournament> findById(UUID id)`
- `List<Tournament> findByStoreIdOrderByScheduledAtDesc(UUID storeId)`
- `List<Tournament> findByStoreIdAndStatus(UUID storeId, TournamentStatus status)`
- `List<Tournament> findByLeagueIdOrderByScheduledAtDesc(UUID leagueId)`
- `boolean existsByTemplateIdAndScheduledAt(UUID templateId, Instant scheduledAt)`
- `long countByStoreId(UUID storeId)`
- `Optional<TournamentDetailSummary> findTournamentDetailById(UUID tournamentId)`
  - Devuelve: `Tournament`, `long registeredPlayersCount`, lista de inscripciones/clasificación y lista de rondas con sus partidas.
- `Tournament save(Tournament tournament)`

### 1.7. `RegistrationRepository` (Tablas `tournament_registrations` y `registration_deck_cards` para `TournamentRegistrationResponse` y `TournamentStandingsResponse`)
- `Optional<TournamentRegistration> findById(UUID id)`
- `Optional<TournamentRegistration> findByTournamentIdAndUserId(UUID tournamentId, UUID userId)`
- `List<TournamentRegistration> findByTournamentIdOrderByRegisteredAtAsc(UUID tournamentId)`
- `List<TournamentRegistration> findStandingsByTournamentId(UUID tournamentId)` (ordenado por `match_points DESC, omw_percentage DESC`)
- `List<TournamentRegistration> findByUserId(UUID userId)`
- `List<RegistrationDeckCard> findDeckCardsByRegistrationId(UUID registrationId)`
- `List<RegistrationDeckCard> findDeckCardsByTournamentId(UUID tournamentId)`
- `long countByTournamentId(UUID tournamentId)`
- `TournamentRegistration save(TournamentRegistration registration, List<RegistrationDeckCard> deckCards)`

### 1.8. `RoundRepository` y `MatchRepository` (Tablas `rounds` y `matches` para `RoundDetailResponse` y `MatchPairingResponse`)
- `Optional<Round> findRoundById(UUID roundId)`
- `Optional<Round> findByTournamentIdAndRoundNumber(UUID tournamentId, int roundNumber)`
- `List<Round> findByTournamentIdOrderByRoundNumberAsc(UUID tournamentId)`
- `Optional<Match> findMatchById(UUID matchId)`
- `List<Match> findMatchesByRoundIdOrderByTableNumberAsc(UUID roundId)`
- `List<Match> findMatchesByTournamentId(UUID tournamentId)`
- `Round saveRound(Round round, List<Match> matches)`
- `Match saveMatch(Match match)`

### 1.9. `ScryfallCardRepository` (Tabla `scryfall_cards` y Vista `v_scryfall_card_stats` para `MetagameAnalyticsResponse`)
- `Optional<ScryfallCard> findById(UUID scryfallId)`
- `List<ScryfallCard> findByIdIn(Collection<UUID> scryfallIds)`
- `List<ScryfallCard> findByOracleId(UUID oracleId)`
- `Optional<ScryfallCard> findFirstByNameIgnoreCase(String name)`
- `List<ScryfallCard> findByNameInIgnoreCase(Collection<String> names)`
- `List<ScryfallCard> findByColorContaining(String colorCode)`
- `List<ScryfallCard> findLegalByFormat(MagicFormat format)`
- `List<ScryfallCard> saveAll(Collection<ScryfallCard> cards)`
- `List<ScryfallCardStats> findCardStatsByStoreIdAndFormat(UUID storeId, MagicFormat format)` (desde `v_scryfall_card_stats`)
- `List<ScryfallCardStats> findCardStatsByLeagueId(UUID leagueId)` (desde `v_scryfall_card_stats`)
- `MetagameAnalyticsSummary computeMetagameAnalytics(UUID storeId, UUID leagueId, UUID tournamentId, MagicFormat format)`
  - Devuelve: `totalDecksAnalyzed`, `averageDeckPrice`, `Map<String, BigDecimal> colorDistributionPercent`, `Map<String, BigDecimal> manaCurveAverage`, y `List<ScryfallCardStats>` con porcentaje de inclusión.

---

## 2. Contrato de Comunicación con Scryfall (`ScryfallCardGateway` / `ScryfallClient`)

### 2.1. Flujo al Registrar o Inscribir un Mazo (`DeckRegistrationScryfallSynchronizer`)
1. **Entrada**: Lista de identificadores de cartas (UUID de Scryfall o nombres exactos de cartas extraídos del mazo al registrarlo) + sección (`COMMANDER`, `MAINBOARD`, `SIDEBOARD`) + cantidad.
2. **Consulta a Caché Local (`scryfall_cards`)**:
   - Busca en `ScryfallCardRepository` todas las cartas solicitadas cuyo `last_synced_at >= now() - cacheTtlHours` (por defecto 24h).
3. **Petición por Lotes a Scryfall API (`POST https://api.scryfall.com/cards/collection`)**:
   - Para las cartas ausentes o caducadas, agrupa hasta **75 identificadores por lote**:
     ```json
     {
       "identifiers": [
         { "id": "c1f829b3-..." },
         { "name": "Sol Ring" }
       ]
     }
     ```
   - Cabeceras HTTP obligatorias:
     - `User-Agent: TournamentManagerBackend/1.0`
     - `Accept: application/json`
   - Respeto de *Rate Limit*: pausa mínima configurable (`100ms`) entre lotes consecutivos.
   - Normalización de respuesta Scryfall:
     - `id`, `oracle_id`, `name`, `mana_cost`, `cmc`, `type_line`, `colors`, `color_identity`, `rarity`, `legalities` (`standard`, `commander`, etc.).
     - Precios: `prices.eur` (o `prices.eur_foil` si `eur` es nulo) -> `price_eur`; `prices.usd` (o `prices.usd_foil`) -> `price_usd`.
     - Imagen: `image_uris.normal` (o `card_faces[0].image_uris.normal` en cartas de doble cara).
4. **Persistencia y Construcción del Snapshot**:
   - Hace `upsert` (`saveAll`) en `scryfall_cards` actualizando `last_synced_at = Instant.now()`.
   - Construye el objeto inmutable `DeckSnapshot` y las filas `RegistrationDeckCard` con el `unit_price_snapshot` según la moneda de la tienda (`EUR` o `USD`).

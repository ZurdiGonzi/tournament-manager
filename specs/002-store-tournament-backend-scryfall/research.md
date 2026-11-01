# Phase 0 Research: Acceso a Datos de Torneos, Ligas y Estadísticas de Tienda e Integración con Scryfall (`002-store-tournament-backend-scryfall`)

## 1. Mapeo de las 12 Tablas y 2 Vistas SQL en Hibernate 6 / Spring Data JPA

- **Decision**: Mapear las 12 tablas relacionales (`users`, `stores`, `store_members`, `decks`, `leagues`, `league_standings`, `tournament_templates`, `tournaments`, `scryfall_cards`, `tournament_registrations`, `registration_deck_cards`, `rounds`, `matches`) mediante entidades JPA en `infrastructure/adapter/persistence/jpa/` y mapear las 2 vistas SQL (`v_league_leaderboard` y `v_scryfall_card_stats`) como entidades JPA de solo lectura anotadas con `@Immutable` de Hibernate (`@Entity @Immutable @Table(name = "v_...")`) usando claves primarias compuestas (`@Embeddable` / `@EmbeddedId`).
- **Rationale**:
  - Permite extraer tanto cada entidad individual como las agregaciones precalculadas en PostgreSQL (`RANK() OVER (...)` en `v_league_leaderboard` y las métricas de uso/precio en `v_scryfall_card_stats`) mediante repositorios Spring Data JPA tipados y seguros sin duplicar SQL complejo en código Java.
  - Mantiene `spring.jpa.hibernate.ddl-auto=validate` compatible con Flyway (`V1__init_store_tournament_schema.sql`).
- **Alternatives considered**:
  - Consultas `JdbcTemplate` manuales para las vistas: descartado para mantener consistencia en los adaptadores de persistencia JPA, aunque se usan proyecciones JPQL/SQL nativas puntuales para agregaciones adicionales como `ColorBreakdown` y contadores de perfil/tienda.

## 2. Mapeo Nativo de Columnas `JSONB` y Arrays `TEXT[]` en PostgreSQL 15+

- **Decision**: Utilizar `@JdbcTypeCode(SqlTypes.JSON)` de Hibernate 6 para mapear `tournament_registrations.deck_snapshot` y `scryfall_cards.legalities`, y `List<String>` / `String[]` nativo de Hibernate 6 para las columnas `TEXT[]` (`colors`, `color_identity` en `scryfall_cards`).
- **Rationale**:
  - Hibernate 6 (incluido en Spring Boot 3.x) soporta de forma nativa tanto `JSONB` como arrays de PostgreSQL sin requerir librerías de terceros (`hypersistence-utils`).
  - Garantiza que el snapshot del mazo (`DeckSnapshot`) sea inmutable y contenga exactamente los campos requeridos por `002-backend-dtos` (`deckId`, `format`, `calculatedDeckPrice`, `snapshotDate` y lista de cartas con metadatos de Scryfall y precio unitario fijado).
- **Alternatives considered**:
  - Serialización manual a `String` con `ObjectMapper`: descartado porque pierde validación estructural y tipado directo en la capa de persistencia.

## 3. Comunicación con Scryfall API al Registrar un Mazo (Lotes, Caché Local y Rate Limiting)

- **Decision**: Implementar `ScryfallClient` en `infrastructure/adapter/scryfall/` utilizando `RestClient` de Spring Boot 3.x contra el endpoint por lotes `POST https://api.scryfall.com/cards/collection` (que admite hasta 75 identificadores/nombres por petición), complementado con búsqueda individual (`GET /cards/named` o `/cards/{id}`) cuando sea necesario, activado internamente al registrar o inscribir un mazo.
- **Rationale**:
  - Un mazo de *Commander* tiene hasta 100 cartas (o hasta 75 en *Standard* con banquillo). Consultar carta por carta requeriría hasta 100 llamadas HTTP (~10 segundos aplicando el límite de 10 req/s de Scryfall). Con `POST /cards/collection` (dividiendo en lotes de máximo 75 cartas), un mazo de 100 cartas requiere a lo sumo **2 peticiones HTTP** (< 1 segundo).
  - Antes de llamar a Scryfall, el servicio comprueba en `scryfall_cards` qué cartas ya existen localmente con `last_synced_at` dentro de la ventana de vigencia configurable (`scryfall.cache-ttl-hours: 24`), solicitando a Scryfall únicamente las cartas faltantes o caducadas.
  - Incluye cabeceras obligatorias exigidas por Scryfall (`User-Agent: TournamentManager/1.0` y `Accept: application/json`), respeto de pausa mínima entre lotes (`100ms`) y manejo de cartas multifaz (`card_faces[0].image_uris` cuando `image_uris` raíz es nulo).
- **Alternatives considered**:
  - Descarga completa de *Bulk Data* de Scryfall (>150 MB en memoria): descartado porque excede los recursos de memoria de entornos cloud gratuitos y añade complejidad innecesaria frente a la sincronización por lotes al registrar mazos.

## 4. Coordinación sin Conflictos con la Rama `002-backend-dtos`

- **Decision**: Implementar todos los modelos de dominio, objetos de valor y resúmenes de consulta en `com.tournament.app.domain.model` y los puertos en `com.tournament.app.domain.repository`, dejando el paquete `com.tournament.app.application.dto` completamente libre de archivos nuevos en esta rama.
- **Rationale**:
  - La rama `002-backend-dtos` desarrolla en paralelo todos los records de `application/dto` (`UserSummaryResponse`, `UserProfileResponse`, `StoreDetailResponse`, `LeagueDetailResponse`, `LeagueStandingEntryResponse`, `DeckSummaryResponse`, `DeckSnapshotDto`, `TournamentSummaryResponse`, `TournamentDetailResponse`, `TournamentRegistrationResponse`, `RoundDetailResponse`, `MatchPairingResponse`, `TournamentStandingsResponse`, `MetagameAnalyticsResponse`, etc.).
  - Al proveer en `domain/` modelos inmutables con los mismos datos y agregaciones (por ejemplo, contadores de tiendas/mazos/ligas del usuario, totales de torneos y miembros de tienda, leaderboard de liga, standings de torneo con puntos/OMW% y analíticas de metajuego con distribución de colores y top de cartas), la integración posterior entre ambas ramas será directa y sin conflictos de `git merge`.
- **Alternatives considered**:
  - Crear DTOs duplicados o temporales en `application/dto`: descartado porque provocaría conflictos directos de nombres y firmas al fusionar `002-backend-dtos`.

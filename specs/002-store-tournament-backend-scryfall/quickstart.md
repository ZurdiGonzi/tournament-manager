# Quickstart & Validation Guide: Acceso a Datos de Torneos, Ligas y Estadísticas e Integración con Scryfall (`002-store-tournament-backend-scryfall`)

## 1. Prerrequisitos

- **Java 21** instalado y configurado en `JAVA_HOME`.
- **Maven 3.9+** (o wrapper `./mvnw`).
- **PostgreSQL 15+** (instancia en Supabase / Neon o contenedor PostgreSQL para pruebas de integración).
- Referencias de diseño:
  - Modelo de datos y vistas: [data-model.md](./data-model.md)
  - Contrato SQL Flyway V1: [database-ddl-contract.sql](./contracts/database-ddl-contract.sql)
  - Contratos de Repositorios de Dominio y Scryfall: [domain-repository-and-scryfall-contracts.md](./contracts/domain-repository-and-scryfall-contracts.md)

---

## 2. Configuración del Entorno

Define las variables de entorno para la base de datos y el cliente de Scryfall antes de levantar o probar el backend:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://<host>:5432/<db>?sslmode=require&prepareThreshold=0"
$env:SPRING_DATASOURCE_USERNAME="<db_user>"
$env:SPRING_DATASOURCE_PASSWORD="<db_password>"
$env:SCRYFALL_BASE_URL="https://api.scryfall.com"
$env:SCRYFALL_CACHE_TTL_HOURS="24"
```

---

## 3. Escenarios de Validación Ejecutables

### Escenario 1: Verificación de Pureza Arquitectónica del Dominio y Reglas de Negocio
Valida que ningún archivo dentro de `com.tournament.app.domain` importe clases de Spring o JPA/Hibernate, y que `BudgetValidator` y `SwissPairingEngine` superen sus pruebas unitarias puras:

```powershell
./mvnw test -Dtest="*Domain*,*ValidatorTest,*PairingEngineTest"
```
- **Resultado esperado**: `BUILD SUCCESS` en menos de 5 segundos sin levantar el contexto de Spring.

### Escenario 2: Migración Flyway V1 y Extracción de las 12 Tablas y 2 Vistas SQL
Valida que la migración `V1__init_store_tournament_schema.sql` se aplique limpiamente sobre PostgreSQL y que todos los repositorios (`UserRepository`, `StoreRepository`, `DeckRepository`, `LeagueRepository`, `TournamentTemplateRepository`, `TournamentRepository`, `RegistrationRepository`, `RoundRepository`, `MatchRepository`, `ScryfallCardRepository`) extraigan cada elemento individual y las vistas `v_league_leaderboard` y `v_scryfall_card_stats`:

```powershell
./mvnw test -Dtest="*RepositoryIntegrationTest"
```
- **Resultado esperado**:
  - Las 12 tablas y las 2 vistas SQL (`v_league_leaderboard`, `v_scryfall_card_stats`) se crean y validan con `ddl-auto=validate`.
  - Se recuperan correctamente perfiles de usuario sin exponer `password_hash`, tiendas con totales de miembros y torneos, ligas con su leaderboard calculado, torneos con sus inscripciones (`deck_snapshot` en `JSONB` y `registration_deck_cards`), rondas con partidas (incluyendo *BYE*) y estadísticas agregadas de cartas de Scryfall.

### Escenario 3: Sincronización de Cartas con Scryfall al Registrar un Mazo
Valida que al registrar/inscribir un mazo el adaptador `ScryfallClient` agrupe las cartas en lotes hacia `/cards/collection`, almacene los metadatos y precios (`EUR`/`USD`) en `scryfall_cards`, reutilice la caché local en lecturas subsiguientes y construya el `DeckSnapshot`:

```powershell
./mvnw test -Dtest="*ScryfallClientTest,*DeckRegistrationScryfallSynchronizerTest"
```
- **Resultado esperado**:
  - Las cartas no presentes en `scryfall_cards` se obtienen de Scryfall por lotes (respetando cabeceras `User-Agent` y rate limit) y se guardan localmente con `last_synced_at`.
  - Una segunda consulta sobre las mismas cartas dentro de la ventana de vigencia resuelve el 100% de las cartas desde PostgreSQL con `0` peticiones HTTP externas.

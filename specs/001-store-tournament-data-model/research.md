# Phase 0 Research: Modelo de Datos de Tienda, Torneos, Mazos (Moxfield) y Estadísticas (Scryfall)

**Feature**: `001-store-tournament-data-model`  
**Date**: 2026-11-01

## 1. Modelado de Tienda, Propietario (Dueño) y Comunidad de Usuarios

- **Decision**: Modelar una tabla `users` (`id`, `username`, `password_hash`, `role`) donde el rol distingue entre `PLAYER` y `ADMIN` (Dueño), una tabla `stores` con clave foránea obligatoria `owner_id -> users(id)` y una tabla intermedia `store_members` (`store_id`, `user_id`, `joined_at`) con clave primaria compuesta `(store_id, user_id)`.
- **Rationale**:
  - Cumple directamente con el requisito funcional de que cada **Tienda** tenga un **Propietario (Dueño)**, una **lista de usuarios** y una **lista de torneos**.
  - Permite tanto validar en Spring Security el rol general (`ROLE_ADMIN` para `/api/v1/admin/**`) como comprobar en el dominio que el usuario autenticado es efectivamente el `owner_id` de la tienda cuyos torneos administra.
- **Alternatives considered**:
  - *Columna `store_id` única dentro de `users`*: Descartado porque impediría que un jugador participe en torneos de más de una tienda o que un dueño gestione su membresía con flexibilidad.

## 2. Representación del Mazo como Enlace a Moxfield + Snapshot Inmutable en Torneos

- **Decision**:
  1. En la tabla `decks` (lista de mazos del usuario), almacenar únicamente la referencia ligera requerida por el usuario: `id`, `user_id`, `name` (opcional/sincronizado), `moxfield_url` y `moxfield_public_id` (extraído de la URL `https://www.moxfield.com/decks/<publicId>`).
  2. En la tabla `tournament_registrations` (usuarios apuntados a un torneo), almacenar tanto `deck_id` como una columna `deck_snapshot JSONB NOT NULL` y `total_Price_eur NUMERIC(10,2) NOT NULL`.
- **Rationale**:
  - Satisface el requisito de que para el usuario *"Mazo será simplemente un enlace a Moxfield"* y, a la vez, cumple el **Principio IV de la Constitución** (snapshots inmutables en `JSONB` al inscribirse).
  - Si el jugador cambia cartas en Moxfield tras apuntarse al torneo, `tournament_registrations.deck_snapshot` conserva exactamente las cartas, cantidades y precios validados en el momento de la inscripción.
- **Alternatives considered**:
  - *Normalizar todas las cartas de cada mazo de usuario en tablas relacionales permanentes*: Añade escrituras innecesarias cada vez que el jugador edita su mazo fuera de torneos; es más limpio consultar Moxfield al inscribirse y congelar el `JSONB` en la inscripción.

## 3. Almacenamiento de Datos de Cartas de Scryfall para Validación y Estadísticas

- **Decision**: Crear una tabla catálogo `scryfall_cards` indexada por `oracle_id` / `scryfall_id` que almacene los atributos de **Scryfall** necesarios para reglas y analítica:
  - `id` (UUID de Scryfall), `oracle_id` (UUID), `name`, `mana_cost`, `cmc` (valor de maná), `type_line`, `colors` (`TEXT[]`), `color_identity` (`TEXT[]`), `rarity`, `legalities` (`JSONB` con claves `standard`, `commander`, etc.), `price_eur` (`NUMERIC(10,2)`), `price_usd` (`NUMERIC(10,2)`), `updated_at`.
  - Adicionalmente, registrar una tabla analítica de aparición de cartas por inscripción (`registration_deck_cards`: `registration_id`, `scryfall_card_id`, `board_section`, `quantity`, `unit_price_at_registration`) poblada automáticamente al confirmar la inscripción junto al `deck_snapshot JSONB`.
- **Rationale**:
  - Permite a `BudgetValidator` comprobar en milisegundos si todas las cartas son `"legal"` en el formato del torneo (`STANDARD` o `COMMANDER`) y si la suma `SUM(quantity * price)` respeta la `max_deck_budget` del torneo.
  - La combinación de `scryfall_cards` + `registration_deck_cards` (además del índice GIN en `deck_snapshot`) permite que `AnalyticsController` calcule estadísticas de tienda y torneo (top cartas más jugadas, curva de maná media, distribución de colores, tipos de carta y coste medio de mazos) mediante consultas SQL directas y rápidas sin llamar a APIs externas al visualizar paneles.
- **Alternatives considered**:
  - *Consultar Scryfall en vivo cada vez que se abren las estadísticas*: Rechazado por límites de rate-limit de Scryfall (máx. 10 req/s) y por incumplir el criterio de éxito `SC-005`.

## 4. Restricciones del Torneo (Formato Magic + Presupuesto) y Recurrencia (`@Scheduled`)

- **Decision**:
  - Incluir en la tabla `tournaments` las columnas explícitas de negocio: `store_id`, `scheduled_at` (día y hora), `entry_fee` (precio de inscripción), `prize_description` (premio), `format` (`STANDARD`, `COMMANDER`), `max_deck_budget` (restricción de precio máximo del mazo) y `status`.
  - Mantener la tabla `tournament_templates` vinculada a `stores` con `cron_expression` y una restricción `UNIQUE (template_id, scheduled_at)` en `tournaments` para que `TournamentAutoScheduler` (`@Scheduled`) cree torneos periódicos de forma idempotente (Principio V de la Constitución).
- **Rationale**:
  - Modela de forma tipada y relacional todas las restricciones pedidas en la especificación (`Standard`, `Commander`, tope de precio del mazo, día/hora, precio de entrada y premio) y soporta tanto creación manual por el Dueño como creación recurrente programada.
- **Alternatives considered**:
  - *Guardar todas las restricciones del torneo en un campo texto libre*: Rechazado porque impide validar automáticamente en base de datos y en `BudgetValidator` el formato y el límite numérico de presupuesto.

## 5. Configuración de PostgreSQL (Supabase / Neon) y Migraciones Flyway

- **Decision**: Gestionar el esquema mediante migraciones SQL versionadas con **Flyway** en `src/main/resources/db/migration/V1__init_store_tournament_schema.sql`, usando tipos nativos de PostgreSQL (`UUID`, `TIMESTAMPTZ`, `NUMERIC(10,2)`, `JSONB`, `TEXT[]`) y mapeando las columnas `JSONB` con `@JdbcTypeCode(SqlTypes.JSON)` de Hibernate 6.
- **Rationale**:
  - Compatible al 100% con los niveles gratuitos de **Supabase** y **Neon**, sin requerir extensiones propietarias fuera de `pgcrypto`/`gen_random_uuid()` (incluido de serie en PostgreSQL 13+).


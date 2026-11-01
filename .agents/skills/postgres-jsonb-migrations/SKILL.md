---
name: postgres-jsonb-migrations
description: >-
  Use this skill when designing database tables, writing Flyway SQL migrations,
  configuring Supabase or Neon PostgreSQL connections, or mapping JSONB deck
  snapshots with Hibernate 6 / Spring Data JPA.
---

# PostgreSQL (Supabase / Neon) & JSONB Deck Snapshots Skill

Guía para el diseño relacional y uso de `JSONB` en PostgreSQL dentro de **Tournament Manager**.

## 1. Configuración para Supabase / Neon

- Configura variables de entorno para la cadena JDBC SSL:
  `SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>?sslmode=require`
- Si se usa el connection pooler en modo transacción (PgBouncer en Supabase/Neon), añade `prepareThreshold=0` en los parámetros JDBC para evitar errores con *prepared statements* cacheados.
- Mantén `spring.jpa.hibernate.ddl-auto=validate` y delega toda creación/modificación de tablas a **Flyway** (`src/main/resources/db/migration/V1__*.sql`).

## 2. Esquema Relacional Principal

1. `users`: `id (UUID PK)`, `username`, `email`, `password_hash`, `role`, `created_at`.
2. `tournament_templates`: Plantillas para el `@Scheduled` (`id`, `name`, `format`, `cron_expression`, `max_players`, `active`, `last_generated_at`).
3. `tournaments`: `id (UUID PK)`, `template_id (FK nullable)`, `name`, `format`, `status`, `scheduled_start_at`, `current_round`, `max_rounds`.
4. `tournament_registrations`:
   - `id (UUID PK)`, `tournament_id (FK)`, `player_id (FK)`, `status` (`REGISTERED`, `CHECKED_IN`, `DROPPED`, `DISQUALIFIED`).
   - `deck_snapshot JSONB NOT NULL`: Snapshot inmutable del mazo en el instante de cerrar inscripción.
   - Restricción única: `UNIQUE (tournament_id, player_id)`.
5. `rounds`: `id (UUID PK)`, `tournament_id (FK)`, `round_number INT`, `status`, `started_at`, `ended_at`. `UNIQUE (tournament_id, round_number)`.
6. `matches`: `id (UUID PK)`, `round_id (FK)`, `table_number INT`, `player1_id (FK)`, `player2_id (FK nullable para BYE)`, `player1_wins INT`, `player2_wins INT`, `draws INT`, `status`, `winner_id (FK nullable)`.

## 3. Mapeo de `JSONB` en Hibernate 6 (Spring Boot 3.x)

No uses librerías externas obsoletas (`vladmihalcea` no es necesaria para JSONB básico en Hibernate 6). Usa el soporte nativo:

```java
public record DeckSnapshot(
    String deckName,
    String archetype,
    String format,
    List<CardEntry> mainDeck,
    List<CardEntry> sideboard
) {
    public record CardEntry(String cardId, String name, int quantity) {}
}
```

En la entidad JPA `TournamentRegistration`:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "deck_snapshot", columnDefinition = "jsonb", nullable = false)
private DeckSnapshot deckSnapshot;
```

## 4. Índices JSONB y Rendimiento
- Si necesitas consultar torneos por cartas o arquetipos jugados en los snapshots:
  ```sql
  CREATE INDEX idx_reg_deck_archetype ON tournament_registrations ((deck_snapshot->>'archetype'));
  CREATE INDEX idx_reg_deck_cards_gin ON tournament_registrations USING GIN ((deck_snapshot->'mainDeck'));
  ```


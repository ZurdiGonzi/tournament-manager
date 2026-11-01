# Data Model: Esquema Relacional Consolidado de PostgreSQL (Flyway V1)

**Feature**: `003-align-database-schema` | **Fecha**: 2026-11-01

Este documento define el modelo relacional final, restricciones de integridad, vistas SQL e índices para la base de datos PostgreSQL en Supabase / Neon.

---

## 1. Tablas y Atributos Normalizados

### 1.1 `users`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `username` (`VARCHAR(60)` NOT NULL UNIQUE)
- `email` (`VARCHAR(255)` NOT NULL UNIQUE)
- `password_hash` (`VARCHAR(255)` NOT NULL)
- `role` (`VARCHAR(20)` NOT NULL CHECK (`role IN ('PLAYER', 'ADMIN')`))
- `created_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)

### 1.2 `stores`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `name` (`VARCHAR(120)` NOT NULL UNIQUE)
- `owner_id` (`UUID` NOT NULL REFERENCES `users(id)` ON DELETE RESTRICT)
- `currency` (`VARCHAR(3)` NOT NULL DEFAULT `'EUR'` CHECK (`currency IN ('EUR', 'USD')`))
- `created_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)

### 1.3 `store_members`
- `store_id` (`UUID` NOT NULL REFERENCES `stores(id)` ON DELETE CASCADE)
- `user_id` (`UUID` NOT NULL REFERENCES `users(id)` ON DELETE CASCADE)
- `joined_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)
- PRIMARY KEY (`store_id`, `user_id`)

### 1.4 `decks`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `user_id` (`UUID` NOT NULL REFERENCES `users(id)` ON DELETE CASCADE)
- `name` (`VARCHAR(150)` NOT NULL)
- `moxfield_url` (`VARCHAR(500)` NOT NULL)
- `moxfield_deck_id` (`VARCHAR(64)` NOT NULL)
- `created_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)
- `updated_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)
- UNIQUE (`user_id`, `moxfield_deck_id`)

### 1.5 `leagues`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `store_id` (`UUID` NOT NULL REFERENCES `stores(id)` ON DELETE CASCADE)
- `name` (`VARCHAR(150)` NOT NULL)
- `format` (`VARCHAR(30)` NOT NULL CHECK (`format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER')`))
- `prize_description` (`TEXT`)
- `start_date` (`TIMESTAMPTZ` NOT NULL)
- `end_date` (`TIMESTAMPTZ`)
- `status` (`VARCHAR(30)` NOT NULL DEFAULT `'IN_PROGRESS'` CHECK (`status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')`))
- `created_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)

### 1.6 `league_standings`
- `league_id` (`UUID` NOT NULL REFERENCES `leagues(id)` ON DELETE CASCADE)
- `user_id` (`UUID` NOT NULL REFERENCES `users(id)` ON DELETE CASCADE)
- `total_points` (`INT` NOT NULL DEFAULT 0 CHECK (`total_points >= 0`))
- `tournaments_played` (`INT` NOT NULL DEFAULT 0 CHECK (`tournaments_played >= 0`))
- `matches_won` (`INT` NOT NULL DEFAULT 0 CHECK (`matches_won >= 0`))
- `matches_drawn` (`INT` NOT NULL DEFAULT 0 CHECK (`matches_drawn >= 0`))
- `matches_lost` (`INT` NOT NULL DEFAULT 0 CHECK (`matches_lost >= 0`))
- `updated_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)
- PRIMARY KEY (`league_id`, `user_id`)

### 1.7 `tournament_templates`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `store_id` (`UUID` NOT NULL REFERENCES `stores(id)` ON DELETE CASCADE)
- `league_id` (`UUID` REFERENCES `leagues(id)` ON DELETE SET NULL)
- `name` (`VARCHAR(150)` NOT NULL)
- `cron_expression` (`VARCHAR(60)` NOT NULL)
- `entry_fee` (`NUMERIC(10, 2)` NOT NULL DEFAULT 0.00 CHECK (`entry_fee >= 0`))
- `prize_description` (`TEXT` NOT NULL)
- `format` (`VARCHAR(30)` NOT NULL CHECK (`format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER')`))
- `max_deck_budget` (`NUMERIC(10, 2)` CHECK (`max_deck_budget IS NULL OR max_deck_budget > 0`))
- `max_players` (`INT` NOT NULL DEFAULT 32 CHECK (`max_players >= 4`))
- `max_rounds` (`INT` NOT NULL DEFAULT 3 CHECK (`max_rounds > 0`))
- `active` (`BOOLEAN` NOT NULL DEFAULT true)
- `last_generated_at` (`TIMESTAMPTZ`)
- `created_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)

### 1.8 `tournaments`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `store_id` (`UUID` NOT NULL REFERENCES `stores(id)` ON DELETE CASCADE)
- `league_id` (`UUID` REFERENCES `leagues(id)` ON DELETE SET NULL)
- `template_id` (`UUID` REFERENCES `tournament_templates(id)` ON DELETE SET NULL)
- `name` (`VARCHAR(150)` NOT NULL)
- `scheduled_at` (`TIMESTAMPTZ` NOT NULL)
- `entry_fee` (`NUMERIC(10, 2)` NOT NULL DEFAULT 0.00 CHECK (`entry_fee >= 0`))
- `prize_description` (`TEXT` NOT NULL)
- `format` (`VARCHAR(30)` NOT NULL CHECK (`format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER')`))
- `max_deck_budget` (`NUMERIC(10, 2)` CHECK (`max_deck_budget IS NULL OR max_deck_budget > 0`))
- `max_players` (`INT` NOT NULL DEFAULT 32 CHECK (`max_players >= 4`))
- `status` (`VARCHAR(30)` NOT NULL DEFAULT `'REGISTRATION_OPEN'` CHECK (`status IN ('DRAFT', 'REGISTRATION_OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')`))
- `current_round` (`INT` NOT NULL DEFAULT 0)
- `max_rounds` (`INT` NOT NULL DEFAULT 3)
- `created_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)
- CONSTRAINT `uq_tournament_template_schedule` UNIQUE (`template_id`, `scheduled_at`)

### 1.9 `scryfall_cards`
- `id` (`UUID` PRIMARY KEY)
- `oracle_id` (`UUID` NOT NULL)
- `name` (`VARCHAR(200)` NOT NULL)
- `mana_cost` (`VARCHAR(60)`)
- `cmc` (`NUMERIC(5, 2)` NOT NULL DEFAULT 0)
- `type_line` (`VARCHAR(200)` NOT NULL)
- `colors` (`TEXT[]` NOT NULL DEFAULT `'{}'`)
- `color_identity` (`TEXT[]` NOT NULL DEFAULT `'{}'`)
- `rarity` (`VARCHAR(20)` NOT NULL)
- `legalities` (`JSONB` NOT NULL)
- `price_eur` (`NUMERIC(10, 2)`)
- `price_usd` (`NUMERIC(10, 2)`)
- `image_uri` (`VARCHAR(500)`)
- `last_synced_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)

### 1.10 `tournament_registrations`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `tournament_id` (`UUID` NOT NULL REFERENCES `tournaments(id)` ON DELETE CASCADE)
- `user_id` (`UUID` NOT NULL REFERENCES `users(id)` ON DELETE CASCADE)
- `deck_id` (`UUID` NOT NULL REFERENCES `decks(id)` ON DELETE RESTRICT)
- `status` (`VARCHAR(25)` NOT NULL DEFAULT `'CONFIRMED'` CHECK (`status IN ('CONFIRMED', 'CANCELLED', 'DROPPED')`))
- `calculated_deck_price` (`NUMERIC(10, 2)` NOT NULL CHECK (`calculated_deck_price >= 0`))
- `match_points` (`INT` NOT NULL DEFAULT 0 CHECK (`match_points >= 0`))
- `omw_percentage` (`NUMERIC(5, 4)` NOT NULL DEFAULT 0.0000)
- `final_rank` (`INT` CHECK (`final_rank IS NULL OR final_rank > 0`))
- `deck_snapshot` (`JSONB` NOT NULL)
- `registered_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)
- UNIQUE (`tournament_id`, `user_id`)

### 1.11 `registration_deck_cards`
- `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
- `registration_id` (`UUID` NOT NULL REFERENCES `tournament_registrations(id)` ON DELETE CASCADE)
- `scryfall_card_id` (`UUID` NOT NULL REFERENCES `scryfall_cards(id)` ON DELETE RESTRICT)
- `board_section` (`VARCHAR(20)` NOT NULL CHECK (`board_section IN ('COMMANDER', 'MAINBOARD', 'SIDEBOARD')`))
- `quantity` (`INT` NOT NULL CHECK (`quantity > 0`))
- `unit_price_snapshot` (`NUMERIC(10, 2)` NOT NULL CHECK (`unit_price_snapshot >= 0`))

### 1.12 `rounds` y `matches`
- **`rounds`**:
  - `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
  - `tournament_id` (`UUID` NOT NULL REFERENCES `tournaments(id)` ON DELETE CASCADE)
  - `round_number` (`INT` NOT NULL CHECK (`round_number > 0`))
  - `status` (`VARCHAR(20)` NOT NULL CHECK (`status IN ('ACTIVE', 'FINISHED')`))
  - `started_at` (`TIMESTAMPTZ` NOT NULL DEFAULT `now()`)
  - `ended_at` (`TIMESTAMPTZ`)
  - UNIQUE (`tournament_id`, `round_number`)
- **`matches`**:
  - `id` (`UUID` PRIMARY KEY DEFAULT `gen_random_uuid()`)
  - `round_id` (`UUID` NOT NULL REFERENCES `rounds(id)` ON DELETE CASCADE)
  - `table_number` (`INT` NOT NULL)
  - `player1_id` (`UUID` NOT NULL REFERENCES `users(id)`)
  - `player2_id` (`UUID` REFERENCES `users(id)`) -- NULL representa descanso/BYE
  - `player1_wins` (`INT` NOT NULL DEFAULT 0)
  - `player2_wins` (`INT` NOT NULL DEFAULT 0)
  - `draws` (`INT` NOT NULL DEFAULT 0)
  - `status` (`VARCHAR(30)` NOT NULL DEFAULT `'PENDING'` CHECK (`status IN ('PENDING', 'SUBMITTED', 'CONFIRMED', 'DISPUTED')`))
  - `winner_id` (`UUID` REFERENCES `users(id)`)

---

## 2. Vistas Analíticas e Índices

- **`v_league_leaderboard`**: Cálculo en tiempo real de puntuaciones de liga sumando los torneos asociados.
- **`v_scryfall_card_stats`**: Estadísticas de metajuego agrupando frecuencia, colores, CMC y precio promedio por tienda y formato.
- **Índices relacionales y GIN**:
  - Índices B-Tree en todas las claves foráneas y campos temporales.
  - Índices GIN en `tournament_registrations.deck_snapshot`, `scryfall_cards.legalities` y `scryfall_cards.colors`.


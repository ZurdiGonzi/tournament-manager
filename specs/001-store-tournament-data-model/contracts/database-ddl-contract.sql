-- =============================================================================
-- Contract: Flyway Migration V1__init_store_tournament_schema.sql
-- Feature: 001-store-tournament-data-model (Synchronized with 003-align-database-schema)
-- Target: PostgreSQL 15+ (Supabase / Neon)
-- =============================================================================

-- 1. USERS (Usuario con username y email únicos, contraseña cifrada y rol)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(60) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PLAYER', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2. STORES (Tienda: nombre, propietario Dueño)
CREATE TABLE stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL UNIQUE,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR' CHECK (currency IN ('EUR', 'USD')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 3. STORE_MEMBERS (Lista de usuarios de la Tienda)
CREATE TABLE store_members (
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (store_id, user_id)
);

-- 4. DECKS (Lista de mazos del Usuario: enlace a Moxfield)
CREATE TABLE decks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    moxfield_url VARCHAR(500) NOT NULL,
    moxfield_deck_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, moxfield_deck_id)
);

-- 5. LEAGUES (Ligas continuadas de tienda con formatos oficiales ampliados)
CREATE TABLE leagues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    format VARCHAR(30) NOT NULL CHECK (format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER')),
    prize_description TEXT,
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS'
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 6. LEAGUE_STANDINGS (Lista de jugadores apuntados en una Liga con sus puntos acumulados por los torneos)
CREATE TABLE league_standings (
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_points INT NOT NULL DEFAULT 0 CHECK (total_points >= 0),
    tournaments_played INT NOT NULL DEFAULT 0 CHECK (tournaments_played >= 0),
    matches_won INT NOT NULL DEFAULT 0 CHECK (matches_won >= 0),
    matches_drawn INT NOT NULL DEFAULT 0 CHECK (matches_drawn >= 0),
    matches_lost INT NOT NULL DEFAULT 0 CHECK (matches_lost >= 0),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (league_id, user_id)
);

-- 7. TOURNAMENT_TEMPLATES (Plantillas para creación periódica con @Scheduled y presupuesto opcional)
CREATE TABLE tournament_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    league_id UUID REFERENCES leagues(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    cron_expression VARCHAR(60) NOT NULL,
    entry_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (entry_fee >= 0),
    prize_description TEXT NOT NULL,
    format VARCHAR(30) NOT NULL CHECK (format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER')),
    max_deck_budget NUMERIC(10, 2) CHECK (max_deck_budget IS NULL OR max_deck_budget > 0),
    max_players INT NOT NULL DEFAULT 32 CHECK (max_players >= 4),
    max_rounds INT NOT NULL DEFAULT 3 CHECK (max_rounds > 0),
    active BOOLEAN NOT NULL DEFAULT true,
    last_generated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 8. TOURNAMENTS (Torneo con formatos ampliados, presupuesto flexible e idempotencia)
CREATE TABLE tournaments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    league_id UUID REFERENCES leagues(id) ON DELETE SET NULL,
    template_id UUID REFERENCES tournament_templates(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    entry_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (entry_fee >= 0),
    prize_description TEXT NOT NULL,
    format VARCHAR(30) NOT NULL CHECK (format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER')),
    max_deck_budget NUMERIC(10, 2) CHECK (max_deck_budget IS NULL OR max_deck_budget > 0),
    max_players INT NOT NULL DEFAULT 32 CHECK (max_players >= 4),
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTRATION_OPEN'
        CHECK (status IN ('DRAFT', 'REGISTRATION_OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    current_round INT NOT NULL DEFAULT 0,
    max_rounds INT NOT NULL DEFAULT 3,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tournament_template_schedule UNIQUE (template_id, scheduled_at)
);

-- 9. SCRYFALL_CARDS (Información de Scryfall sobre las cartas para validación y estadísticas)
CREATE TABLE scryfall_cards (
    id UUID PRIMARY KEY,
    oracle_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    mana_cost VARCHAR(60),
    cmc NUMERIC(5, 2) NOT NULL DEFAULT 0,
    type_line VARCHAR(200) NOT NULL,
    colors TEXT[] NOT NULL DEFAULT '{}',
    color_identity TEXT[] NOT NULL DEFAULT '{}',
    rarity VARCHAR(20) NOT NULL,
    legalities JSONB NOT NULL,
    price_eur NUMERIC(10, 2),
    price_usd NUMERIC(10, 2),
    image_uri VARCHAR(500),
    last_synced_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 10. TOURNAMENT_REGISTRATIONS (Inscripciones con estados normalizados y snapshot JSONB)
CREATE TABLE tournament_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deck_id UUID NOT NULL REFERENCES decks(id) ON DELETE RESTRICT,
    status VARCHAR(25) NOT NULL DEFAULT 'CONFIRMED'
        CHECK (status IN ('CONFIRMED', 'CANCELLED', 'DROPPED')),
    calculated_deck_price NUMERIC(10, 2) NOT NULL CHECK (calculated_deck_price >= 0),
    match_points INT NOT NULL DEFAULT 0 CHECK (match_points >= 0),
    omw_percentage NUMERIC(5, 4) NOT NULL DEFAULT 0.0000,
    final_rank INT CHECK (final_rank IS NULL OR final_rank > 0),
    deck_snapshot JSONB NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tournament_id, user_id)
);

-- 11. REGISTRATION_DECK_CARDS (Tabla analítica para cruzar inscripciones con Scryfall)
CREATE TABLE registration_deck_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    registration_id UUID NOT NULL REFERENCES tournament_registrations(id) ON DELETE CASCADE,
    scryfall_card_id UUID NOT NULL REFERENCES scryfall_cards(id) ON DELETE RESTRICT,
    board_section VARCHAR(20) NOT NULL CHECK (board_section IN ('COMMANDER', 'MAINBOARD', 'SIDEBOARD')),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_snapshot NUMERIC(10, 2) NOT NULL CHECK (unit_price_snapshot >= 0)
);

-- 12. ROUNDS & MATCHES (Estructura competitiva con estados normalizados 1:1 con Java 21)
CREATE TABLE rounds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    round_number INT NOT NULL CHECK (round_number > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'FINISHED')),
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ended_at TIMESTAMPTZ,
    UNIQUE (tournament_id, round_number)
);

CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    round_id UUID NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    table_number INT NOT NULL,
    player1_id UUID NOT NULL REFERENCES users(id),
    player2_id UUID REFERENCES users(id), -- NULL indica BYE/descanso
    player1_wins INT NOT NULL DEFAULT 0,
    player2_wins INT NOT NULL DEFAULT 0,
    draws INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'SUBMITTED', 'CONFIRMED', 'DISPUTED')),
    winner_id UUID REFERENCES users(id)
);

-- =============================================================================
-- VISTAS SQL (ESTADÍSTICAS DE SCRYFALL Y CLASIFICACIÓN DE LIGAS POR TORNEOS)
-- =============================================================================

-- Vista 1: Clasificación de Liga calculada desde los puntos de sus Torneos asociados
CREATE OR REPLACE VIEW v_league_leaderboard AS
SELECT
    l.id AS league_id,
    l.store_id,
    l.name AS league_name,
    u.id AS user_id,
    u.username,
    COALESCE(ls.total_points, SUM(tr.match_points), 0) AS total_points,
    COUNT(DISTINCT tr.tournament_id) AS tournaments_played,
    RANK() OVER (
        PARTITION BY l.id
        ORDER BY COALESCE(ls.total_points, SUM(tr.match_points), 0) DESC,
                 COUNT(DISTINCT tr.tournament_id) DESC
    ) AS current_rank
FROM leagues l
JOIN league_standings ls ON ls.league_id = l.id
JOIN users u ON u.id = ls.user_id
LEFT JOIN tournaments t ON t.league_id = l.id
LEFT JOIN tournament_registrations tr ON tr.tournament_id = t.id AND tr.user_id = u.id
GROUP BY l.id, l.store_id, l.name, u.id, u.username, ls.total_points;

-- Vista 2: Estadísticas de Cartas de Scryfall por Tienda y Formato
CREATE OR REPLACE VIEW v_scryfall_card_stats AS
SELECT
    t.store_id,
    t.league_id,
    t.format,
    sc.oracle_id,
    sc.name AS card_name,
    sc.type_line,
    sc.mana_cost,
    sc.cmc,
    sc.colors,
    sc.color_identity,
    sc.rarity,
    COUNT(DISTINCT tr.id) AS decks_including_card,
    SUM(rdc.quantity) AS total_copies_played,
    ROUND(AVG(rdc.unit_price_snapshot), 2) AS avg_unit_price
FROM registration_deck_cards rdc
JOIN tournament_registrations tr ON tr.id = rdc.registration_id
JOIN tournaments t ON t.id = tr.tournament_id
JOIN scryfall_cards sc ON sc.id = rdc.scryfall_card_id
GROUP BY
    t.store_id, t.league_id, t.format,
    sc.oracle_id, sc.name, sc.type_line, sc.mana_cost, sc.cmc, sc.colors, sc.color_identity, sc.rarity;

-- =============================================================================
-- ÍNDICES RELACIONALES Y GIN (JSONB / ARRAYS) PARA ESTADÍSTICAS Y RENDIMIENTO
-- =============================================================================
CREATE INDEX idx_leagues_store ON leagues(store_id);
CREATE INDEX idx_league_standings_points ON league_standings(league_id, total_points DESC);
CREATE INDEX idx_tournaments_store_date ON tournaments(store_id, scheduled_at DESC);
CREATE INDEX idx_tournaments_league ON tournaments(league_id);
CREATE INDEX idx_decks_user_id ON decks(user_id);
CREATE INDEX idx_registrations_tournament ON tournament_registrations(tournament_id);
CREATE INDEX idx_registrations_points ON tournament_registrations(tournament_id, match_points DESC);
CREATE INDEX idx_registrations_deck_snapshot_gin ON tournament_registrations USING GIN (deck_snapshot);
CREATE INDEX idx_scryfall_oracle_id ON scryfall_cards(oracle_id);
CREATE INDEX idx_scryfall_legalities_gin ON scryfall_cards USING GIN (legalities);
CREATE INDEX idx_scryfall_colors_gin ON scryfall_cards USING GIN (colors);
CREATE INDEX idx_reg_deck_cards_card ON registration_deck_cards(scryfall_card_id);
CREATE INDEX idx_reg_deck_cards_registration ON registration_deck_cards(registration_id);

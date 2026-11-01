-- =============================================================================
-- Flyway Migration V1__init_store_tournament_schema.sql
-- =============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('PLAYER', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL UNIQUE,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR' CHECK (currency IN ('EUR', 'USD')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE store_members (
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (store_id, user_id)
);

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

CREATE TABLE scoring_formulas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    formula_type VARCHAR(35) NOT NULL
        CHECK (formula_type IN ('MATCH_POINTS', 'LEAGUE_MATCHDAY_POINTS', 'PAIRING_WEIGHT')),
    expression TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tournament_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    cron_expression VARCHAR(60) NOT NULL,
    entry_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (entry_fee >= 0),
    prize_description TEXT NOT NULL,
    format VARCHAR(30) NOT NULL CHECK (format IN ('STANDARD', 'COMMANDER')),
    max_deck_budget NUMERIC(10, 2) NOT NULL CHECK (max_deck_budget > 0),
    max_rounds INT NOT NULL DEFAULT 3 CHECK (max_rounds > 0),
    active BOOLEAN NOT NULL DEFAULT true,
    last_generated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tournaments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    template_id UUID REFERENCES tournament_templates(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    entry_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (entry_fee >= 0),
    prize_description TEXT NOT NULL,
    format VARCHAR(30) NOT NULL CHECK (format IN ('STANDARD', 'COMMANDER')),
    max_deck_budget NUMERIC(10, 2) NOT NULL CHECK (max_deck_budget > 0),
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTRATION_OPEN'
        CHECK (status IN ('DRAFT', 'REGISTRATION_OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    current_round INT NOT NULL DEFAULT 0,
    max_rounds INT NOT NULL DEFAULT 3 CHECK (max_rounds > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tournament_template_schedule UNIQUE (template_id, scheduled_at)
);

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

CREATE TABLE tournament_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deck_id UUID NOT NULL REFERENCES decks(id) ON DELETE RESTRICT,
    status VARCHAR(25) NOT NULL DEFAULT 'REGISTERED'
        CHECK (status IN ('REGISTERED', 'CHECKED_IN', 'DROPPED', 'DISQUALIFIED')),
    calculated_deck_price NUMERIC(10, 2) NOT NULL CHECK (calculated_deck_price >= 0),
    deck_snapshot JSONB NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tournament_id, user_id)
);

CREATE TABLE registration_deck_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    registration_id UUID NOT NULL REFERENCES tournament_registrations(id) ON DELETE CASCADE,
    scryfall_card_id UUID NOT NULL REFERENCES scryfall_cards(id) ON DELETE RESTRICT,
    board_section VARCHAR(20) NOT NULL CHECK (board_section IN ('COMMANDER', 'MAINBOARD', 'SIDEBOARD')),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price_snapshot NUMERIC(10, 2) NOT NULL CHECK (unit_price_snapshot >= 0)
);

CREATE TABLE rounds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    round_number INT NOT NULL CHECK (round_number > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ended_at TIMESTAMPTZ,
    UNIQUE (tournament_id, round_number)
);

CREATE TABLE matches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    round_id UUID NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    table_number INT NOT NULL,
    player1_id UUID NOT NULL REFERENCES users(id),
    player2_id UUID REFERENCES users(id),
    player1_wins INT NOT NULL DEFAULT 0,
    player2_wins INT NOT NULL DEFAULT 0,
    draws INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'AWAITING_CONFIRMATION', 'DISPUTED', 'CONFIRMED')),
    winner_id UUID REFERENCES users(id)
);

CREATE INDEX idx_tournaments_store_date ON tournaments(store_id, scheduled_at DESC);
CREATE INDEX idx_decks_user_id ON decks(user_id);
CREATE INDEX idx_registrations_tournament ON tournament_registrations(tournament_id);
CREATE INDEX idx_registrations_deck_snapshot_gin ON tournament_registrations USING GIN (deck_snapshot);
CREATE INDEX idx_scryfall_oracle_id ON scryfall_cards(oracle_id);
CREATE INDEX idx_scryfall_legalities_gin ON scryfall_cards USING GIN (legalities);
CREATE INDEX idx_scryfall_colors_gin ON scryfall_cards USING GIN (colors);
CREATE INDEX idx_reg_deck_cards_card ON registration_deck_cards(scryfall_card_id);
CREATE INDEX idx_reg_deck_cards_registration ON registration_deck_cards(registration_id);

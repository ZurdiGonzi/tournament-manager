-- =============================================================================
-- Flyway Migration V2__league_formulas_and_business_logic_schema.sql
-- =============================================================================

CREATE TABLE IF NOT EXISTS scoring_formulas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    formula_type VARCHAR(35) NOT NULL
        CHECK (formula_type IN ('MATCH_POINTS', 'LEAGUE_MATCHDAY_POINTS', 'PAIRING_WEIGHT')),
    expression TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE leagues (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    format VARCHAR(30) NOT NULL CHECK (format IN ('STANDARD', 'COMMANDER')),
    max_deck_budget NUMERIC(10, 2) NOT NULL CHECK (max_deck_budget > 0),
    match_points_formula_id UUID NOT NULL REFERENCES scoring_formulas(id) ON DELETE RESTRICT,
    matchday_points_formula_id UUID NOT NULL REFERENCES scoring_formulas(id) ON DELETE RESTRICT,
    pairing_formula_id UUID NOT NULL REFERENCES scoring_formulas(id) ON DELETE RESTRICT,
    status VARCHAR(25) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE league_standings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_league_points NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    matchdays_played INT NOT NULL DEFAULT 0,
    total_match_wins INT NOT NULL DEFAULT 0,
    total_matches_played INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (league_id, user_id)
);

ALTER TABLE decks
    ADD COLUMN IF NOT EXISTS format VARCHAR(30) DEFAULT 'COMMANDER' CHECK (format IN ('STANDARD', 'COMMANDER')),
    ADD COLUMN IF NOT EXISTS commander_oracle_id UUID,
    ADD COLUMN IF NOT EXISTS commander_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS color_identity TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS last_calculated_price NUMERIC(10, 2);

ALTER TABLE tournaments
    ADD COLUMN IF NOT EXISTS league_id UUID REFERENCES leagues(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS matchday_number INT CHECK (matchday_number IS NULL OR matchday_number > 0),
    ADD COLUMN IF NOT EXISTS match_points_formula_id UUID REFERENCES scoring_formulas(id) ON DELETE RESTRICT,
    ADD COLUMN IF NOT EXISTS pairing_formula_id UUID REFERENCES scoring_formulas(id) ON DELETE RESTRICT,
    ADD CONSTRAINT uq_league_matchday_number UNIQUE (league_id, matchday_number);

ALTER TABLE tournament_registrations
    ADD COLUMN IF NOT EXISTS commander_oracle_id UUID,
    ADD COLUMN IF NOT EXISTS commander_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS color_identity TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS tournament_points NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS calculated_league_matchday_points NUMERIC(10, 2) NOT NULL DEFAULT 0.00;

CREATE TABLE match_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    registration_id UUID NOT NULL REFERENCES tournament_registrations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deck_id UUID NOT NULL REFERENCES decks(id) ON DELETE RESTRICT,
    commander_oracle_id UUID,
    commander_name VARCHAR(200) NOT NULL,
    placement INT NOT NULL DEFAULT 1 CHECK (placement >= 1),
    wins INT NOT NULL DEFAULT 0 CHECK (wins >= 0),
    losses INT NOT NULL DEFAULT 0 CHECK (losses >= 0),
    draws INT NOT NULL DEFAULT 0 CHECK (draws >= 0),
    kills INT NOT NULL DEFAULT 0 CHECK (kills >= 0),
    match_points_earned NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    UNIQUE (match_id, user_id)
);

CREATE INDEX idx_scoring_formulas_store ON scoring_formulas(store_id, formula_type);
CREATE INDEX idx_tournaments_league ON tournaments(league_id, matchday_number);
CREATE INDEX idx_league_standings_points ON league_standings(league_id, total_league_points DESC);
CREATE INDEX idx_match_participants_match ON match_participants(match_id);
CREATE INDEX idx_match_participants_commander ON match_participants(commander_name, placement);
CREATE INDEX idx_match_participants_user_deck ON match_participants(user_id, deck_id);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stores (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    owner_id UUID NOT NULL REFERENCES users(id),
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS store_members (
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (store_id, user_id)
);

CREATE TABLE IF NOT EXISTS decks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    moxfield_url VARCHAR(500) NOT NULL,
    moxfield_deck_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, moxfield_deck_id)
);

CREATE TABLE IF NOT EXISTS leagues (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    format VARCHAR(30) NOT NULL,
    prize_description TEXT,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS league_standings (
    league_id UUID NOT NULL REFERENCES leagues(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_points INT NOT NULL DEFAULT 0,
    tournaments_played INT NOT NULL DEFAULT 0,
    matches_won INT NOT NULL DEFAULT 0,
    matches_drawn INT NOT NULL DEFAULT 0,
    matches_lost INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (league_id, user_id)
);

CREATE TABLE IF NOT EXISTS tournament_templates (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    league_id UUID REFERENCES leagues(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    cron_expression VARCHAR(60) NOT NULL,
    entry_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    prize_description TEXT NOT NULL,
    format VARCHAR(30) NOT NULL,
    max_deck_budget NUMERIC(10, 2) NOT NULL,
    max_players INT NOT NULL DEFAULT 32,
    max_rounds INT NOT NULL DEFAULT 3,
    active BOOLEAN NOT NULL DEFAULT true,
    last_generated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tournaments (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    league_id UUID REFERENCES leagues(id) ON DELETE SET NULL,
    template_id UUID REFERENCES tournament_templates(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    entry_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    prize_description TEXT NOT NULL,
    format VARCHAR(30) NOT NULL,
    max_deck_budget NUMERIC(10, 2) NOT NULL,
    max_players INT NOT NULL DEFAULT 32,
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTRATION_OPEN',
    current_round INT NOT NULL DEFAULT 0,
    max_rounds INT NOT NULL DEFAULT 3,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tournament_template_schedule UNIQUE (template_id, scheduled_at)
);

CREATE TABLE IF NOT EXISTS scryfall_cards (
    id UUID PRIMARY KEY,
    oracle_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    mana_cost VARCHAR(60),
    cmc NUMERIC(5, 2) NOT NULL DEFAULT 0,
    type_line VARCHAR(200) NOT NULL,
    colors VARCHAR(20) ARRAY NOT NULL,
    color_identity VARCHAR(20) ARRAY NOT NULL,
    rarity VARCHAR(20) NOT NULL,
    legalities JSONB NOT NULL,
    price_eur NUMERIC(10, 2),
    price_usd NUMERIC(10, 2),
    image_uri VARCHAR(500),
    last_synced_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tournament_registrations (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deck_id UUID NOT NULL REFERENCES decks(id) ON DELETE RESTRICT,
    status VARCHAR(25) NOT NULL DEFAULT 'REGISTERED',
    calculated_deck_price NUMERIC(10, 2) NOT NULL,
    match_points INT NOT NULL DEFAULT 0,
    omw_percentage NUMERIC(5, 4) NOT NULL DEFAULT 0.0000,
    final_rank INT,
    deck_snapshot JSONB NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tournament_id, user_id)
);

CREATE TABLE IF NOT EXISTS registration_deck_cards (
    id UUID PRIMARY KEY,
    registration_id UUID NOT NULL REFERENCES tournament_registrations(id) ON DELETE CASCADE,
    scryfall_card_id UUID NOT NULL REFERENCES scryfall_cards(id) ON DELETE RESTRICT,
    board_section VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    unit_price_snapshot NUMERIC(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS rounds (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    round_number INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (tournament_id, round_number)
);

CREATE TABLE IF NOT EXISTS matches (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    table_number INT NOT NULL,
    player1_id UUID NOT NULL REFERENCES users(id),
    player2_id UUID REFERENCES users(id),
    player1_wins INT NOT NULL DEFAULT 0,
    player2_wins INT NOT NULL DEFAULT 0,
    draws INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    winner_id UUID REFERENCES users(id)
);

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

-- =============================================================================
-- Contract: flyway migration update for custom pairings and scoring rules
-- Feature: 004-custom-scoring-pairings
-- Target: PostgreSQL 15+ (Supabase / Neon)
-- =============================================================================

-- 1. Actualización de TOURNAMENT_TEMPLATES
-- Incorpora columnas de pairing y puntuación personalizada:
--   pairing_system VARCHAR(30) NOT NULL DEFAULT 'SWISS' CHECK (pairing_system IN ('SWISS', 'SINGLE_ELIMINATION', 'ROUND_ROBIN', 'CUSTOM')),
--   pairing_config JSONB,
--   points_per_win INT NOT NULL DEFAULT 3 CHECK (points_per_win >= 0),
--   points_per_draw INT NOT NULL DEFAULT 1 CHECK (points_per_draw >= 0),
--   points_per_loss INT NOT NULL DEFAULT 0 CHECK (points_per_loss >= 0),
--   points_per_bye INT NOT NULL DEFAULT 3 CHECK (points_per_bye >= 0),
--   scoring_rules JSONB,

-- 2. Actualización de TOURNAMENTS
-- Incorpora columnas idénticas a tournament_templates para pairings y puntuación.

-- 3. Actualización de LEAGUES
-- Incorpora columnas de puntuación de temporada:
--   scoring_system VARCHAR(30) NOT NULL DEFAULT 'CUMULATIVE_MATCH_POINTS' CHECK (scoring_system IN ('CUMULATIVE_MATCH_POINTS', 'RANK_BASED', 'PARTICIPATION_PLUS_RECORD', 'CUSTOM')),
--   participation_points INT NOT NULL DEFAULT 0 CHECK (participation_points >= 0),
--   best_n_tournaments INT CHECK (best_n_tournaments IS NULL OR best_n_tournaments > 0),
--   rank_points_distribution JSONB,

-- 4. Actualización de la Vista v_league_leaderboard
-- Cómputo dinámico integrando participación y puntos de torneos.


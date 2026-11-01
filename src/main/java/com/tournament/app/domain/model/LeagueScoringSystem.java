package com.tournament.app.domain.model;

/**
 * Estrategia de cálculo de puntuaciones y ranking para una liga multijornada.
 */
public enum LeagueScoringSystem {
    CUMULATIVE_MATCH_POINTS,
    RANK_BASED,
    PARTICIPATION_PLUS_RECORD,
    CUSTOM
}


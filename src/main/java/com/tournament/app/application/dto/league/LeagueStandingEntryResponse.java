package com.tournament.app.application.dto.league;

import java.util.UUID;

/**
 * Entrada en la tabla acumulada de posiciones de una liga multijornada.
 */
public record LeagueStandingEntryResponse(
    int rank,
    UUID userId,
    String username,
    int tournamentsPlayed,
    int accumulatedPoints
) {}


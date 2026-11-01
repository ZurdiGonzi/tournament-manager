package com.tournament.app.application.dto.round;

import java.util.List;
import java.util.UUID;

/**
 * Tabla completa de clasificación y desempates de un torneo.
 */
public record TournamentStandingsResponse(
    UUID tournamentId,
    int roundNumber,
    List<StandingEntryResponse> standings
) {
    public TournamentStandingsResponse {
        if (standings == null) {
            standings = List.of();
        }
    }
}


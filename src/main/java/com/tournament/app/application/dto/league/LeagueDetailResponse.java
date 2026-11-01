package com.tournament.app.application.dto.league;

import com.tournament.app.application.dto.tournament.TournamentSummaryResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detalle completo de una liga de tienda con sus torneos computables y clasificación.
 */
public record LeagueDetailResponse(
    UUID id,
    UUID storeId,
    String name,
    Instant startDate,
    Instant endDate,
    List<TournamentSummaryResponse> tournaments,
    List<LeagueStandingEntryResponse> standings
) {
    public LeagueDetailResponse {
        if (tournaments == null) {
            tournaments = List.of();
        }
        if (standings == null) {
            standings = List.of();
        }
    }
}


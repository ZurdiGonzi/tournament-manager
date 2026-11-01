package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LeagueStanding(
        UUID id,
        UUID leagueId,
        UUID userId,
        BigDecimal totalLeaguePoints,
        int matchdaysPlayed,
        int totalMatchWins,
        int totalMatchesPlayed,
        Instant updatedAt
) {
    public LeagueStanding {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (totalLeaguePoints == null) {
            totalLeaguePoints = BigDecimal.ZERO;
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
}

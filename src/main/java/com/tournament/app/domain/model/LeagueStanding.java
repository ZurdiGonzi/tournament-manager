package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record LeagueStanding(
    UUID leagueId,
    UUID userId,
    int totalPoints,
    int tournamentsPlayed,
    int matchesWon,
    int matchesDrawn,
    int matchesLost,
    Instant updatedAt
) {
    public LeagueStanding {
        Objects.requireNonNull(leagueId, "leagueId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        if (totalPoints < 0 || tournamentsPlayed < 0 || matchesWon < 0 || matchesDrawn < 0 || matchesLost < 0) {
            throw new IllegalArgumentException("league standing counters must be >= 0");
        }
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}

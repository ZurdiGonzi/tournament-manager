package com.tournament.app.domain.model;

import java.util.Objects;
import java.util.UUID;

public record LeagueLeaderboardEntry(
    UUID leagueId,
    UUID storeId,
    String leagueName,
    UUID userId,
    String username,
    long totalPoints,
    long tournamentsPlayed,
    long currentRank
) {
    public LeagueLeaderboardEntry {
        Objects.requireNonNull(leagueId, "leagueId must not be null");
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(leagueName, "leagueName must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(username, "username must not be null");
    }
}

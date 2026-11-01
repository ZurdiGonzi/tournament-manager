package com.tournament.app.domain.model;

import java.util.List;
import java.util.Objects;

public record LeagueDetailSummary(
    League league,
    List<Tournament> tournaments,
    List<LeagueLeaderboardEntry> leaderboard
) {
    public LeagueDetailSummary {
        Objects.requireNonNull(league, "league must not be null");
        tournaments = tournaments == null ? List.of() : List.copyOf(tournaments);
        leaderboard = leaderboard == null ? List.of() : List.copyOf(leaderboard);
    }
}

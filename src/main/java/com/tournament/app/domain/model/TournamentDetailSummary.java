package com.tournament.app.domain.model;

import java.util.List;
import java.util.Objects;

public record TournamentDetailSummary(
    Tournament tournament,
    long registeredPlayersCount,
    List<TournamentRegistration> standings,
    List<RoundWithMatches> rounds
) {
    public TournamentDetailSummary {
        Objects.requireNonNull(tournament, "tournament must not be null");
        standings = standings == null ? List.of() : List.copyOf(standings);
        rounds = rounds == null ? List.of() : List.copyOf(rounds);
    }

    public record RoundWithMatches(
        Round round,
        List<Match> matches
    ) {
        public RoundWithMatches {
            Objects.requireNonNull(round, "round must not be null");
            matches = matches == null ? List.of() : List.copyOf(matches);
        }
    }
}

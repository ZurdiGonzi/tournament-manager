package com.tournament.app.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Match(
    UUID id,
    UUID roundId,
    int tableNumber,
    UUID player1Id,
    UUID player2Id,
    int player1Wins,
    int player2Wins,
    int draws,
    MatchStatus status,
    UUID winnerId
) {
    public Match {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(roundId, "roundId must not be null");
        Objects.requireNonNull(player1Id, "player1Id must not be null");
        status = status == null ? MatchStatus.PENDING : status;
    }

    public boolean isBye() {
        return player2Id == null;
    }
}

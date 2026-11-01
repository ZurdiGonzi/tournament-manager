package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Round(
    UUID id,
    UUID tournamentId,
    int roundNumber,
    RoundStatus status,
    Instant startedAt,
    Instant endedAt
) {
    public Round {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tournamentId, "tournamentId must not be null");
        if (roundNumber <= 0) {
            throw new IllegalArgumentException("roundNumber must be > 0");
        }
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
    }
}

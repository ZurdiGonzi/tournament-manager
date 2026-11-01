package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Round(
        UUID id,
        UUID tournamentId,
        int roundNumber,
        String status,
        Instant startedAt,
        Instant endedAt
) {
    public Round {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null || status.isBlank()) {
            status = "IN_PROGRESS";
        }
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }
}

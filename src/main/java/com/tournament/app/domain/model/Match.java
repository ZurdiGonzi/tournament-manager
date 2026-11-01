package com.tournament.app.domain.model;

import java.util.List;
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
        String status,
        UUID winnerId,
        List<MatchParticipant> participants
) {
    public Match {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null || status.isBlank()) {
            status = "PENDING";
        }
        if (participants == null) {
            participants = List.of();
        }
    }
}

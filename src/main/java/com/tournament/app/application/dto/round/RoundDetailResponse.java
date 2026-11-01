package com.tournament.app.application.dto.round;

import com.tournament.app.domain.model.RoundStatus;
import java.util.List;
import java.util.UUID;

/**
 * Detalle completo de una ronda de torneo con su lista de emparejamientos.
 */
public record RoundDetailResponse(
    UUID roundId,
    UUID tournamentId,
    int roundNumber,
    RoundStatus status,
    List<MatchPairingResponse> pairings
) {
    public RoundDetailResponse {
        if (pairings == null) {
            pairings = List.of();
        }
    }
}


package com.tournament.app.application.dto.round;

import com.tournament.app.application.dto.auth.UserSummaryResponse;
import com.tournament.app.domain.model.MatchStatus;
import java.util.UUID;

/**
 * Representación de un emparejamiento de mesa en una ronda, con soporte de descanso/BYE.
 */
public record MatchPairingResponse(
    UUID matchId,
    int tableNumber,
    UserSummaryResponse player1,
    UserSummaryResponse player2,
    boolean isBye,
    MatchStatus status,
    Integer player1Wins,
    Integer player2Wins,
    Integer draws
) {}


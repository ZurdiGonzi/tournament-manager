package com.tournament.app.application.dto.round;

import com.tournament.app.domain.model.MatchStatus;
import java.util.UUID;

/**
 * Respuesta de estado de una partida tras registrarse o arbitrarse un resultado.
 */
public record MatchResultResponse(
    UUID matchId,
    MatchStatus status,
    UUID reportedByUserId,
    int player1Wins,
    int player2Wins,
    int draws,
    boolean resolvedByAdmin
) {}


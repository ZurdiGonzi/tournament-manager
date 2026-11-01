package com.tournament.app.application.dto.round;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Solicitud de reporte del resultado de una partida por un jugador o administrador.
 */
public record ReportMatchResultRequest(
    @NotNull(message = "El identificador de la partida es obligatorio")
    UUID matchId,

    @Min(value = 0, message = "Las victorias no pueden ser negativas")
    @Max(value = 2, message = "El máximo de partidas ganadas es 2")
    int player1Wins,

    @Min(value = 0, message = "Las victorias no pueden ser negativas")
    @Max(value = 2, message = "El máximo de partidas ganadas es 2")
    int player2Wins,

    @Min(value = 0, message = "Los empates no pueden ser negativos")
    @Max(value = 3, message = "El máximo de empates es 3")
    int draws
) {}


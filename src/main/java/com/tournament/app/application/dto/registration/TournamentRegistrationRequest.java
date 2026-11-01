package com.tournament.app.application.dto.registration;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Solicitud de inscripción de un jugador a un torneo con uno de sus mazos.
 */
public record TournamentRegistrationRequest(
    @NotNull(message = "El identificador del torneo es obligatorio")
    UUID tournamentId,

    @NotNull(message = "El identificador del mazo es obligatorio")
    UUID deckId
) {}


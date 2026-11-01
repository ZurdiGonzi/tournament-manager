package com.tournament.app.application.dto.registration;

import com.tournament.app.application.dto.deck.DeckSnapshotDto;
import com.tournament.app.domain.model.RegistrationStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Respuesta de confirmación de inscripción con snapshot inmutable del mazo.
 */
public record TournamentRegistrationResponse(
    UUID id,
    UUID tournamentId,
    UUID userId,
    String username,
    RegistrationStatus status,
    Instant registeredAt,
    DeckSnapshotDto deckSnapshot
) {}


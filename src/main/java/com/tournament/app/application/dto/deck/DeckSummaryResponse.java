package com.tournament.app.application.dto.deck;

import java.time.Instant;
import java.util.UUID;

/**
 * Resumen de mazo en la colección del jugador.
 */
public record DeckSummaryResponse(
    UUID id,
    String name,
    String moxfieldUrl,
    String moxfieldId,
    Instant createdAt
) {}


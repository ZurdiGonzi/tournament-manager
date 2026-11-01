package com.tournament.app.application.dto.deck;

import com.tournament.app.domain.model.TournamentFormat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Snapshot inmutable del mazo capturado al momento de la inscripción en un torneo.
 */
public record DeckSnapshotDto(
    UUID deckId,
    String deckName,
    String moxfieldUrl,
    TournamentFormat format,
    BigDecimal totalPriceEur,
    Instant capturedAt,
    List<DeckCardDto> cards
) {
    public DeckSnapshotDto {
        if (cards == null) {
            cards = List.of();
        }
    }
}


package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DeckSnapshot(
        String moxfieldDeckId,
        String moxfieldUrl,
        String deckName,
        String format,
        BigDecimal totalPrice,
        String currency,
        Instant capturedAt,
        List<CardEntry> commanders,
        List<CardEntry> mainboard,
        List<CardEntry> sideboard
) {
    public record CardEntry(
            UUID scryfallId,
            UUID oracleId,
            String name,
            int quantity,
            String manaCost,
            BigDecimal cmc,
            String typeLine,
            List<String> colors,
            List<String> colorIdentity,
            String rarity,
            BigDecimal unitPrice
    ) {
    }
}

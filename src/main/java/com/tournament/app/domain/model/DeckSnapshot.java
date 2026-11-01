package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record DeckSnapshot(
    UUID deckId,
    String deckName,
    String moxfieldDeckId,
    MagicFormat format,
    BigDecimal calculatedDeckPrice,
    Instant snapshotDate,
    List<SnapshotCardEntry> cards
) {
    public DeckSnapshot {
        Objects.requireNonNull(deckId, "deckId must not be null");
        Objects.requireNonNull(deckName, "deckName must not be null");
        Objects.requireNonNull(moxfieldDeckId, "moxfieldDeckId must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(calculatedDeckPrice, "calculatedDeckPrice must not be null");
        Objects.requireNonNull(snapshotDate, "snapshotDate must not be null");
        cards = cards == null ? List.of() : List.copyOf(cards);
    }

    public record SnapshotCardEntry(
        UUID scryfallCardId,
        UUID oracleId,
        String name,
        BoardSection boardSection,
        int quantity,
        String typeLine,
        String manaCost,
        BigDecimal cmc,
        List<String> colors,
        BigDecimal unitPriceSnapshot
    ) {
        public SnapshotCardEntry {
            Objects.requireNonNull(scryfallCardId, "scryfallCardId must not be null");
            Objects.requireNonNull(oracleId, "oracleId must not be null");
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(boardSection, "boardSection must not be null");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0");
            }
            colors = colors == null ? List.of() : List.copyOf(colors);
            Objects.requireNonNull(unitPriceSnapshot, "unitPriceSnapshot must not be null");
        }
    }
}

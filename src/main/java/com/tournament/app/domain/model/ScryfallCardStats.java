package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ScryfallCardStats(
    UUID storeId,
    UUID leagueId,
    MagicFormat format,
    UUID oracleId,
    String cardName,
    String typeLine,
    String manaCost,
    BigDecimal cmc,
    List<String> colors,
    List<String> colorIdentity,
    String rarity,
    long decksIncludingCard,
    long totalCopiesPlayed,
    BigDecimal avgUnitPrice
) {
    public ScryfallCardStats {
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(oracleId, "oracleId must not be null");
        Objects.requireNonNull(cardName, "cardName must not be null");
        colors = colors == null ? List.of() : List.copyOf(colors);
        colorIdentity = colorIdentity == null ? List.of() : List.copyOf(colorIdentity);
        avgUnitPrice = avgUnitPrice == null ? BigDecimal.ZERO : avgUnitPrice;
    }
}

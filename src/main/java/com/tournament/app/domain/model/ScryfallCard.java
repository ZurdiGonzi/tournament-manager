package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record ScryfallCard(
    UUID id,
    UUID oracleId,
    String name,
    String manaCost,
    BigDecimal cmc,
    String typeLine,
    List<String> colors,
    List<String> colorIdentity,
    String rarity,
    Map<String, String> legalities,
    BigDecimal priceEur,
    BigDecimal priceUsd,
    String imageUri,
    Instant lastSyncedAt
) {
    public ScryfallCard {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(oracleId, "oracleId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank() || name.length() > 200) {
            throw new IllegalArgumentException("card name must be between 1 and 200 characters");
        }
        cmc = cmc == null ? BigDecimal.ZERO : cmc;
        Objects.requireNonNull(typeLine, "typeLine must not be null");
        colors = colors == null ? List.of() : List.copyOf(colors);
        colorIdentity = colorIdentity == null ? List.of() : List.copyOf(colorIdentity);
        Objects.requireNonNull(rarity, "rarity must not be null");
        legalities = legalities == null ? Map.of() : Map.copyOf(legalities);
        Objects.requireNonNull(lastSyncedAt, "lastSyncedAt must not be null");
    }

    public boolean isLegalIn(MagicFormat format) {
        String legality = legalities.get(format.scryfallKey());
        return "legal".equalsIgnoreCase(legality) || "restricted".equalsIgnoreCase(legality);
    }

    public BigDecimal resolvePriceForCurrency(CurrencyCode currency) {
        if (currency == CurrencyCode.USD) {
            if (priceUsd != null) return priceUsd;
            return priceEur != null ? priceEur : BigDecimal.ZERO;
        }
        if (priceEur != null) return priceEur;
        return priceUsd != null ? priceUsd : BigDecimal.ZERO;
    }
}

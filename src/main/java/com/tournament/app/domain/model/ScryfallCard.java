package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    public boolean isLegalIn(String format) {
        if (legalities == null || format == null) {
            return false;
        }
        String status = legalities.get(format.toLowerCase());
        return "legal".equalsIgnoreCase(status) || "restricted".equalsIgnoreCase(status);
    }

    public boolean isBasicLand() {
        return typeLine != null && typeLine.contains("Basic Land");
    }

    public BigDecimal resolvePrice(String currency) {
        if ("USD".equalsIgnoreCase(currency)) {
            return priceUsd != null ? priceUsd : BigDecimal.ZERO;
        }
        return priceEur != null ? priceEur : BigDecimal.ZERO;
    }
}

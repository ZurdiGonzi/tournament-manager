package com.tournament.app.infrastructure.adapter.scryfall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tournament.app.domain.model.ScryfallCard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ScryfallCardResponse(
        UUID id,
        @JsonProperty("oracle_id") UUID oracleId,
        String name,
        @JsonProperty("mana_cost") String manaCost,
        BigDecimal cmc,
        @JsonProperty("type_line") String typeLine,
        List<String> colors,
        @JsonProperty("color_identity") List<String> colorIdentity,
        String rarity,
        Map<String, String> legalities,
        Map<String, String> prices,
        @JsonProperty("image_uris") Map<String, String> imageUris
) {
    public ScryfallCard toDomain() {
        BigDecimal eur = parsePrice(prices != null ? prices.get("eur") : null);
        BigDecimal usd = parsePrice(prices != null ? prices.get("usd") : null);
        String imageUri = imageUris != null ? imageUris.get("normal") : null;
        return new ScryfallCard(
                id,
                oracleId,
                name,
                manaCost != null ? manaCost : "",
                cmc != null ? cmc : BigDecimal.ZERO,
                typeLine != null ? typeLine : "",
                colors != null ? colors : List.of(),
                colorIdentity != null ? colorIdentity : List.of(),
                rarity != null ? rarity : "common",
                legalities != null ? legalities : Map.of(),
                eur,
                usd,
                imageUri,
                Instant.now()
        );
    }

    private static BigDecimal parsePrice(String raw) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    public record ScryfallCollectionBatchResponse(
            List<ScryfallCardResponse> data
    ) {
    }
}

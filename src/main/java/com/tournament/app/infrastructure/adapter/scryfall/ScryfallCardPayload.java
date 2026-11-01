package com.tournament.app.infrastructure.adapter.scryfall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tournament.app.domain.model.ScryfallCard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScryfallCardPayload(
    @JsonProperty("id") UUID id,
    @JsonProperty("oracle_id") UUID oracleId,
    @JsonProperty("name") String name,
    @JsonProperty("mana_cost") String manaCost,
    @JsonProperty("cmc") BigDecimal cmc,
    @JsonProperty("type_line") String typeLine,
    @JsonProperty("colors") List<String> colors,
    @JsonProperty("color_identity") List<String> colorIdentity,
    @JsonProperty("rarity") String rarity,
    @JsonProperty("legalities") Map<String, String> legalities,
    @JsonProperty("prices") PricesPayload prices,
    @JsonProperty("image_uris") ImageUrisPayload imageUris,
    @JsonProperty("card_faces") List<CardFacePayload> cardFaces
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PricesPayload(
        @JsonProperty("eur") String eur,
        @JsonProperty("eur_foil") String eurFoil,
        @JsonProperty("usd") String usd,
        @JsonProperty("usd_foil") String usdFoil
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageUrisPayload(
        @JsonProperty("normal") String normal,
        @JsonProperty("small") String small
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CardFacePayload(
        @JsonProperty("name") String name,
        @JsonProperty("mana_cost") String manaCost,
        @JsonProperty("colors") List<String> colors,
        @JsonProperty("image_uris") ImageUrisPayload imageUris
    ) {}

    public ScryfallCard toDomain(Instant syncedAt) {
        String resolvedManaCost = manaCost;
        List<String> resolvedColors = colors;
        String resolvedImageUri = imageUris != null ? imageUris.normal() : null;

        if (cardFaces != null && !cardFaces.isEmpty()) {
            CardFacePayload firstFace = cardFaces.get(0);
            if ((resolvedManaCost == null || resolvedManaCost.isBlank()) && firstFace.manaCost() != null) {
                resolvedManaCost = firstFace.manaCost();
            }
            if ((resolvedColors == null || resolvedColors.isEmpty()) && firstFace.colors() != null) {
                resolvedColors = firstFace.colors();
            }
            if (resolvedImageUri == null && firstFace.imageUris() != null) {
                resolvedImageUri = firstFace.imageUris().normal();
            }
        }

        BigDecimal eurPrice = parseDecimal(prices != null ? prices.eur() : null);
        if (eurPrice == null && prices != null) {
            eurPrice = parseDecimal(prices.eurFoil());
        }
        BigDecimal usdPrice = parseDecimal(prices != null ? prices.usd() : null);
        if (usdPrice == null && prices != null) {
            usdPrice = parseDecimal(prices.usdFoil());
        }

        UUID effectiveOracleId = oracleId != null ? oracleId : id;
        return new ScryfallCard(
            id,
            effectiveOracleId,
            name != null ? name : "Unknown Card",
            resolvedManaCost,
            cmc != null ? cmc : BigDecimal.ZERO,
            typeLine != null ? typeLine : "Unknown",
            resolvedColors != null ? resolvedColors : List.of(),
            colorIdentity != null ? colorIdentity : List.of(),
            rarity != null ? rarity : "common",
            legalities != null ? legalities : Map.of(),
            eurPrice,
            usdPrice,
            resolvedImageUri,
            syncedAt != null ? syncedAt : Instant.now()
        );
    }

    private static BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

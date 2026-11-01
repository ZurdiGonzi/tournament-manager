package com.tournament.app.application.dto.deck;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Representación de una carta individual dentro del snapshot de un mazo.
 */
public record DeckCardDto(
    UUID scryfallId,
    UUID oracleId,
    String name,
    @Min(value = 1, message = "La cantidad mínima por carta es 1")
    int quantity,
    String manaCost,
    BigDecimal manaValue,
    String typeLine,
    List<String> colors,
    List<String> colorIdentity,
    BigDecimal unitPriceEur,
    boolean isCommander
) {
    public DeckCardDto {
        if (colors == null) {
            colors = List.of();
        }
        if (colorIdentity == null) {
            colorIdentity = List.of();
        }
    }
}


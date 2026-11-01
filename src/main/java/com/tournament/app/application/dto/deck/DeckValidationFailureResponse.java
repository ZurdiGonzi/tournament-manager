package com.tournament.app.application.dto.deck;

import java.math.BigDecimal;
import java.util.List;

/**
 * Detalle del rechazo de un mazo al intentar inscribirse en un torneo.
 */
public record DeckValidationFailureResponse(
    String reason,
    BigDecimal maxAllowedBudgetEur,
    BigDecimal evaluatedBudgetEur,
    List<String> violatingCards
) {
    public DeckValidationFailureResponse {
        if (violatingCards == null) {
            violatingCards = List.of();
        }
    }
}


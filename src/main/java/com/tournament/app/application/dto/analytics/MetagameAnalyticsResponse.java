package com.tournament.app.application.dto.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Resumen analítico global de cartas, costes y colores para un torneo o tienda.
 */
public record MetagameAnalyticsResponse(
    String scope,
    UUID targetId,
    int totalDecksAnalyzed,
    BigDecimal averageDeckBudgetEur,
    List<ColorBreakdownDto> colorDistribution,
    List<CardUsageStatDto> topPlayedCards
) {
    public MetagameAnalyticsResponse {
        if (colorDistribution == null) {
            colorDistribution = List.of();
        }
        if (topPlayedCards == null) {
            topPlayedCards = List.of();
        }
    }
}


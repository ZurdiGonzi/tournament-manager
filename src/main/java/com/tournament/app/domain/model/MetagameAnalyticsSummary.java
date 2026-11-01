package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record MetagameAnalyticsSummary(
    UUID storeId,
    UUID leagueId,
    UUID tournamentId,
    MagicFormat format,
    long totalDecksAnalyzed,
    BigDecimal averageDeckPrice,
    Map<String, BigDecimal> colorDistributionPercent,
    Map<String, BigDecimal> manaCurveAverage,
    List<CardUsageSummary> mostPlayedCards
) {
    public MetagameAnalyticsSummary {
        Objects.requireNonNull(storeId, "storeId must not be null");
        averageDeckPrice = averageDeckPrice == null ? BigDecimal.ZERO : averageDeckPrice;
        colorDistributionPercent = colorDistributionPercent == null ? Map.of() : Map.copyOf(colorDistributionPercent);
        manaCurveAverage = manaCurveAverage == null ? Map.of() : Map.copyOf(manaCurveAverage);
        mostPlayedCards = mostPlayedCards == null ? List.of() : List.copyOf(mostPlayedCards);
    }

    public record CardUsageSummary(
        UUID oracleId,
        String cardName,
        String typeLine,
        String manaCost,
        String rarity,
        long decksIncludingCard,
        long totalCopiesPlayed,
        BigDecimal inclusionRatePercent,
        BigDecimal avgUnitPrice
    ) {}
}

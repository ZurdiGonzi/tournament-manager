package com.tournament.app.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AnalyticsDtos {

    private AnalyticsDtos() {
    }

    public record MostPlayedCardStat(
            UUID oracleId,
            String cardName,
            String typeLine,
            int decksCount,
            double presencePercentage,
            int totalCopies
    ) {
    }

    public record CommanderWinStat(
            UUID commanderOracleId,
            String commanderName,
            List<String> colorIdentity,
            int matchesPlayed,
            int matchesWon,
            double winRatePercentage,
            BigDecimal averagePointsEarned
    ) {
    }

    public record CommanderMatchupStat(
            String commanderA,
            String commanderB,
            int matchesTogether,
            int commanderAWins,
            int commanderBWins,
            String dominantCommander,
            double dominantWinPercentage,
            String summaryLabel
    ) {
    }

    public record MatchdayDeckAnalyticsResponse(
            UUID scopeId,
            String scopeType,
            int totalDecks,
            List<MostPlayedCardStat> mostPlayedCards,
            Map<String, Double> mostPlayedColors,
            List<CommanderWinStat> winningestCommanders,
            List<CommanderMatchupStat> commanderMatchups
    ) {
    }

    public record RivalMatchupStat(
            String rivalCommanderName,
            int matchesPlayed,
            int winsAgainstRival,
            double winRatePercentage,
            String summary
    ) {
    }

    public record ScryfallDeckCompositionStat(
            double averageCmc,
            double matchdayAverageCmcComparison,
            Map<String, Integer> colorBreakdown,
            Map<String, Integer> typeBreakdown,
            BigDecimal totalPrice
    ) {
    }

    public record PersonalDeckAnalyticsResponse(
            UUID deckId,
            String deckName,
            String commanderName,
            List<String> colorIdentity,
            String scope,
            int matchesPlayed,
            int matchesWon,
            double winRatePercentage,
            BigDecimal averageMatchPoints,
            BigDecimal averageMatchdayLeaguePoints,
            List<RivalMatchupStat> rivalMatchups,
            ScryfallDeckCompositionStat scryfallComposition
    ) {
    }
}

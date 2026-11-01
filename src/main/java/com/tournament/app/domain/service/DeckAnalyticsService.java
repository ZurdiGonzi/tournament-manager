package com.tournament.app.domain.service;

import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.TournamentRegistration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeckAnalyticsService {

    public record MostPlayedCardMetric(
            UUID oracleId,
            String cardName,
            String typeLine,
            int decksCount,
            double presencePercentage,
            int totalCopies
    ) {
    }

    public record CommanderWinMetric(
            UUID commanderOracleId,
            String commanderName,
            List<String> colorIdentity,
            int matchesPlayed,
            int matchesWon,
            double winRatePercentage,
            BigDecimal averagePointsEarned
    ) {
    }

    public record CommanderMatchupMetric(
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

    public record MatchdayAnalyticsSummary(
            UUID scopeId,
            String scopeType,
            int totalDecks,
            List<MostPlayedCardMetric> mostPlayedCards,
            Map<String, Double> mostPlayedColors,
            List<CommanderWinMetric> winningestCommanders,
            List<CommanderMatchupMetric> commanderMatchups
    ) {
    }

    public record RivalMatchupMetric(
            String rivalCommanderName,
            int matchesPlayed,
            int winsAgainstRival,
            double winRatePercentage,
            String summary
    ) {
    }

    public record ScryfallCompositionMetric(
            double averageCmc,
            double matchdayAverageCmcComparison,
            Map<String, Integer> colorBreakdown,
            Map<String, Integer> typeBreakdown,
            BigDecimal totalPrice
    ) {
    }

    public record PersonalDeckAnalyticsSummary(
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
            List<RivalMatchupMetric> rivalMatchups,
            ScryfallCompositionMetric scryfallComposition
    ) {
    }

    public MatchdayAnalyticsSummary computeMatchdayOrLeagueAnalytics(
            UUID scopeId,
            String scopeType,
            List<TournamentRegistration> registrations,
            List<Match> confirmedMatches,
            boolean includeBasicLands) {

        int totalDecks = registrations.size();
        List<MostPlayedCardMetric> mostPlayedCards = computeMostPlayedCards(registrations, includeBasicLands);
        Map<String, Double> mostPlayedColors = computeColorDistribution(registrations);
        List<CommanderWinMetric> winningestCommanders = computeWinningestCommanders(registrations, confirmedMatches);
        List<CommanderMatchupMetric> commanderMatchups = computeCommanderMatchups(confirmedMatches);

        return new MatchdayAnalyticsSummary(
                scopeId,
                scopeType,
                totalDecks,
                mostPlayedCards,
                mostPlayedColors,
                winningestCommanders,
                commanderMatchups
        );
    }

    public PersonalDeckAnalyticsSummary computePersonalDeckAnalytics(
            Deck deck,
            String scope,
            List<TournamentRegistration> allScopeRegistrations,
            List<Match> confirmedMatches) {

        List<TournamentRegistration> myRegistrations = allScopeRegistrations.stream()
                .filter(r -> r.deckId().equals(deck.id()) || r.userId().equals(deck.userId()))
                .toList();

        int matchesPlayed = 0;
        int matchesWon = 0;
        BigDecimal totalMatchPts = BigDecimal.ZERO;
        Map<String, int[]> rivalRecord = new HashMap<>(); // [played, winsAgainst]

        for (Match match : confirmedMatches) {
            if (!"CONFIRMED".equalsIgnoreCase(match.status())) {
                continue;
            }
            MatchParticipant myPart = match.participants().stream()
                    .filter(mp -> mp.deckId().equals(deck.id()) || mp.userId().equals(deck.userId()))
                    .findFirst()
                    .orElse(null);

            if (myPart == null) {
                continue;
            }

            matchesPlayed++;
            boolean wonMatch = isWinner(myPart, match);
            if (wonMatch) {
                matchesWon++;
            }
            totalMatchPts = totalMatchPts.add(myPart.matchPointsEarned());

            for (MatchParticipant rival : match.participants()) {
                if (rival.userId().equals(myPart.userId())) {
                    continue;
                }
                int[] counts = rivalRecord.computeIfAbsent(rival.commanderName(), k -> new int[2]);
                counts[0]++;
                if (didOutperform(myPart, rival, match)) {
                    counts[1]++;
                }
            }
        }

        double winRate = matchesPlayed == 0 ? 0.0 : round2((matchesWon * 100.0) / matchesPlayed);
        BigDecimal avgMatchPts = matchesPlayed == 0
                ? BigDecimal.ZERO
                : totalMatchPts.divide(BigDecimal.valueOf(matchesPlayed), 2, RoundingMode.HALF_UP);

        BigDecimal avgLeaguePts = myRegistrations.isEmpty()
                ? BigDecimal.ZERO
                : myRegistrations.stream()
                        .map(TournamentRegistration::calculatedLeagueMatchdayPoints)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(myRegistrations.size()), 2, RoundingMode.HALF_UP);

        String myCmdShort = shortName(deck.commanderName() != null ? deck.commanderName() : deck.name());
        List<RivalMatchupMetric> rivalMatchups = rivalRecord.entrySet().stream()
                .map(entry -> {
                    String rivalName = entry.getKey();
                    int played = entry.getValue()[0];
                    int wins = entry.getValue()[1];
                    double rate = played == 0 ? 0.0 : round2((wins * 100.0) / played);
                    String dominant = rate >= 50.0 ? myCmdShort : shortName(rivalName);
                    double domRate = rate >= 50.0 ? rate : round2(100.0 - rate);
                    String summary = String.format("%s vs %s: gana %s %.0f%%",
                            myCmdShort, shortName(rivalName), dominant, domRate);
                    return new RivalMatchupMetric(rivalName, played, wins, rate, summary);
                })
                .sorted(Comparator.comparingInt(RivalMatchupMetric::matchesPlayed).reversed())
                .toList();

        ScryfallCompositionMetric composition = computeScryfallComposition(myRegistrations, allScopeRegistrations, deck);

        return new PersonalDeckAnalyticsSummary(
                deck.id(),
                deck.name(),
                deck.commanderName() != null ? deck.commanderName() : deck.name(),
                deck.colorIdentity(),
                scope,
                matchesPlayed,
                matchesWon,
                winRate,
                avgMatchPts,
                avgLeaguePts,
                rivalMatchups,
                composition
        );
    }

    private List<MostPlayedCardMetric> computeMostPlayedCards(
            List<TournamentRegistration> registrations,
            boolean includeBasicLands) {
        if (registrations.isEmpty()) {
            return List.of();
        }
        int totalDecks = registrations.size();
        Map<UUID, String> cardNames = new HashMap<>();
        Map<UUID, String> typeLines = new HashMap<>();
        Map<UUID, Integer> decksCount = new HashMap<>();
        Map<UUID, Integer> copiesCount = new HashMap<>();

        for (TournamentRegistration reg : registrations) {
            DeckSnapshot snap = reg.deckSnapshot();
            if (snap == null) {
                continue;
            }
            List<DeckSnapshot.CardEntry> allCards = new ArrayList<>();
            if (snap.commanders() != null) allCards.addAll(snap.commanders());
            if (snap.mainboard() != null) allCards.addAll(snap.mainboard());

            Map<UUID, Integer> deckCopies = new HashMap<>();
            for (DeckSnapshot.CardEntry card : allCards) {
                if (!includeBasicLands && card.typeLine() != null && card.typeLine().contains("Basic Land")) {
                    continue;
                }
                cardNames.put(card.oracleId(), card.name());
                typeLines.put(card.oracleId(), card.typeLine());
                deckCopies.merge(card.oracleId(), card.quantity(), Integer::sum);
            }
            for (Map.Entry<UUID, Integer> entry : deckCopies.entrySet()) {
                decksCount.merge(entry.getKey(), 1, Integer::sum);
                copiesCount.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        return decksCount.entrySet().stream()
                .map(e -> {
                    UUID oracleId = e.getKey();
                    int decks = e.getValue();
                    int copies = copiesCount.getOrDefault(oracleId, 0);
                    double pct = round2((decks * 100.0) / totalDecks);
                    return new MostPlayedCardMetric(
                            oracleId,
                            cardNames.get(oracleId),
                            typeLines.get(oracleId),
                            decks,
                            pct,
                            copies
                    );
                })
                .sorted(Comparator.comparingInt(MostPlayedCardMetric::decksCount).reversed()
                        .thenComparing(Comparator.comparingInt(MostPlayedCardMetric::totalCopies).reversed())
                        .thenComparing(MostPlayedCardMetric::cardName))
                .toList();
    }

    private Map<String, Double> computeColorDistribution(List<TournamentRegistration> registrations) {
        Map<String, Double> distribution = new LinkedHashMap<>();
        List<String> wubrg = List.of("W", "U", "B", "R", "G");
        for (String c : wubrg) {
            distribution.put(c, 0.0);
        }
        if (registrations.isEmpty()) {
            return distribution;
        }
        int totalDecks = registrations.size();
        for (String color : wubrg) {
            long count = registrations.stream()
                    .filter(r -> r.colorIdentity() != null && r.colorIdentity().contains(color))
                    .count();
            distribution.put(color, round2((count * 100.0) / totalDecks));
        }
        return distribution;
    }

    private List<CommanderWinMetric> computeWinningestCommanders(
            List<TournamentRegistration> registrations,
            List<Match> confirmedMatches) {

        Map<String, UUID> oracleByCmd = new HashMap<>();
        Map<String, List<String>> colorsByCmd = new HashMap<>();
        for (TournamentRegistration reg : registrations) {
            String cmd = reg.commanderName() != null ? reg.commanderName() : reg.deckSnapshot().deckName();
            oracleByCmd.putIfAbsent(cmd, reg.commanderOracleId());
            colorsByCmd.putIfAbsent(cmd, reg.colorIdentity());
        }

        Map<String, Integer> playedByCmd = new HashMap<>();
        Map<String, Integer> winsByCmd = new HashMap<>();
        Map<String, BigDecimal> pointsByCmd = new HashMap<>();

        for (Match match : confirmedMatches) {
            if (!"CONFIRMED".equalsIgnoreCase(match.status())) {
                continue;
            }
            for (MatchParticipant mp : match.participants()) {
                String cmd = mp.commanderName();
                oracleByCmd.putIfAbsent(cmd, mp.commanderOracleId());
                playedByCmd.merge(cmd, 1, Integer::sum);
                if (isWinner(mp, match)) {
                    winsByCmd.merge(cmd, 1, Integer::sum);
                }
                pointsByCmd.merge(cmd, mp.matchPointsEarned(), BigDecimal::add);
            }
        }

        return playedByCmd.entrySet().stream()
                .map(e -> {
                    String cmd = e.getKey();
                    int played = e.getValue();
                    int wins = winsByCmd.getOrDefault(cmd, 0);
                    BigDecimal totalPts = pointsByCmd.getOrDefault(cmd, BigDecimal.ZERO);
                    BigDecimal avgPts = played == 0
                            ? BigDecimal.ZERO
                            : totalPts.divide(BigDecimal.valueOf(played), 2, RoundingMode.HALF_UP);
                    double winRate = played == 0 ? 0.0 : round2((wins * 100.0) / played);
                    return new CommanderWinMetric(
                            oracleByCmd.get(cmd),
                            cmd,
                            colorsByCmd.getOrDefault(cmd, List.of()),
                            played,
                            wins,
                            winRate,
                            avgPts
                    );
                })
                .sorted(Comparator.comparingDouble(CommanderWinMetric::winRatePercentage).reversed()
                        .thenComparing(CommanderWinMetric::averagePointsEarned, Comparator.reverseOrder()))
                .toList();
    }

    private List<CommanderMatchupMetric> computeCommanderMatchups(List<Match> confirmedMatches) {
        Map<String, int[]> pairStats = new LinkedHashMap<>(); // key "CmdA||CmdB" -> [matchesTogether, aWins, bWins]

        for (Match match : confirmedMatches) {
            if (!"CONFIRMED".equalsIgnoreCase(match.status())) {
                continue;
            }
            List<MatchParticipant> parts = match.participants();
            for (int i = 0; i < parts.size(); i++) {
                for (int j = i + 1; j < parts.size(); j++) {
                    MatchParticipant p1 = parts.get(i);
                    MatchParticipant p2 = parts.get(j);
                    if (p1.commanderName().equalsIgnoreCase(p2.commanderName())) {
                        continue;
                    }
                    boolean firstIsA = p1.commanderName().compareToIgnoreCase(p2.commanderName()) <= 0;
                    MatchParticipant sideA = firstIsA ? p1 : p2;
                    MatchParticipant sideB = firstIsA ? p2 : p1;
                    String key = sideA.commanderName() + "||" + sideB.commanderName();

                    int[] stats = pairStats.computeIfAbsent(key, k -> new int[3]);
                    stats[0]++;
                    if (didOutperform(sideA, sideB, match)) {
                        stats[1]++;
                    } else if (didOutperform(sideB, sideA, match)) {
                        stats[2]++;
                    }
                }
            }
        }

        return pairStats.entrySet().stream()
                .map(entry -> {
                    String[] names = entry.getKey().split("\\|\\|");
                    String cmdA = names[0];
                    String cmdB = names[1];
                    int total = entry.getValue()[0];
                    int aWins = entry.getValue()[1];
                    int bWins = entry.getValue()[2];

                    boolean bDominates = bWins >= aWins;
                    String dominant = bDominates ? cmdB : cmdA;
                    int domWins = bDominates ? bWins : aWins;
                    double domPct = total == 0 ? 0.0 : round2((domWins * 100.0) / total);
                    String label = String.format("%s vs %s: gana %s %.0f%%",
                            shortName(cmdA), shortName(cmdB), shortName(dominant), domPct);

                    return new CommanderMatchupMetric(
                            cmdA, cmdB, total, aWins, bWins, dominant, domPct, label
                    );
                })
                .sorted(Comparator.comparingInt(CommanderMatchupMetric::matchesTogether).reversed())
                .toList();
    }

    private ScryfallCompositionMetric computeScryfallComposition(
            List<TournamentRegistration> myRegistrations,
            List<TournamentRegistration> allScopeRegistrations,
            Deck deck) {

        if (myRegistrations.isEmpty()) {
            return new ScryfallCompositionMetric(
                    0.0, 0.0, Map.of(), Map.of(),
                    deck.lastCalculatedPrice() != null ? deck.lastCalculatedPrice() : BigDecimal.ZERO
            );
        }

        TournamentRegistration latestReg = myRegistrations.getLast();
        double myAvgCmc = calculateDeckAverageCmc(latestReg.deckSnapshot());
        double scopeAvgCmc = allScopeRegistrations.stream()
                .mapToDouble(r -> calculateDeckAverageCmc(r.deckSnapshot()))
                .average()
                .orElse(myAvgCmc);

        Map<String, Integer> colorBreakdown = new LinkedHashMap<>();
        Map<String, Integer> typeBreakdown = new LinkedHashMap<>();
        DeckSnapshot snap = latestReg.deckSnapshot();
        if (snap != null && snap.mainboard() != null) {
            for (DeckSnapshot.CardEntry card : snap.mainboard()) {
                if (card.colors() != null) {
                    for (String col : card.colors()) {
                        colorBreakdown.merge(col, card.quantity(), Integer::sum);
                    }
                }
                String primaryType = extractPrimaryType(card.typeLine());
                typeBreakdown.merge(primaryType, card.quantity(), Integer::sum);
            }
        }

        return new ScryfallCompositionMetric(
                round2(myAvgCmc),
                round2(myAvgCmc - scopeAvgCmc),
                colorBreakdown,
                typeBreakdown,
                latestReg.calculatedDeckPrice()
        );
    }

    private double calculateDeckAverageCmc(DeckSnapshot snap) {
        if (snap == null || snap.mainboard() == null) {
            return 0.0;
        }
        double weightedSum = 0.0;
        int nonLandCards = 0;
        for (DeckSnapshot.CardEntry card : snap.mainboard()) {
            if (card.typeLine() != null && card.typeLine().contains("Land")) {
                continue;
            }
            weightedSum += (card.cmc() != null ? card.cmc().doubleValue() : 0.0) * card.quantity();
            nonLandCards += card.quantity();
        }
        return nonLandCards == 0 ? 0.0 : (weightedSum / nonLandCards);
    }

    private String extractPrimaryType(String typeLine) {
        if (typeLine == null || typeLine.isBlank()) {
            return "Other";
        }
        for (String known : List.of("Creature", "Instant", "Sorcery", "Artifact", "Enchantment", "Planeswalker", "Land")) {
            if (typeLine.contains(known)) {
                return known;
            }
        }
        return "Other";
    }

    private boolean isWinner(MatchParticipant mp, Match match) {
        if (match.winnerId() != null && match.winnerId().equals(mp.userId())) {
            return true;
        }
        return mp.placement() == 1 && (mp.wins() > 0 || match.participants().size() > 2);
    }

    private boolean didOutperform(MatchParticipant a, MatchParticipant b, Match match) {
        if (a.placement() != b.placement()) {
            return a.placement() < b.placement();
        }
        if (a.wins() != b.wins()) {
            return a.wins() > b.wins();
        }
        return match.winnerId() != null && match.winnerId().equals(a.userId());
    }

    private static String shortName(String commanderName) {
        if (commanderName == null) {
            return "Unknown";
        }
        int commaIdx = commanderName.indexOf(',');
        return commaIdx > 0 ? commanderName.substring(0, commaIdx).trim() : commanderName.trim();
    }

    private static double round2(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

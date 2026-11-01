package com.tournament.app.domain.service;

import com.tournament.app.domain.model.DomainExceptions.IllegalPairingException;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SwissPairingEngine {

    private final FormulaEvaluationEngine formulaEvaluationEngine;

    public SwissPairingEngine(FormulaEvaluationEngine formulaEvaluationEngine) {
        this.formulaEvaluationEngine = formulaEvaluationEngine;
    }

    public record GeneratedRoundResult(
            Tournament updatedTournament,
            Round round,
            List<Match> matches
    ) {
    }

    public GeneratedRoundResult generateNextRound(
            Tournament tournament,
            List<TournamentRegistration> registrations,
            List<Match> previousMatches,
            Map<UUID, BigDecimal> leaguePointsByUser,
            String pairingFormulaExpression) {

        List<TournamentRegistration> activeRegistrations = registrations.stream()
                .filter(r -> "REGISTERED".equals(r.status()) || "CHECKED_IN".equals(r.status()))
                .toList();

        if (activeRegistrations.size() < 2) {
            throw new IllegalPairingException("At least 2 active players are required to generate pairings");
        }

        int nextRoundNumber = tournament.currentRound() + 1;
        Tournament updatedTournament = tournament.advanceToRound(nextRoundNumber);

        String formula = (pairingFormulaExpression == null || pairingFormulaExpression.isBlank())
                ? "(current_points * 100) + (league_points * 10) + (omw_percentage * 10)"
                : pairingFormulaExpression;

        Map<UUID, Set<UUID>> previousOpponents = buildOpponentHistory(previousMatches);
        Set<UUID> playersWithPreviousBye = findPlayersWithPreviousBye(previousMatches);
        Map<UUID, Double> omwMap = computeOmwPercentages(activeRegistrations, previousOpponents);

        record ScoredPlayer(TournamentRegistration registration, BigDecimal pairingScore) {
        }

        List<ScoredPlayer> rankedPlayers = activeRegistrations.stream()
                .map(reg -> {
                    double currentPts = reg.tournamentPoints() != null ? reg.tournamentPoints().doubleValue() : 0.0;
                    double leaguePts = leaguePointsByUser != null && leaguePointsByUser.containsKey(reg.userId())
                            ? leaguePointsByUser.get(reg.userId()).doubleValue()
                            : 0.0;
                    double omw = omwMap.getOrDefault(reg.userId(), 0.33);
                    BigDecimal score = formulaEvaluationEngine.evaluate(formula, Map.of(
                            "current_points", currentPts,
                            "tournament_points", currentPts,
                            "league_points", leaguePts,
                            "omw_percentage", omw,
                            "times_paired", 0.0
                    ));
                    return new ScoredPlayer(reg, score);
                })
                .sorted(Comparator.comparing(ScoredPlayer::pairingScore).reversed()
                        .thenComparing(sp -> sp.registration().userId().toString()))
                .toList();

        Round round = new Round(
                UUID.randomUUID(),
                tournament.id(),
                nextRoundNumber,
                "IN_PROGRESS",
                Instant.now(),
                null
        );

        List<TournamentRegistration> ordered = rankedPlayers.stream().map(ScoredPlayer::registration).toList();
        List<Match> generatedMatches = "COMMANDER".equalsIgnoreCase(tournament.format())
                ? pairCommanderPods(round.id(), ordered)
                : pairStandardDuels(round.id(), ordered, previousOpponents, playersWithPreviousBye);

        return new GeneratedRoundResult(updatedTournament, round, generatedMatches);
    }

    private List<Match> pairStandardDuels(
            UUID roundId,
            List<TournamentRegistration> ordered,
            Map<UUID, Set<UUID>> previousOpponents,
            Set<UUID> playersWithPreviousBye) {

        List<TournamentRegistration> pool = new ArrayList<>(ordered);
        TournamentRegistration byeRecipient = null;

        if (pool.size() % 2 != 0) {
            for (int i = pool.size() - 1; i >= 0; i--) {
                if (!playersWithPreviousBye.contains(pool.get(i).userId())) {
                    byeRecipient = pool.remove(i);
                    break;
                }
            }
            if (byeRecipient == null) {
                byeRecipient = pool.removeLast();
            }
        }

        List<Match> matches = new ArrayList<>();
        boolean[] used = new boolean[pool.size()];
        int tableNumber = 1;

        for (int i = 0; i < pool.size(); i++) {
            if (used[i]) {
                continue;
            }
            used[i] = true;
            TournamentRegistration p1 = pool.get(i);
            Set<UUID> p1Opponents = previousOpponents.getOrDefault(p1.userId(), Set.of());

            int chosenIdx = -1;
            for (int j = i + 1; j < pool.size(); j++) {
                if (!used[j] && !p1Opponents.contains(pool.get(j).userId())) {
                    chosenIdx = j;
                    break;
                }
            }
            if (chosenIdx == -1) {
                for (int j = i + 1; j < pool.size(); j++) {
                    if (!used[j]) {
                        chosenIdx = j;
                        break;
                    }
                }
            }

            used[chosenIdx] = true;
            TournamentRegistration p2 = pool.get(chosenIdx);
            UUID matchId = UUID.randomUUID();
            List<MatchParticipant> participants = List.of(
                    toParticipant(matchId, p1, 1, 0, BigDecimal.ZERO),
                    toParticipant(matchId, p2, 1, 0, BigDecimal.ZERO)
            );
            matches.add(new Match(
                    matchId, roundId, tableNumber++, p1.userId(), p2.userId(),
                    0, 0, 0, "IN_PROGRESS", null, participants
            ));
        }

        if (byeRecipient != null) {
            UUID matchId = UUID.randomUUID();
            MatchParticipant byeParticipant = toParticipant(matchId, byeRecipient, 1, 1, BigDecimal.valueOf(3.00));
            matches.add(new Match(
                    matchId, roundId, tableNumber, byeRecipient.userId(), null,
                    1, 0, 0, "CONFIRMED", byeRecipient.userId(), List.of(byeParticipant)
            ));
        }

        return matches;
    }

    private List<Match> pairCommanderPods(UUID roundId, List<TournamentRegistration> ordered) {
        List<Match> matches = new ArrayList<>();
        int idx = 0;
        int tableNumber = 1;
        int remaining = ordered.size();

        while (remaining > 0) {
            int podSize;
            if (remaining == 4 || remaining >= 8 || remaining == 7) {
                podSize = 4;
            } else if (remaining == 6 || remaining == 5 || remaining == 3) {
                podSize = 3;
            } else {
                podSize = remaining;
            }

            List<TournamentRegistration> podRegs = ordered.subList(idx, idx + podSize);
            UUID matchId = UUID.randomUUID();
            List<MatchParticipant> participants = podRegs.stream()
                    .map(reg -> toParticipant(matchId, reg, 1, 0, BigDecimal.ZERO))
                    .toList();

            UUID p1 = podRegs.getFirst().userId();
            UUID p2 = podRegs.size() > 1 ? podRegs.get(1).userId() : null;
            matches.add(new Match(
                    matchId, roundId, tableNumber++, p1, p2,
                    0, 0, 0, "IN_PROGRESS", null, participants
            ));

            idx += podSize;
            remaining -= podSize;
        }
        return matches;
    }

    private MatchParticipant toParticipant(
            UUID matchId,
            TournamentRegistration reg,
            int placement,
            int wins,
            BigDecimal pointsEarned) {
        String cmdName = (reg.commanderName() != null && !reg.commanderName().isBlank())
                ? reg.commanderName()
                : reg.deckSnapshot().deckName();
        return new MatchParticipant(
                UUID.randomUUID(),
                matchId,
                reg.id(),
                reg.userId(),
                reg.deckId(),
                reg.commanderOracleId(),
                cmdName,
                placement,
                wins,
                0,
                0,
                0,
                pointsEarned
        );
    }

    private Map<UUID, Set<UUID>> buildOpponentHistory(List<Match> previousMatches) {
        Map<UUID, Set<UUID>> history = new HashMap<>();
        if (previousMatches == null) {
            return history;
        }
        for (Match match : previousMatches) {
            for (MatchParticipant mp1 : match.participants()) {
                for (MatchParticipant mp2 : match.participants()) {
                    if (!mp1.userId().equals(mp2.userId())) {
                        history.computeIfAbsent(mp1.userId(), k -> new HashSet<>()).add(mp2.userId());
                    }
                }
            }
            if (match.player1Id() != null && match.player2Id() != null) {
                history.computeIfAbsent(match.player1Id(), k -> new HashSet<>()).add(match.player2Id());
                history.computeIfAbsent(match.player2Id(), k -> new HashSet<>()).add(match.player1Id());
            }
        }
        return history;
    }

    private Set<UUID> findPlayersWithPreviousBye(List<Match> previousMatches) {
        Set<UUID> byes = new HashSet<>();
        if (previousMatches == null) {
            return byes;
        }
        for (Match match : previousMatches) {
            if (match.player2Id() == null && match.player1Id() != null) {
                byes.add(match.player1Id());
            }
        }
        return byes;
    }

    private Map<UUID, Double> computeOmwPercentages(
            List<TournamentRegistration> registrations,
            Map<UUID, Set<UUID>> opponentsMap) {
        Map<UUID, Double> pointsByUser = new HashMap<>();
        for (TournamentRegistration reg : registrations) {
            pointsByUser.put(reg.userId(),
                    reg.tournamentPoints() != null ? reg.tournamentPoints().doubleValue() : 0.0);
        }
        Map<UUID, Double> omw = new HashMap<>();
        for (TournamentRegistration reg : registrations) {
            Set<UUID> opps = opponentsMap.getOrDefault(reg.userId(), Set.of());
            if (opps.isEmpty()) {
                omw.put(reg.userId(), 0.33);
            } else {
                double avgPts = opps.stream()
                        .mapToDouble(oppId -> pointsByUser.getOrDefault(oppId, 0.0))
                        .average()
                        .orElse(0.0);
                omw.put(reg.userId(), Math.max(0.33, Math.min(1.0, avgPts / 12.0)));
            }
        }
        return omw;
    }
}

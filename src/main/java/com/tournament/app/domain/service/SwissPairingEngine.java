package com.tournament.app.domain.service;

import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchStatus;
import com.tournament.app.domain.model.RegistrationStatus;
import com.tournament.app.domain.model.TournamentRegistration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Pure domain service for Swiss pairings, BYE assignment, rematch prevention,
 * and OMW% (Opponent Match Win Percentage) calculation.
 * Contains zero framework dependencies (Constitution Principle I).
 */
public class SwissPairingEngine {

    private static final BigDecimal MINIMUM_OMW_FLOOR = new BigDecimal("0.3333");

    public List<Match> generatePairings(
        UUID roundId,
        List<TournamentRegistration> registrations,
        List<Match> previousMatches
    ) {
        List<TournamentRegistration> activePlayers = registrations.stream()
            .filter(r -> r.status() == RegistrationStatus.REGISTERED || r.status() == RegistrationStatus.CHECKED_IN)
            .sorted(Comparator
                .comparingInt(TournamentRegistration::matchPoints).reversed()
                .thenComparing(TournamentRegistration::omwPercentage, Comparator.reverseOrder())
                .thenComparing(TournamentRegistration::registeredAt))
            .toList();

        if (activePlayers.size() < 2) {
            throw new IllegalArgumentException("At least 2 active players are required to generate pairings");
        }

        Map<UUID, Set<UUID>> opponentsByPlayer = new HashMap<>();
        Set<UUID> playersWithPreviousBye = new HashSet<>();
        for (TournamentRegistration reg : activePlayers) {
            opponentsByPlayer.put(reg.userId(), new HashSet<>());
        }
        if (previousMatches != null) {
            for (Match m : previousMatches) {
                if (m.isBye()) {
                    playersWithPreviousBye.add(m.player1Id());
                } else {
                    opponentsByPlayer.computeIfAbsent(m.player1Id(), k -> new HashSet<>()).add(m.player2Id());
                    opponentsByPlayer.computeIfAbsent(m.player2Id(), k -> new HashSet<>()).add(m.player1Id());
                }
            }
        }

        List<TournamentRegistration> pool = new ArrayList<>(activePlayers);
        TournamentRegistration byePlayer = null;

        if (pool.size() % 2 != 0) {
            // Assign BYE to the lowest-ranked player who has not received a BYE yet
            for (int i = pool.size() - 1; i >= 0; i--) {
                TournamentRegistration candidate = pool.get(i);
                if (!playersWithPreviousBye.contains(candidate.userId())) {
                    byePlayer = pool.remove(i);
                    break;
                }
            }
            if (byePlayer == null) {
                byePlayer = pool.remove(pool.size() - 1);
            }
        }

        List<Match> generated = new ArrayList<>();
        int tableNumber = 1;
        boolean[] paired = new boolean[pool.size()];

        for (int i = 0; i < pool.size(); i++) {
            if (paired[i]) {
                continue;
            }
            TournamentRegistration p1 = pool.get(i);
            paired[i] = true;
            int chosenIdx = -1;

            for (int j = i + 1; j < pool.size(); j++) {
                if (!paired[j]) {
                    TournamentRegistration candidate = pool.get(j);
                    Set<UUID> prevOpps = opponentsByPlayer.getOrDefault(p1.userId(), Set.of());
                    if (!prevOpps.contains(candidate.userId())) {
                        chosenIdx = j;
                        break;
                    }
                }
            }
            if (chosenIdx == -1) {
                for (int j = i + 1; j < pool.size(); j++) {
                    if (!paired[j]) {
                        chosenIdx = j;
                        break;
                    }
                }
            }

            paired[chosenIdx] = true;
            TournamentRegistration p2 = pool.get(chosenIdx);
            generated.add(new Match(
                UUID.randomUUID(),
                roundId,
                tableNumber++,
                p1.userId(),
                p2.userId(),
                0,
                0,
                0,
                MatchStatus.PENDING,
                null
            ));
        }

        if (byePlayer != null) {
            generated.add(new Match(
                UUID.randomUUID(),
                roundId,
                tableNumber,
                byePlayer.userId(),
                null,
                2,
                0,
                0,
                MatchStatus.CONFIRMED,
                byePlayer.userId()
            ));
        }

        return List.copyOf(generated);
    }

    public BigDecimal calculateOmwPercentage(UUID playerId, Map<UUID, Integer> matchPointsByPlayer, List<Match> matches, int roundsPlayed) {
        if (roundsPlayed <= 0 || matches == null || matches.isEmpty()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        List<UUID> opponents = new ArrayList<>();
        for (Match match : matches) {
            if (match.isBye()) {
                continue;
            }
            if (playerId.equals(match.player1Id())) {
                opponents.add(match.player2Id());
            } else if (playerId.equals(match.player2Id())) {
                opponents.add(match.player1Id());
            }
        }
        if (opponents.isEmpty()) {
            return MINIMUM_OMW_FLOOR;
        }
        BigDecimal maxPossiblePoints = BigDecimal.valueOf(roundsPlayed * 3L);
        BigDecimal sumWinRates = BigDecimal.ZERO;
        for (UUID oppId : opponents) {
            int oppPoints = matchPointsByPlayer.getOrDefault(oppId, 0);
            BigDecimal winRate = BigDecimal.valueOf(oppPoints).divide(maxPossiblePoints, 4, RoundingMode.HALF_UP);
            if (winRate.compareTo(MINIMUM_OMW_FLOOR) < 0) {
                winRate = MINIMUM_OMW_FLOOR;
            }
            sumWinRates = sumWinRates.add(winRate);
        }
        return sumWinRates.divide(BigDecimal.valueOf(opponents.size()), 4, RoundingMode.HALF_UP);
    }
}

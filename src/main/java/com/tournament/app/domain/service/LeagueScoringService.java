package com.tournament.app.domain.service;

import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.TournamentRegistration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeagueScoringService {

    private final FormulaEvaluationEngine formulaEvaluationEngine;

    public LeagueScoringService(FormulaEvaluationEngine formulaEvaluationEngine) {
        this.formulaEvaluationEngine = formulaEvaluationEngine;
    }

    public record ParticipantScoreInput(
            UUID userId,
            int placement,
            int wins,
            int losses,
            int draws,
            int kills
    ) {
    }

    public BigDecimal calculateMatchPoints(ParticipantScoreInput input, String matchPointsFormula) {
        String expr = (matchPointsFormula == null || matchPointsFormula.isBlank())
                ? "(wins * 3) + (draws * 1)"
                : matchPointsFormula;
        return formulaEvaluationEngine.evaluate(expr, Map.of(
                "win", input.wins() > 0 || input.placement() == 1 ? 1.0 : 0.0,
                "wins", (double) input.wins(),
                "loss", input.losses() > 0 ? 1.0 : 0.0,
                "losses", (double) input.losses(),
                "draw", input.draws() > 0 ? 1.0 : 0.0,
                "draws", (double) input.draws(),
                "placement", (double) input.placement(),
                "kills", (double) input.kills()
        ));
    }

    public Map<UUID, TournamentRegistration> recalculateMatchdayAndLeaguePoints(
            List<TournamentRegistration> registrations,
            List<MatchParticipant> allTournamentParticipants,
            String matchdayLeagueFormula) {

        Map<UUID, BigDecimal> totalTournamentPts = new HashMap<>();
        Map<UUID, Integer> totalWins = new HashMap<>();
        Map<UUID, Integer> matchesPlayed = new HashMap<>();

        for (MatchParticipant mp : allTournamentParticipants) {
            totalTournamentPts.merge(mp.userId(), mp.matchPointsEarned(), BigDecimal::add);
            int winFlag = (mp.wins() > mp.losses() || mp.placement() == 1) ? 1 : 0;
            totalWins.merge(mp.userId(), winFlag, Integer::sum);
            matchesPlayed.merge(mp.userId(), 1, Integer::sum);
        }

        List<TournamentRegistration> sortedByTournamentPoints = registrations.stream()
                .sorted(Comparator.comparing((TournamentRegistration r) ->
                        totalTournamentPts.getOrDefault(r.userId(), BigDecimal.ZERO)).reversed())
                .toList();

        String leagueExpr = (matchdayLeagueFormula == null || matchdayLeagueFormula.isBlank())
                ? "tournament_points + attended"
                : matchdayLeagueFormula;

        Map<UUID, TournamentRegistration> updatedMap = new HashMap<>();
        int rank = 1;
        for (TournamentRegistration reg : sortedByTournamentPoints) {
            BigDecimal tourneyPts = totalTournamentPts.getOrDefault(reg.userId(), BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
            int wins = totalWins.getOrDefault(reg.userId(), 0);
            int played = matchesPlayed.getOrDefault(reg.userId(), 0);

            BigDecimal leagueMatchdayPts = formulaEvaluationEngine.evaluate(leagueExpr, Map.of(
                    "tournament_points", tourneyPts.doubleValue(),
                    "rank", (double) rank,
                    "match_wins", (double) wins,
                    "matches_played", (double) played,
                    "attended", 1.0
            ));

            updatedMap.put(reg.userId(), reg.withUpdatedPoints(tourneyPts, leagueMatchdayPts));
            rank++;
        }

        return updatedMap;
    }
}

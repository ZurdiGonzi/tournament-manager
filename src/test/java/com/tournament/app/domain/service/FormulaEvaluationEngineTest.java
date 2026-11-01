package com.tournament.app.domain.service;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.service.LeagueScoringService.ParticipantScoreInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormulaEvaluationEngineTest {

    private final FormulaEvaluationEngine engine = new FormulaEvaluationEngine();
    private final LeagueScoringService scoringService = new LeagueScoringService(engine);

    @Test
    void shouldEvaluateCustomStoreOwnerMatchAndLeagueFormulas() {
        String matchFormula = "(wins * 3) + (draws * 1) + (kills * 0.5)";
        BigDecimal matchPts = scoringService.calculateMatchPoints(
                new ParticipantScoreInput(UUID.randomUUID(), 1, 1, 0, 0, 3),
                matchFormula
        );
        assertThat(matchPts).isEqualByComparingTo("4.50");

        String commanderPlacementFormula = "max(0, 5 - placement) + (kills * 1)";
        BigDecimal podPts = scoringService.calculateMatchPoints(
                new ParticipantScoreInput(UUID.randomUUID(), 2, 0, 1, 0, 1),
                commanderPlacementFormula
        );
        assertThat(podPts).isEqualByComparingTo("4.00");
    }

    @Test
    void shouldHandleDivisionByZeroSafelyWithoutCrashingMatchday() {
        BigDecimal result = engine.evaluate("tournament_points / matches_played", Map.of(
                "tournament_points", 9.0,
                "matches_played", 0.0
        ));
        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldRejectInvalidFormulaSyntaxWhenStoreOwnerSavesIt() {
        assertThatThrownBy(() -> engine.validateSyntax("(wins * 3 + "))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void shouldRecalculateMatchdayAndLeaguePointsProgressively() {
        UUID tournamentId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        DeckSnapshot dummySnap = new DeckSnapshot(
                "d1", "https://moxfield.com/decks/d1", "Deck", "COMMANDER",
                BigDecimal.TEN, "EUR", Instant.now(), List.of(), List.of(), List.of()
        );

        TournamentRegistration reg1 = new TournamentRegistration(
                UUID.randomUUID(), tournamentId, user1, UUID.randomUUID(), "REGISTERED",
                BigDecimal.TEN, "Kefka, Court Mage", UUID.randomUUID(), List.of("U", "B", "R"),
                BigDecimal.ZERO, BigDecimal.ZERO, dummySnap, Instant.now()
        );
        TournamentRegistration reg2 = new TournamentRegistration(
                UUID.randomUUID(), tournamentId, user2, UUID.randomUUID(), "REGISTERED",
                BigDecimal.TEN, "Magda, Brazen Outlaw", UUID.randomUUID(), List.of("R"),
                BigDecimal.ZERO, BigDecimal.ZERO, dummySnap, Instant.now()
        );

        List<MatchParticipant> participants = List.of(
                new MatchParticipant(UUID.randomUUID(), UUID.randomUUID(), reg1.id(), user1, reg1.deckId(),
                        reg1.commanderOracleId(), "Kefka, Court Mage", 1, 1, 0, 0, 2, new BigDecimal("4.00")),
                new MatchParticipant(UUID.randomUUID(), UUID.randomUUID(), reg2.id(), user2, reg2.deckId(),
                        reg2.commanderOracleId(), "Magda, Brazen Outlaw", 2, 0, 1, 0, 1, new BigDecimal("1.50"))
        );

        String leagueMatchdayFormula = "(tournament_points * 2) + (attended * 1) + max(0, 3 - rank)";
        Map<UUID, TournamentRegistration> updated = scoringService.recalculateMatchdayAndLeaguePoints(
                List.of(reg1, reg2), participants, leagueMatchdayFormula
        );

        // user1: rank 1, tournament_points = 4.00 -> (4*2) + 1 + (3-1) = 11.00
        assertThat(updated.get(user1).tournamentPoints()).isEqualByComparingTo("4.00");
        assertThat(updated.get(user1).calculatedLeagueMatchdayPoints()).isEqualByComparingTo("11.00");

        // user2: rank 2, tournament_points = 1.50 -> (1.5*2) + 1 + (3-2) = 5.00
        assertThat(updated.get(user2).tournamentPoints()).isEqualByComparingTo("1.50");
        assertThat(updated.get(user2).calculatedLeagueMatchdayPoints()).isEqualByComparingTo("5.00");
    }
}

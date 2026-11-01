package com.tournament.app.domain.service;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.DomainExceptions.InvalidTournamentStateException;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.service.SwissPairingEngine.GeneratedRoundResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwissPairingEngineTest {

    private final SwissPairingEngine pairingEngine = new SwissPairingEngine(new FormulaEvaluationEngine());

    @Test
    void shouldGeneratePairingsUsingStoreOwnerFormulaAndAssignByeForOddPlayers() {
        Tournament tournament = new Tournament(
                UUID.randomUUID(), UUID.randomUUID(), null, null, null,
                "Standard FNM", Instant.now(), new BigDecimal("5.00"), "Packs",
                "STANDARD", new BigDecimal("150.00"), "REGISTRATION_OPEN",
                0, 3, null, null, Instant.now()
        );

        List<TournamentRegistration> regs = List.of(
                createReg(tournament.id(), "Player1", new BigDecimal("6.00")),
                createReg(tournament.id(), "Player2", new BigDecimal("6.00")),
                createReg(tournament.id(), "Player3", new BigDecimal("0.00"))
        );

        GeneratedRoundResult result = pairingEngine.generateNextRound(
                tournament,
                regs,
                List.of(),
                Map.of(),
                "(current_points * 10) + (omw_percentage * 5)"
        );

        assertThat(result.updatedTournament().currentRound()).isEqualTo(1);
        assertThat(result.matches()).hasSize(2);

        Match byeMatch = result.matches().getLast();
        assertThat(byeMatch.player2Id()).isNull();
        assertThat(byeMatch.player1Id()).isEqualTo(regs.get(2).userId());
        assertThat(byeMatch.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void shouldEnforceMaxRoundsConfiguredByOrganizer() {
        Tournament tournamentAtMax = new Tournament(
                UUID.randomUUID(), UUID.randomUUID(), null, null, null,
                "Commander Jornada 1", Instant.now(), new BigDecimal("5.00"), "Prize",
                "COMMANDER", new BigDecimal("100.00"), "IN_PROGRESS",
                4, 4, null, null, Instant.now()
        );

        List<TournamentRegistration> regs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            regs.add(createReg(tournamentAtMax.id(), "Cmd" + i, BigDecimal.ZERO));
        }

        assertThatThrownBy(() -> pairingEngine.generateNextRound(
                tournamentAtMax, regs, List.of(), Map.of(), "current_points"
        )).isInstanceOf(InvalidTournamentStateException.class)
                .hasMessageContaining("maxRounds (4)");
    }

    private TournamentRegistration createReg(UUID tournamentId, String cmd, BigDecimal pts) {
        DeckSnapshot snap = new DeckSnapshot(
                "id", "https://moxfield.com/decks/id", cmd, "STANDARD",
                BigDecimal.TEN, "EUR", Instant.now(), List.of(), List.of(), List.of()
        );
        return new TournamentRegistration(
                UUID.randomUUID(), tournamentId, UUID.randomUUID(), UUID.randomUUID(),
                "REGISTERED", BigDecimal.TEN, cmd, UUID.randomUUID(), List.of("R"),
                pts, BigDecimal.ZERO, snap, Instant.now()
        );
    }
}

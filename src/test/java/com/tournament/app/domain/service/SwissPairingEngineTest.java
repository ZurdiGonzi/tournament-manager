package com.tournament.app.domain.service;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchStatus;
import com.tournament.app.domain.model.RegistrationStatus;
import com.tournament.app.domain.model.TournamentRegistration;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SwissPairingEngineTest {

    private final SwissPairingEngine engine = new SwissPairingEngine();

    @Test
    void shouldAssignByeToLowestRankedPlayerWithoutPreviousByeAndAvoidRematches() {
        UUID tournamentId = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();

        TournamentRegistration r1 = reg(tournamentId, p1, 6, "0.6667", 1);
        TournamentRegistration r2 = reg(tournamentId, p2, 3, "0.5000", 2);
        TournamentRegistration r3 = reg(tournamentId, p3, 0, "0.3333", 3);

        // Suppose p3 already had a BYE in round 1, and p1 played p2 in round 1
        Match prevBye = new Match(UUID.randomUUID(), UUID.randomUUID(), 2, p3, null, 2, 0, 0, MatchStatus.CONFIRMED, p3);
        Match prevMatch = new Match(UUID.randomUUID(), UUID.randomUUID(), 1, p1, p2, 2, 0, 0, MatchStatus.CONFIRMED, p1);

        List<Match> round2Matches = engine.generatePairings(
            UUID.randomUUID(),
            List.of(r1, r2, r3),
            List.of(prevMatch, prevBye)
        );

        assertThat(round2Matches).hasSize(2);
        Match byeMatch = round2Matches.stream().filter(Match::isBye).findFirst().orElseThrow();
        // Since p3 already had a BYE, the next lowest player (p2) receives the BYE
        assertThat(byeMatch.player1Id()).isEqualTo(p2);
        assertThat(byeMatch.status()).isEqualTo(MatchStatus.CONFIRMED);

        Match pairedMatch = round2Matches.stream().filter(m -> !m.isBye()).findFirst().orElseThrow();
        assertThat(List.of(pairedMatch.player1Id(), pairedMatch.player2Id())).containsExactlyInAnyOrder(p1, p3);
    }

    @Test
    void shouldApplyOneThirdFloorWhenCalculatingOmwPercentage() {
        UUID p1 = UUID.randomUUID();
        UUID opp1 = UUID.randomUUID();
        UUID opp2 = UUID.randomUUID();

        Match m1 = new Match(UUID.randomUUID(), UUID.randomUUID(), 1, p1, opp1, 2, 0, 0, MatchStatus.CONFIRMED, p1);
        Match m2 = new Match(UUID.randomUUID(), UUID.randomUUID(), 1, p1, opp2, 2, 1, 0, MatchStatus.CONFIRMED, p1);

        // 2 rounds played (max 6 points): opp1 has 0 pts (floored to 0.3333), opp2 has 6 pts (1.0000)
        BigDecimal omw = engine.calculateOmwPercentage(
            p1,
            Map.of(p1, 6, opp1, 0, opp2, 6),
            List.of(m1, m2),
            2
        );

        assertThat(omw).isEqualByComparingTo(new BigDecimal("0.6667"));
    }

    private static TournamentRegistration reg(UUID tournamentId, UUID userId, int points, String omw, int secOffset) {
        DeckSnapshot snapshot = new DeckSnapshot(
            UUID.randomUUID(),
            "Test Deck",
            "mox123",
            MagicFormat.COMMANDER,
            new BigDecimal("50.00"),
            Instant.now(),
            List.of()
        );
        return new TournamentRegistration(
            UUID.randomUUID(),
            tournamentId,
            userId,
            snapshot.deckId(),
            RegistrationStatus.REGISTERED,
            new BigDecimal("50.00"),
            points,
            new BigDecimal(omw),
            null,
            snapshot,
            Instant.parse("2026-11-01T10:00:00Z").plusSeconds(secOffset)
        );
    }
}

package com.tournament.app.domain.service;

import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.service.DeckAnalyticsService.CommanderMatchupMetric;
import com.tournament.app.domain.service.DeckAnalyticsService.MatchdayAnalyticsSummary;
import com.tournament.app.domain.service.DeckAnalyticsService.PersonalDeckAnalyticsSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeckAnalyticsServiceTest {

    private final DeckAnalyticsService analyticsService = new DeckAnalyticsService();

    @Test
    void shouldCalculateMatchdayStatsCommanderMatchupsAndPersonalDeckStats() {
        UUID tournamentId = UUID.randomUUID();
        UUID userKefka = UUID.randomUUID();
        UUID userMagda = UUID.randomUUID();
        UUID deckKefkaId = UUID.randomUUID();
        UUID deckMagdaId = UUID.randomUUID();
        UUID solRingOracleId = UUID.randomUUID();

        DeckSnapshot.CardEntry solRing = new DeckSnapshot.CardEntry(
                UUID.randomUUID(), solRingOracleId, "Sol Ring", 1, "{1}",
                new BigDecimal("1.0"), "Artifact", List.of(), List.of(), "uncommon", new BigDecimal("1.50")
        );
        DeckSnapshot.CardEntry counterspell = new DeckSnapshot.CardEntry(
                UUID.randomUUID(), UUID.randomUUID(), "Counterspell", 1, "{U}{U}",
                new BigDecimal("2.0"), "Instant", List.of("U"), List.of("U"), "uncommon", new BigDecimal("1.20")
        );

        DeckSnapshot kefkaSnap = new DeckSnapshot(
                "kefka1", "https://moxfield.com/decks/kefka1", "Kefka Control", "COMMANDER",
                new BigDecimal("85.00"), "EUR", Instant.now(),
                List.of(), List.of(solRing, counterspell), List.of()
        );
        DeckSnapshot magdaSnap = new DeckSnapshot(
                "magda1", "https://moxfield.com/decks/magda1", "Magda Dwarves", "COMMANDER",
                new BigDecimal("70.00"), "EUR", Instant.now(),
                List.of(), List.of(solRing), List.of()
        );

        TournamentRegistration regKefka = new TournamentRegistration(
                UUID.randomUUID(), tournamentId, userKefka, deckKefkaId, "REGISTERED",
                new BigDecimal("85.00"), "Kefka, Court Mage", UUID.randomUUID(), List.of("U", "B", "R"),
                new BigDecimal("9.00"), new BigDecimal("12.00"), kefkaSnap, Instant.now()
        );
        TournamentRegistration regMagda = new TournamentRegistration(
                UUID.randomUUID(), tournamentId, userMagda, deckMagdaId, "REGISTERED",
                new BigDecimal("70.00"), "Magda, Brazen Outlaw", UUID.randomUUID(), List.of("R"),
                new BigDecimal("6.00"), new BigDecimal("8.00"), magdaSnap, Instant.now()
        );

        // 5 matches between Magda and Kefka where Kefka wins 3 (60%) and Magda wins 2 (40%)
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UUID matchId = UUID.randomUUID();
            boolean kefkaWins = i < 3;
            MatchParticipant pKefka = new MatchParticipant(
                    UUID.randomUUID(), matchId, regKefka.id(), userKefka, deckKefkaId,
                    regKefka.commanderOracleId(), "Kefka, Court Mage",
                    kefkaWins ? 1 : 2, kefkaWins ? 1 : 0, kefkaWins ? 0 : 1, 0, 2,
                    kefkaWins ? new BigDecimal("3.00") : BigDecimal.ZERO
            );
            MatchParticipant pMagda = new MatchParticipant(
                    UUID.randomUUID(), matchId, regMagda.id(), userMagda, deckMagdaId,
                    regMagda.commanderOracleId(), "Magda, Brazen Outlaw",
                    kefkaWins ? 2 : 1, kefkaWins ? 0 : 1, kefkaWins ? 1 : 0, 0, 1,
                    kefkaWins ? BigDecimal.ZERO : new BigDecimal("3.00")
            );
            matches.add(new Match(
                    matchId, UUID.randomUUID(), 1, userKefka, userMagda,
                    pKefka.wins(), pMagda.wins(), 0, "CONFIRMED",
                    kefkaWins ? userKefka : userMagda, List.of(pKefka, pMagda)
            ));
        }

        MatchdayAnalyticsSummary summary = analyticsService.computeMatchdayOrLeagueAnalytics(
                tournamentId, "MATCHDAY", List.of(regKefka, regMagda), matches, false
        );

        assertThat(summary.mostPlayedCards()).isNotEmpty();
        assertThat(summary.mostPlayedCards().getFirst().cardName()).isEqualTo("Sol Ring");
        assertThat(summary.mostPlayedCards().getFirst().presencePercentage()).isEqualTo(100.0);

        assertThat(summary.mostPlayedColors().get("R")).isEqualTo(100.0);
        assertThat(summary.mostPlayedColors().get("U")).isEqualTo(50.0);

        assertThat(summary.winningestCommanders()).hasSize(2);
        assertThat(summary.winningestCommanders().getFirst().commanderName()).isEqualTo("Kefka, Court Mage");
        assertThat(summary.winningestCommanders().getFirst().winRatePercentage()).isEqualTo(60.0);

        assertThat(summary.commanderMatchups()).hasSize(1);
        CommanderMatchupMetric matchup = summary.commanderMatchups().getFirst();
        assertThat(matchup.dominantCommander()).isEqualTo("Kefka, Court Mage");
        assertThat(matchup.dominantWinPercentage()).isEqualTo(60.0);
        assertThat(matchup.summaryLabel()).isEqualTo("Kefka vs Magda: gana Kefka 60%");

        Deck kefkaDeck = new Deck(
                deckKefkaId, userKefka, "Kefka Control", "https://moxfield.com/decks/kefka1",
                "kefka1", "COMMANDER", regKefka.commanderOracleId(), "Kefka, Court Mage",
                List.of("U", "B", "R"), new BigDecimal("85.00"), Instant.now(), Instant.now()
        );
        PersonalDeckAnalyticsSummary personal = analyticsService.computePersonalDeckAnalytics(
                kefkaDeck, "MATCHDAY", List.of(regKefka, regMagda), matches
        );

        assertThat(personal.matchesPlayed()).isEqualTo(5);
        assertThat(personal.matchesWon()).isEqualTo(3);
        assertThat(personal.winRatePercentage()).isEqualTo(60.0);
        assertThat(personal.rivalMatchups()).hasSize(1);
        assertThat(personal.rivalMatchups().getFirst().summary()).isEqualTo("Kefka vs Magda: gana Kefka 60%");
    }
}

package com.tournament.app.infrastructure.adapter.persistence;

import com.tournament.app.application.usecase.DeckRegistrationScryfallSynchronizer;
import com.tournament.app.domain.model.BoardSection;
import com.tournament.app.domain.model.CurrencyCode;
import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueDetailSummary;
import com.tournament.app.domain.model.LeagueLeaderboardEntry;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.model.LeagueStatus;
import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchStatus;
import com.tournament.app.domain.model.MetagameAnalyticsSummary;
import com.tournament.app.domain.model.RegistrationDeckCard;
import com.tournament.app.domain.model.RegistrationStatus;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.model.RoundStatus;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.model.StoreDetailSummary;
import com.tournament.app.domain.model.StoreMember;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentDetailSummary;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.model.TournamentRestrictions;
import com.tournament.app.domain.model.TournamentStatus;
import com.tournament.app.domain.model.TournamentTemplate;
import com.tournament.app.domain.model.User;
import com.tournament.app.domain.model.UserProfileSummary;
import com.tournament.app.domain.model.UserRole;
import com.tournament.app.domain.repository.DeckRepository;
import com.tournament.app.domain.repository.LeagueRepository;
import com.tournament.app.domain.repository.MatchRepository;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.domain.repository.RoundRepository;
import com.tournament.app.domain.repository.ScryfallCardRepository;
import com.tournament.app.domain.repository.StoreRepository;
import com.tournament.app.domain.repository.TournamentRepository;
import com.tournament.app.domain.repository.TournamentTemplateRepository;
import com.tournament.app.domain.repository.UserRepository;
import com.tournament.app.domain.service.ScryfallCardGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryExtractionIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TournamentTemplateRepository templateRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ScryfallCardRepository scryfallCardRepository;

    @Autowired
    private DeckRegistrationScryfallSynchronizer deckSynchronizer;

    @MockBean
    private ScryfallCardGateway scryfallCardGateway;

    @Test
    void shouldExtractAllTwelveTablesAndTwoViewsAndSynchronizeWithScryfallWhenRegisteringDeck() {
        Instant now = Instant.parse("2026-11-01T16:00:00Z");

        // 1. Users (Owner + 2 Players)
        User owner = userRepository.save(new User(UUID.randomUUID(), "gonzalo_owner", "$2a$10$hashed", UserRole.ADMIN, now));
        User player1 = userRepository.save(new User(UUID.randomUUID(), "alice_planeswalker", "$2a$10$hashed1", UserRole.PLAYER, now));
        User player2 = userRepository.save(new User(UUID.randomUUID(), "bob_mage", "$2a$10$hashed2", UserRole.PLAYER, now));

        assertThat(userRepository.findAll())
            .extracting(User::passwordHash)
            .containsOnlyNulls();

        // 2 & 3. Store & Store Members
        Store store = storeRepository.save(new Store(UUID.randomUUID(), "Magic Corner Madrid", owner.id(), CurrencyCode.EUR, now));
        StoreMember m1 = storeRepository.addMember(store.id(), player1.id());
        StoreMember m2 = storeRepository.addMember(store.id(), player2.id());
        assertThat(m1.username()).isEqualTo("alice_planeswalker");
        assertThat(m2.username()).isEqualTo("bob_mage");

        // 4. Decks
        Deck deck1 = deckRepository.save(new Deck(
            UUID.randomUUID(),
            player1.id(),
            "Talrand Sky Summoner Budget",
            "https://www.moxfield.com/decks/Talrand123",
            "Talrand123",
            now,
            now
        ));
        assertThat(deckRepository.findByUserId(player1.id())).hasSize(1);

        // 5 & 6. League & League Standings
        League league = leagueRepository.save(new League(
            UUID.randomUUID(),
            store.id(),
            "Liga Otoño Commander 2026",
            MagicFormat.COMMANDER,
            "Tapete exclusivo + sobres",
            now,
            now.plusSeconds(86400 * 30),
            LeagueStatus.IN_PROGRESS,
            now
        ));
        leagueRepository.saveStanding(new LeagueStanding(league.id(), player1.id(), 9, 1, 3, 0, 0, now));
        leagueRepository.saveStanding(new LeagueStanding(league.id(), player2.id(), 3, 1, 1, 0, 2, now));

        // 7. Tournament Template
        TournamentTemplate template = templateRepository.save(new TournamentTemplate(
            UUID.randomUUID(),
            store.id(),
            league.id(),
            "Viernes Commander",
            "0 0 18 * * FRI",
            new BigDecimal("5.00"),
            "Sobres por victoria",
            MagicFormat.COMMANDER,
            new BigDecimal("100.00"),
            32,
            3,
            true,
            null,
            now
        ));
        assertThat(templateRepository.findByStoreIdAndActiveTrue(store.id())).hasSize(1);

        // 8. Tournament
        Tournament tournament = tournamentRepository.save(new Tournament(
            UUID.randomUUID(),
            store.id(),
            league.id(),
            template.id(),
            "Viernes Commander #1",
            now.plusSeconds(3600),
            new BigDecimal("5.00"),
            "Sobres por victoria",
            new TournamentRestrictions(MagicFormat.COMMANDER, new BigDecimal("100.00")),
            32,
            TournamentStatus.IN_PROGRESS,
            1,
            3,
            now
        ));

        // 9, 10 & 11. Scryfall sync when registering deck + Tournament Registration + Registration Deck Cards
        UUID solRingId = UUID.randomUUID();
        UUID counterspellId = UUID.randomUUID();
        ScryfallCard solRing = new ScryfallCard(
            solRingId,
            UUID.randomUUID(),
            "Sol Ring",
            "{1}",
            BigDecimal.ONE,
            "Artifact",
            List.of(),
            List.of(),
            "uncommon",
            Map.of("commander", "legal", "standard", "not_legal"),
            new BigDecimal("1.50"),
            new BigDecimal("1.80"),
            "https://cards.scryfall.io/normal/sol_ring.jpg",
            now
        );
        ScryfallCard counterspell = new ScryfallCard(
            counterspellId,
            UUID.randomUUID(),
            "Counterspell",
            "{U}{U}",
            new BigDecimal("2.00"),
            "Instant",
            List.of("U"),
            List.of("U"),
            "common",
            Map.of("commander", "legal", "standard", "not_legal"),
            new BigDecimal("1.20"),
            new BigDecimal("1.40"),
            "https://cards.scryfall.io/normal/counterspell.jpg",
            now
        );

        when(scryfallCardGateway.fetchCardsByNames(anyCollection()))
            .thenReturn(List.of(solRing, counterspell));

        UUID registrationId = UUID.randomUUID();
        DeckRegistrationScryfallSynchronizer.SynchronizedDeckRegistrationResult syncResult =
            deckSynchronizer.synchronizeAndValidateDeck(
                registrationId,
                deck1,
                List.of(
                    new DeckRegistrationScryfallSynchronizer.RawDeckCardLine(null, "Sol Ring", BoardSection.MAINBOARD, 1),
                    new DeckRegistrationScryfallSynchronizer.RawDeckCardLine(null, "Counterspell", BoardSection.MAINBOARD, 1)
                ),
                tournament.restrictions(),
                store.currency()
            );

        assertThat(syncResult.validationResult().valid()).isTrue();
        assertThat(syncResult.validationResult().calculatedDeckPrice()).isEqualByComparingTo("2.70");
        verify(scryfallCardGateway, times(1)).fetchCardsByNames(anyCollection());

        // Second sync for the same cards must hit local DB cache and make 0 additional calls to Scryfall
        deckSynchronizer.synchronizeAndValidateDeck(
            UUID.randomUUID(),
            deck1,
            List.of(new DeckRegistrationScryfallSynchronizer.RawDeckCardLine(null, "Sol Ring", BoardSection.MAINBOARD, 1)),
            tournament.restrictions(),
            store.currency()
        );
        verify(scryfallCardGateway, times(1)).fetchCardsByNames(anyCollection());

        TournamentRegistration reg1 = registrationRepository.save(
            new TournamentRegistration(
                registrationId,
                tournament.id(),
                player1.id(),
                deck1.id(),
                RegistrationStatus.CHECKED_IN,
                syncResult.validationResult().calculatedDeckPrice(),
                9,
                new BigDecimal("0.6667"),
                1,
                syncResult.deckSnapshot(),
                now
            ),
            syncResult.registrationDeckCards()
        );
        assertThat(reg1.deckSnapshot().cards()).hasSize(2);
        List<RegistrationDeckCard> savedDeckCards = registrationRepository.findDeckCardsByRegistrationId(registrationId);
        assertThat(savedDeckCards).hasSize(2);

        // 12. Rounds & Matches
        UUID roundId = UUID.randomUUID();
        Match match1 = new Match(
            UUID.randomUUID(),
            roundId,
            1,
            player1.id(),
            player2.id(),
            2,
            0,
            0,
            MatchStatus.CONFIRMED,
            player1.id()
        );
        Round round1 = roundRepository.save(
            new Round(roundId, tournament.id(), 1, RoundStatus.COMPLETED, now, now.plusSeconds(1800)),
            List.of(match1)
        );
        assertThat(round1.roundNumber()).isEqualTo(1);
        assertThat(matchRepository.findByRoundIdOrderByTableNumberAsc(roundId)).hasSize(1);

        // Verify Aggregated Summaries & SQL Views (v_league_leaderboard & v_scryfall_card_stats)
        UserProfileSummary profile = userRepository.findUserProfileSummaryById(player1.id()).orElseThrow();
        assertThat(profile.registeredDeckCount()).isEqualTo(1);
        assertThat(profile.memberStores()).hasSize(1);
        assertThat(profile.leagues()).hasSize(1);

        StoreDetailSummary storeDetail = storeRepository.findStoreDetailById(store.id()).orElseThrow();
        assertThat(storeDetail.totalMembers()).isEqualTo(2);
        assertThat(storeDetail.totalTournaments()).isEqualTo(1);

        LeagueDetailSummary leagueDetail = leagueRepository.findLeagueDetailById(league.id()).orElseThrow();
        assertThat(leagueDetail.tournaments()).hasSize(1);
        List<LeagueLeaderboardEntry> leaderboard = leagueDetail.leaderboard();
        assertThat(leaderboard).hasSize(2);
        assertThat(leaderboard.get(0).username()).isEqualTo("alice_planeswalker");
        assertThat(leaderboard.get(0).currentRank()).isEqualTo(1L);

        TournamentDetailSummary tournamentDetail = tournamentRepository.findTournamentDetailById(tournament.id()).orElseThrow();
        assertThat(tournamentDetail.registeredPlayersCount()).isEqualTo(1);
        assertThat(tournamentDetail.rounds()).hasSize(1);

        MetagameAnalyticsSummary analytics = scryfallCardRepository.computeMetagameAnalytics(
            store.id(),
            league.id(),
            tournament.id(),
            MagicFormat.COMMANDER
        );
        assertThat(analytics.totalDecksAnalyzed()).isEqualTo(1);
        assertThat(analytics.averageDeckPrice()).isEqualByComparingTo("2.70");
        assertThat(analytics.mostPlayedCards()).hasSize(2);
    }
}

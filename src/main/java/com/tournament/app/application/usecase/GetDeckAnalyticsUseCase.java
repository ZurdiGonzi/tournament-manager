package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AnalyticsDtos.CommanderMatchupStat;
import com.tournament.app.application.dto.AnalyticsDtos.CommanderWinStat;
import com.tournament.app.application.dto.AnalyticsDtos.MatchdayDeckAnalyticsResponse;
import com.tournament.app.application.dto.AnalyticsDtos.MostPlayedCardStat;
import com.tournament.app.application.dto.AnalyticsDtos.PersonalDeckAnalyticsResponse;
import com.tournament.app.application.dto.AnalyticsDtos.RivalMatchupStat;
import com.tournament.app.application.dto.AnalyticsDtos.ScryfallDeckCompositionStat;
import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.DomainExceptions.ResourceNotFoundException;
import com.tournament.app.domain.model.DomainExceptions.UnauthorizedStoreActionException;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.repository.DeckRepository;
import com.tournament.app.domain.repository.MatchRepository;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.domain.service.DeckAnalyticsService;
import com.tournament.app.domain.service.DeckAnalyticsService.MatchdayAnalyticsSummary;
import com.tournament.app.domain.service.DeckAnalyticsService.PersonalDeckAnalyticsSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetDeckAnalyticsUseCase {

    private final RegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final DeckRepository deckRepository;
    private final DeckAnalyticsService deckAnalyticsService;

    public GetDeckAnalyticsUseCase(
            RegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            DeckRepository deckRepository,
            DeckAnalyticsService deckAnalyticsService) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.deckRepository = deckRepository;
        this.deckAnalyticsService = deckAnalyticsService;
    }

    @Transactional(readOnly = true)
    public MatchdayDeckAnalyticsResponse getMatchdayAnalytics(UUID tournamentId, boolean includeBasicLands) {
        List<TournamentRegistration> regs = registrationRepository.findByTournamentId(tournamentId);
        List<Match> matches = matchRepository.findMatchesByTournamentId(tournamentId);
        MatchdayAnalyticsSummary summary = deckAnalyticsService.computeMatchdayOrLeagueAnalytics(
                tournamentId, "MATCHDAY", regs, matches, includeBasicLands
        );
        return toMatchdayResponse(summary);
    }

    @Transactional(readOnly = true)
    public MatchdayDeckAnalyticsResponse getLeagueAnalytics(UUID leagueId, boolean includeBasicLands) {
        List<TournamentRegistration> regs = registrationRepository.findByLeagueId(leagueId);
        List<Match> matches = matchRepository.findMatchesByLeagueId(leagueId);
        MatchdayAnalyticsSummary summary = deckAnalyticsService.computeMatchdayOrLeagueAnalytics(
                leagueId, "LEAGUE_ACCUMULATED", regs, matches, includeBasicLands
        );
        return toMatchdayResponse(summary);
    }

    @Transactional(readOnly = true)
    public PersonalDeckAnalyticsResponse getPersonalDeckAnalytics(
            UUID currentUserId,
            UUID deckId,
            UUID tournamentId,
            UUID leagueId) {

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + deckId));
        if (!deck.userId().equals(currentUserId)) {
            throw new UnauthorizedStoreActionException("You can only view 'Your Deck' statistics for your own decks");
        }

        String scope;
        List<TournamentRegistration> scopeRegistrations;
        List<Match> scopeMatches;

        if (tournamentId != null) {
            scope = "MATCHDAY";
            scopeRegistrations = registrationRepository.findByTournamentId(tournamentId);
            scopeMatches = matchRepository.findMatchesByTournamentId(tournamentId);
        } else if (leagueId != null) {
            scope = "LEAGUE_ACCUMULATED";
            scopeRegistrations = registrationRepository.findByLeagueId(leagueId);
            scopeMatches = matchRepository.findMatchesByLeagueId(leagueId);
        } else {
            scope = "HISTORICAL_ALL";
            scopeRegistrations = registrationRepository.findByUserIdAndDeckId(currentUserId, deckId);
            scopeMatches = matchRepository.findMatchesByDeckId(deckId);
        }

        PersonalDeckAnalyticsSummary summary = deckAnalyticsService.computePersonalDeckAnalytics(
                deck, scope, scopeRegistrations, scopeMatches
        );

        return new PersonalDeckAnalyticsResponse(
                summary.deckId(),
                summary.deckName(),
                summary.commanderName(),
                summary.colorIdentity(),
                summary.scope(),
                summary.matchesPlayed(),
                summary.matchesWon(),
                summary.winRatePercentage(),
                summary.averageMatchPoints(),
                summary.averageMatchdayLeaguePoints(),
                summary.rivalMatchups().stream()
                        .map(r -> new RivalMatchupStat(
                                r.rivalCommanderName(),
                                r.matchesPlayed(),
                                r.winsAgainstRival(),
                                r.winRatePercentage(),
                                r.summary()
                        ))
                        .toList(),
                new ScryfallDeckCompositionStat(
                        summary.scryfallComposition().averageCmc(),
                        summary.scryfallComposition().matchdayAverageCmcComparison(),
                        summary.scryfallComposition().colorBreakdown(),
                        summary.scryfallComposition().typeBreakdown(),
                        summary.scryfallComposition().totalPrice()
                )
        );
    }

    private MatchdayDeckAnalyticsResponse toMatchdayResponse(MatchdayAnalyticsSummary summary) {
        return new MatchdayDeckAnalyticsResponse(
                summary.scopeId(),
                summary.scopeType(),
                summary.totalDecks(),
                summary.mostPlayedCards().stream()
                        .map(c -> new MostPlayedCardStat(
                                c.oracleId(),
                                c.cardName(),
                                c.typeLine(),
                                c.decksCount(),
                                c.presencePercentage(),
                                c.totalCopies()
                        ))
                        .toList(),
                summary.mostPlayedColors(),
                summary.winningestCommanders().stream()
                        .map(w -> new CommanderWinStat(
                                w.commanderOracleId(),
                                w.commanderName(),
                                w.colorIdentity(),
                                w.matchesPlayed(),
                                w.matchesWon(),
                                w.winRatePercentage(),
                                w.averagePointsEarned()
                        ))
                        .toList(),
                summary.commanderMatchups().stream()
                        .map(m -> new CommanderMatchupStat(
                                m.commanderA(),
                                m.commanderB(),
                                m.matchesTogether(),
                                m.commanderAWins(),
                                m.commanderBWins(),
                                m.dominantCommander(),
                                m.dominantWinPercentage(),
                                m.summaryLabel()
                        ))
                        .toList()
        );
    }
}

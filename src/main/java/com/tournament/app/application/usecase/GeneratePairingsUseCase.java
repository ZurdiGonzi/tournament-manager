package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AdminTournamentDtos.MatchPairingDto;
import com.tournament.app.application.dto.AdminTournamentDtos.ParticipantPairingDto;
import com.tournament.app.application.dto.AdminTournamentDtos.RoundPairingsResponse;
import com.tournament.app.domain.model.DomainExceptions.ResourceNotFoundException;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.model.ScoringFormula;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.repository.LeagueRepository;
import com.tournament.app.domain.repository.MatchRepository;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.domain.repository.ScoringFormulaRepository;
import com.tournament.app.domain.repository.TournamentRepository;
import com.tournament.app.domain.service.SwissPairingEngine;
import com.tournament.app.domain.service.SwissPairingEngine.GeneratedRoundResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GeneratePairingsUseCase {

    private final ManageScoringFormulaUseCase manageScoringFormulaUseCase;
    private final TournamentRepository tournamentRepository;
    private final RegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final LeagueRepository leagueRepository;
    private final ScoringFormulaRepository scoringFormulaRepository;
    private final SwissPairingEngine swissPairingEngine;

    public GeneratePairingsUseCase(
            ManageScoringFormulaUseCase manageScoringFormulaUseCase,
            TournamentRepository tournamentRepository,
            RegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            LeagueRepository leagueRepository,
            ScoringFormulaRepository scoringFormulaRepository,
            SwissPairingEngine swissPairingEngine) {
        this.manageScoringFormulaUseCase = manageScoringFormulaUseCase;
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.leagueRepository = leagueRepository;
        this.scoringFormulaRepository = scoringFormulaRepository;
        this.swissPairingEngine = swissPairingEngine;
    }

    @Transactional
    public RoundPairingsResponse execute(UUID currentUserId, UUID tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        manageScoringFormulaUseCase.verifyStoreOwnership(tournament.storeId(), currentUserId);

        List<TournamentRegistration> registrations = registrationRepository.findByTournamentId(tournamentId);
        List<Match> previousMatches = matchRepository.findMatchesByTournamentId(tournamentId);

        Map<UUID, BigDecimal> leaguePointsByUser = new HashMap<>();
        if (tournament.leagueId() != null) {
            for (LeagueStanding ls : leagueRepository.findStandingsByLeagueId(tournament.leagueId())) {
                leaguePointsByUser.put(ls.userId(), ls.totalLeaguePoints());
            }
        }

        String pairingExpression = null;
        if (tournament.pairingFormulaId() != null) {
            pairingExpression = scoringFormulaRepository.findById(tournament.pairingFormulaId())
                    .map(ScoringFormula::expression)
                    .orElse(null);
        }

        GeneratedRoundResult generated = swissPairingEngine.generateNextRound(
                tournament,
                registrations,
                previousMatches,
                leaguePointsByUser,
                pairingExpression
        );

        tournamentRepository.save(generated.updatedTournament());
        Round savedRound = matchRepository.saveRound(generated.round());
        List<Match> savedMatches = matchRepository.saveMatches(generated.matches());

        List<MatchPairingDto> matchDtos = savedMatches.stream()
                .map(m -> new MatchPairingDto(
                        m.id(),
                        m.tableNumber(),
                        m.status(),
                        m.participants().stream()
                                .map(mp -> new ParticipantPairingDto(
                                        mp.userId(),
                                        mp.deckId(),
                                        mp.commanderName(),
                                        mp.placement(),
                                        mp.wins(),
                                        mp.losses(),
                                        mp.draws(),
                                        mp.kills(),
                                        mp.matchPointsEarned()
                                ))
                                .toList()
                ))
                .toList();

        return new RoundPairingsResponse(
                tournamentId,
                savedRound.id(),
                savedRound.roundNumber(),
                generated.updatedTournament().maxRounds(),
                matchDtos
        );
    }
}

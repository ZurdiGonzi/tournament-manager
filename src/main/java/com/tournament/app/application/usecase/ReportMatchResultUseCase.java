package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AdminTournamentDtos.MatchPairingDto;
import com.tournament.app.application.dto.AdminTournamentDtos.ParticipantPairingDto;
import com.tournament.app.application.dto.AdminTournamentDtos.ParticipantResultInput;
import com.tournament.app.application.dto.AdminTournamentDtos.RecordMatchResultRequest;
import com.tournament.app.domain.model.DomainExceptions.ResourceNotFoundException;
import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.model.ScoringFormula;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.repository.LeagueRepository;
import com.tournament.app.domain.repository.MatchRepository;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.domain.repository.ScoringFormulaRepository;
import com.tournament.app.domain.repository.TournamentRepository;
import com.tournament.app.domain.service.LeagueScoringService;
import com.tournament.app.domain.service.LeagueScoringService.ParticipantScoreInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportMatchResultUseCase {

    private final ManageScoringFormulaUseCase manageScoringFormulaUseCase;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final RegistrationRepository registrationRepository;
    private final LeagueRepository leagueRepository;
    private final ScoringFormulaRepository scoringFormulaRepository;
    private final LeagueScoringService leagueScoringService;

    public ReportMatchResultUseCase(
            ManageScoringFormulaUseCase manageScoringFormulaUseCase,
            MatchRepository matchRepository,
            TournamentRepository tournamentRepository,
            RegistrationRepository registrationRepository,
            LeagueRepository leagueRepository,
            ScoringFormulaRepository scoringFormulaRepository,
            LeagueScoringService leagueScoringService) {
        this.manageScoringFormulaUseCase = manageScoringFormulaUseCase;
        this.matchRepository = matchRepository;
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
        this.leagueRepository = leagueRepository;
        this.scoringFormulaRepository = scoringFormulaRepository;
        this.leagueScoringService = leagueScoringService;
    }

    @Transactional
    public MatchPairingDto execute(UUID currentUserId, UUID matchId, RecordMatchResultRequest request) {
        Match match = matchRepository.findMatchById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));

        Round round = matchRepository.findRoundById(match.roundId())
                .orElseThrow(() -> new ResourceNotFoundException("Round not found: " + match.roundId()));

        Tournament tournament = tournamentRepository.findById(round.tournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + round.tournamentId()));

        // Clarified in Session 2026-11-01: Only the store owner/organizer can enter match results
        manageScoringFormulaUseCase.verifyStoreOwnership(tournament.storeId(), currentUserId);

        String matchFormulaExpr = null;
        if (tournament.matchPointsFormulaId() != null) {
            matchFormulaExpr = scoringFormulaRepository.findById(tournament.matchPointsFormulaId())
                    .map(ScoringFormula::expression)
                    .orElse(null);
        }

        Map<UUID, ParticipantResultInput> resultsByUser = request.participantResults().stream()
                .collect(Collectors.toMap(ParticipantResultInput::userId, Function.identity(), (a, b) -> a));

        List<MatchParticipant> updatedParticipants = new ArrayList<>();
        UUID winnerId = null;
        int p1Wins = 0;
        int p2Wins = 0;
        int draws = 0;

        for (MatchParticipant existing : match.participants()) {
            ParticipantResultInput input = resultsByUser.get(existing.userId());
            if (input == null) {
                updatedParticipants.add(existing);
                continue;
            }
            BigDecimal pointsEarned = leagueScoringService.calculateMatchPoints(
                    new ParticipantScoreInput(
                            input.userId(),
                            input.placement(),
                            input.wins(),
                            input.losses(),
                            input.draws(),
                            input.kills()
                    ),
                    matchFormulaExpr
            );
            if (input.placement() == 1 && (input.wins() > 0 || match.participants().size() > 2)) {
                winnerId = input.userId();
            }
            if (existing.userId().equals(match.player1Id())) {
                p1Wins = input.wins();
                draws = input.draws();
            } else if (existing.userId().equals(match.player2Id())) {
                p2Wins = input.wins();
            }
            updatedParticipants.add(new MatchParticipant(
                    existing.id(),
                    existing.matchId(),
                    existing.registrationId(),
                    existing.userId(),
                    existing.deckId(),
                    existing.commanderOracleId(),
                    existing.commanderName(),
                    input.placement(),
                    input.wins(),
                    input.losses(),
                    input.draws(),
                    input.kills(),
                    pointsEarned
            ));
        }

        Match savedMatch = matchRepository.saveMatch(new Match(
                match.id(),
                match.roundId(),
                match.tableNumber(),
                match.player1Id(),
                match.player2Id(),
                p1Wins,
                p2Wins,
                draws,
                "CONFIRMED",
                winnerId,
                updatedParticipants
        ));

        recalculateTournamentAndLeagueStandings(tournament);

        return new MatchPairingDto(
                savedMatch.id(),
                savedMatch.tableNumber(),
                savedMatch.status(),
                savedMatch.participants().stream()
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
        );
    }

    private void recalculateTournamentAndLeagueStandings(Tournament tournament) {
        List<TournamentRegistration> registrations = registrationRepository.findByTournamentId(tournament.id());
        List<MatchParticipant> confirmedParticipants = matchRepository.findParticipantsByTournamentId(tournament.id());

        League league = tournament.leagueId() != null
                ? leagueRepository.findById(tournament.leagueId()).orElse(null)
                : null;

        String matchdayFormulaExpr = null;
        if (league != null && league.matchdayPointsFormulaId() != null) {
            matchdayFormulaExpr = scoringFormulaRepository.findById(league.matchdayPointsFormulaId())
                    .map(ScoringFormula::expression)
                    .orElse(null);
        }

        Map<UUID, TournamentRegistration> updatedRegsMap = leagueScoringService.recalculateMatchdayAndLeaguePoints(
                registrations,
                confirmedParticipants,
                matchdayFormulaExpr
        );

        registrationRepository.saveAll(new ArrayList<>(updatedRegsMap.values()));

        if (league != null) {
            rebuildCumulativeLeagueStandings(league.id());
        }
    }

    private void rebuildCumulativeLeagueStandings(UUID leagueId) {
        List<TournamentRegistration> allLeagueRegs = registrationRepository.findByLeagueId(leagueId);
        List<Match> allLeagueMatches = matchRepository.findMatchesByLeagueId(leagueId);

        Map<UUID, BigDecimal> totalLeaguePts = new HashMap<>();
        Map<UUID, Set<UUID>> matchdaysPlayedByUser = new HashMap<>();
        for (TournamentRegistration reg : allLeagueRegs) {
            totalLeaguePts.merge(reg.userId(), reg.calculatedLeagueMatchdayPoints(), BigDecimal::add);
            matchdaysPlayedByUser.computeIfAbsent(reg.userId(), k -> new HashSet<>()).add(reg.tournamentId());
        }

        Map<UUID, Integer> totalMatchWins = new HashMap<>();
        Map<UUID, Integer> totalMatchesPlayed = new HashMap<>();
        for (Match m : allLeagueMatches) {
            if (!"CONFIRMED".equalsIgnoreCase(m.status())) {
                continue;
            }
            for (MatchParticipant mp : m.participants()) {
                totalMatchesPlayed.merge(mp.userId(), 1, Integer::sum);
                if (mp.placement() == 1 && (mp.wins() > 0 || m.participants().size() > 2)) {
                    totalMatchWins.merge(mp.userId(), 1, Integer::sum);
                }
            }
        }

        List<LeagueStanding> standings = totalLeaguePts.entrySet().stream()
                .map(entry -> {
                    UUID userId = entry.getKey();
                    return new LeagueStanding(
                            UUID.randomUUID(),
                            leagueId,
                            userId,
                            entry.getValue(),
                            matchdaysPlayedByUser.getOrDefault(userId, Set.of()).size(),
                            totalMatchWins.getOrDefault(userId, 0),
                            totalMatchesPlayed.getOrDefault(userId, 0),
                            Instant.now()
                    );
                })
                .sorted(Comparator.comparing(LeagueStanding::totalLeaguePoints).reversed())
                .toList();

        leagueRepository.saveStandings(standings);
    }
}

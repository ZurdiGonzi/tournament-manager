package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AdminTournamentDtos.CreateTournamentRequest;
import com.tournament.app.application.dto.AdminTournamentDtos.MatchdayStandingItem;
import com.tournament.app.application.dto.AdminTournamentDtos.TournamentResponse;
import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.repository.LeagueRepository;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.domain.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CreateTournamentUseCase {

    private final ManageScoringFormulaUseCase manageScoringFormulaUseCase;
    private final TournamentRepository tournamentRepository;
    private final LeagueRepository leagueRepository;
    private final RegistrationRepository registrationRepository;

    public CreateTournamentUseCase(
            ManageScoringFormulaUseCase manageScoringFormulaUseCase,
            TournamentRepository tournamentRepository,
            LeagueRepository leagueRepository,
            RegistrationRepository registrationRepository) {
        this.manageScoringFormulaUseCase = manageScoringFormulaUseCase;
        this.tournamentRepository = tournamentRepository;
        this.leagueRepository = leagueRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public TournamentResponse execute(UUID currentUserId, UUID storeId, CreateTournamentRequest request) {
        manageScoringFormulaUseCase.verifyStoreOwnership(storeId, currentUserId);

        UUID matchPointsFormulaId = request.matchPointsFormulaId();
        UUID pairingFormulaId = request.pairingFormulaId();

        if (request.leagueId() != null) {
            League league = leagueRepository.findById(request.leagueId()).orElse(null);
            if (league != null) {
                if (matchPointsFormulaId == null) {
                    matchPointsFormulaId = league.matchPointsFormulaId();
                }
                if (pairingFormulaId == null) {
                    pairingFormulaId = league.pairingFormulaId();
                }
            }
        }

        Tournament saved = tournamentRepository.save(new Tournament(
                UUID.randomUUID(),
                storeId,
                null,
                request.leagueId(),
                request.matchdayNumber(),
                request.name().trim(),
                request.scheduledAt(),
                request.entryFee(),
                request.prizeDescription().trim(),
                request.format().trim().toUpperCase(),
                request.maxDeckBudget(),
                "REGISTRATION_OPEN",
                0,
                request.maxRounds(),
                matchPointsFormulaId,
                pairingFormulaId,
                Instant.now()
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MatchdayStandingItem> getTournamentStandings(UUID tournamentId) {
        List<TournamentRegistration> sorted = registrationRepository.findByTournamentId(tournamentId).stream()
                .sorted(Comparator.comparing(TournamentRegistration::tournamentPoints).reversed())
                .toList();
        List<MatchdayStandingItem> result = new ArrayList<>();
        int rank = 1;
        for (TournamentRegistration reg : sorted) {
            result.add(new MatchdayStandingItem(
                    rank++,
                    reg.userId(),
                    reg.commanderName(),
                    reg.tournamentPoints(),
                    reg.calculatedLeagueMatchdayPoints()
            ));
        }
        return result;
    }

    public static TournamentResponse toResponse(Tournament t) {
        return new TournamentResponse(
                t.id(),
                t.storeId(),
                t.leagueId(),
                t.matchdayNumber(),
                t.name(),
                t.scheduledAt(),
                t.entryFee(),
                t.prizeDescription(),
                t.format(),
                t.maxDeckBudget(),
                t.status(),
                t.currentRound(),
                t.maxRounds(),
                t.matchPointsFormulaId(),
                t.pairingFormulaId()
        );
    }
}

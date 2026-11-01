package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AdminTournamentDtos.CreateLeagueRequest;
import com.tournament.app.application.dto.AdminTournamentDtos.LeagueResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.LeagueStandingItem;
import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.repository.LeagueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CreateLeagueUseCase {

    private final ManageScoringFormulaUseCase manageScoringFormulaUseCase;
    private final LeagueRepository leagueRepository;

    public CreateLeagueUseCase(
            ManageScoringFormulaUseCase manageScoringFormulaUseCase,
            LeagueRepository leagueRepository) {
        this.manageScoringFormulaUseCase = manageScoringFormulaUseCase;
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public LeagueResponse execute(UUID currentUserId, UUID storeId, CreateLeagueRequest request) {
        manageScoringFormulaUseCase.verifyStoreOwnership(storeId, currentUserId);
        League saved = leagueRepository.save(new League(
                UUID.randomUUID(),
                storeId,
                request.name().trim(),
                request.format().trim().toUpperCase(),
                request.maxDeckBudget(),
                request.matchPointsFormulaId(),
                request.matchdayPointsFormulaId(),
                request.pairingFormulaId(),
                "ACTIVE",
                Instant.now()
        ));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LeagueStandingItem> getLeagueStandings(UUID leagueId) {
        List<LeagueStanding> standings = leagueRepository.findStandingsByLeagueId(leagueId);
        List<LeagueStandingItem> items = new ArrayList<>();
        int rank = 1;
        for (LeagueStanding s : standings) {
            items.add(new LeagueStandingItem(
                    rank++,
                    s.userId(),
                    s.totalLeaguePoints(),
                    s.matchdaysPlayed(),
                    s.totalMatchWins(),
                    s.totalMatchesPlayed()
            ));
        }
        return items;
    }

    private LeagueResponse toResponse(League l) {
        return new LeagueResponse(
                l.id(),
                l.storeId(),
                l.name(),
                l.format(),
                l.maxDeckBudget(),
                l.matchPointsFormulaId(),
                l.matchdayPointsFormulaId(),
                l.pairingFormulaId(),
                l.status(),
                l.createdAt()
        );
    }
}

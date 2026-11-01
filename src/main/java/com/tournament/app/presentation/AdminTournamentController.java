package com.tournament.app.presentation;

import com.tournament.app.application.dto.AdminTournamentDtos.CreateLeagueRequest;
import com.tournament.app.application.dto.AdminTournamentDtos.CreateStoreRequest;
import com.tournament.app.application.dto.AdminTournamentDtos.CreateTournamentRequest;
import com.tournament.app.application.dto.AdminTournamentDtos.LeagueResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.LeagueStandingItem;
import com.tournament.app.application.dto.AdminTournamentDtos.MatchPairingDto;
import com.tournament.app.application.dto.AdminTournamentDtos.MatchdayStandingItem;
import com.tournament.app.application.dto.AdminTournamentDtos.RecordMatchResultRequest;
import com.tournament.app.application.dto.AdminTournamentDtos.RoundPairingsResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.ScoringFormulaResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.StoreResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.TournamentResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.UpsertScoringFormulaRequest;
import com.tournament.app.application.usecase.CreateLeagueUseCase;
import com.tournament.app.application.usecase.CreateTournamentUseCase;
import com.tournament.app.application.usecase.GeneratePairingsUseCase;
import com.tournament.app.application.usecase.ManageScoringFormulaUseCase;
import com.tournament.app.application.usecase.ReportMatchResultUseCase;
import com.tournament.app.infrastructure.security.JwtAuthenticationFilter.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AdminTournamentController {

    private final ManageScoringFormulaUseCase manageScoringFormulaUseCase;
    private final CreateLeagueUseCase createLeagueUseCase;
    private final CreateTournamentUseCase createTournamentUseCase;
    private final GeneratePairingsUseCase generatePairingsUseCase;
    private final ReportMatchResultUseCase reportMatchResultUseCase;

    public AdminTournamentController(
            ManageScoringFormulaUseCase manageScoringFormulaUseCase,
            CreateLeagueUseCase createLeagueUseCase,
            CreateTournamentUseCase createTournamentUseCase,
            GeneratePairingsUseCase generatePairingsUseCase,
            ReportMatchResultUseCase reportMatchResultUseCase) {
        this.manageScoringFormulaUseCase = manageScoringFormulaUseCase;
        this.createLeagueUseCase = createLeagueUseCase;
        this.createTournamentUseCase = createTournamentUseCase;
        this.generatePairingsUseCase = generatePairingsUseCase;
        this.reportMatchResultUseCase = reportMatchResultUseCase;
    }

    @PostMapping("/api/v1/admin/stores")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponse createStore(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateStoreRequest request) {
        return manageScoringFormulaUseCase.createStore(principal.userId(), request);
    }

    @PostMapping("/api/v1/admin/stores/{storeId}/formulas")
    @ResponseStatus(HttpStatus.CREATED)
    public ScoringFormulaResponse createScoringFormula(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID storeId,
            @Valid @RequestBody UpsertScoringFormulaRequest request) {
        return manageScoringFormulaUseCase.createFormula(principal.userId(), storeId, request);
    }

    @PutMapping("/api/v1/admin/stores/{storeId}/formulas/{formulaId}")
    public ScoringFormulaResponse updateScoringFormula(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID storeId,
            @PathVariable UUID formulaId,
            @Valid @RequestBody UpsertScoringFormulaRequest request) {
        return manageScoringFormulaUseCase.updateFormula(principal.userId(), storeId, formulaId, request);
    }

    @GetMapping("/api/v1/admin/stores/{storeId}/formulas")
    public List<ScoringFormulaResponse> listStoreFormulas(@PathVariable UUID storeId) {
        return manageScoringFormulaUseCase.listStoreFormulas(storeId);
    }

    @PostMapping("/api/v1/admin/stores/{storeId}/leagues")
    @ResponseStatus(HttpStatus.CREATED)
    public LeagueResponse createLeague(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID storeId,
            @Valid @RequestBody CreateLeagueRequest request) {
        return createLeagueUseCase.execute(principal.userId(), storeId, request);
    }

    @PostMapping("/api/v1/admin/stores/{storeId}/tournaments")
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse createTournament(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID storeId,
            @Valid @RequestBody CreateTournamentRequest request) {
        return createTournamentUseCase.execute(principal.userId(), storeId, request);
    }

    @PostMapping("/api/v1/admin/tournaments/{tournamentId}/rounds/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public RoundPairingsResponse generateNextRoundPairings(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID tournamentId) {
        return generatePairingsUseCase.execute(principal.userId(), tournamentId);
    }

    @PostMapping("/api/v1/admin/matches/{matchId}/result")
    public MatchPairingDto recordMatchResult(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID matchId,
            @Valid @RequestBody RecordMatchResultRequest request) {
        return reportMatchResultUseCase.execute(principal.userId(), matchId, request);
    }

    @GetMapping("/api/v1/public/tournaments/{tournamentId}/standings")
    public List<MatchdayStandingItem> getTournamentStandings(@PathVariable UUID tournamentId) {
        return createTournamentUseCase.getTournamentStandings(tournamentId);
    }

    @GetMapping("/api/v1/public/leagues/{leagueId}/standings")
    public List<LeagueStandingItem> getLeagueStandings(@PathVariable UUID leagueId) {
        return createLeagueUseCase.getLeagueStandings(leagueId);
    }
}

package com.tournament.app.presentation;

import com.tournament.app.application.dto.AnalyticsDtos.MatchdayDeckAnalyticsResponse;
import com.tournament.app.application.usecase.GetDeckAnalyticsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/analytics")
public class AnalyticsController {

    private final GetDeckAnalyticsUseCase getDeckAnalyticsUseCase;

    public AnalyticsController(GetDeckAnalyticsUseCase getDeckAnalyticsUseCase) {
        this.getDeckAnalyticsUseCase = getDeckAnalyticsUseCase;
    }

    @GetMapping("/tournaments/{tournamentId}")
    public MatchdayDeckAnalyticsResponse getMatchdayAnalytics(
            @PathVariable UUID tournamentId,
            @RequestParam(defaultValue = "false") boolean includeBasicLands) {
        return getDeckAnalyticsUseCase.getMatchdayAnalytics(tournamentId, includeBasicLands);
    }

    @GetMapping("/leagues/{leagueId}")
    public MatchdayDeckAnalyticsResponse getLeagueAnalytics(
            @PathVariable UUID leagueId,
            @RequestParam(defaultValue = "false") boolean includeBasicLands) {
        return getDeckAnalyticsUseCase.getLeagueAnalytics(leagueId, includeBasicLands);
    }
}

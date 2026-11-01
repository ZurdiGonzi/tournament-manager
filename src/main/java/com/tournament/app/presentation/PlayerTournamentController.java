package com.tournament.app.presentation;

import com.tournament.app.application.dto.AnalyticsDtos.PersonalDeckAnalyticsResponse;
import com.tournament.app.application.dto.PlayerDeckDtos.ProfileDeckResponse;
import com.tournament.app.application.dto.PlayerDeckDtos.SaveProfileDeckRequest;
import com.tournament.app.application.dto.PlayerDeckDtos.TournamentRegistrationRequest;
import com.tournament.app.application.dto.PlayerDeckDtos.TournamentRegistrationResponse;
import com.tournament.app.application.usecase.GetDeckAnalyticsUseCase;
import com.tournament.app.application.usecase.RegisterPlayerUseCase;
import com.tournament.app.application.usecase.SaveProfileDeckUseCase;
import com.tournament.app.infrastructure.security.JwtAuthenticationFilter.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/player")
public class PlayerTournamentController {

    private final SaveProfileDeckUseCase saveProfileDeckUseCase;
    private final RegisterPlayerUseCase registerPlayerUseCase;
    private final GetDeckAnalyticsUseCase getDeckAnalyticsUseCase;

    public PlayerTournamentController(
            SaveProfileDeckUseCase saveProfileDeckUseCase,
            RegisterPlayerUseCase registerPlayerUseCase,
            GetDeckAnalyticsUseCase getDeckAnalyticsUseCase) {
        this.saveProfileDeckUseCase = saveProfileDeckUseCase;
        this.registerPlayerUseCase = registerPlayerUseCase;
        this.getDeckAnalyticsUseCase = getDeckAnalyticsUseCase;
    }

    @PostMapping("/decks")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDeckResponse saveDeckToProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SaveProfileDeckRequest request) {
        return saveProfileDeckUseCase.saveToProfile(principal.userId(), request);
    }

    @GetMapping("/decks")
    public List<ProfileDeckResponse> listProfileDecks(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return saveProfileDeckUseCase.listProfileDecks(principal.userId());
    }

    @DeleteMapping("/decks/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfileDeck(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID deckId) {
        saveProfileDeckUseCase.deleteProfileDeck(principal.userId(), deckId);
    }

    @PostMapping("/tournaments/{tournamentId}/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentRegistrationResponse registerForTournament(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID tournamentId,
            @Valid @RequestBody TournamentRegistrationRequest request) {
        return registerPlayerUseCase.execute(principal.userId(), tournamentId, request);
    }

    @GetMapping("/decks/{deckId}/analytics")
    public PersonalDeckAnalyticsResponse getYourDeckAnalytics(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable UUID deckId,
            @RequestParam(required = false) UUID tournamentId,
            @RequestParam(required = false) UUID leagueId) {
        return getDeckAnalyticsUseCase.getPersonalDeckAnalytics(
                principal.userId(), deckId, tournamentId, leagueId
        );
    }
}

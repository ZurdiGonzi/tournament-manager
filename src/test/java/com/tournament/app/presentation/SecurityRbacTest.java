package com.tournament.app.presentation;

import com.tournament.app.application.dto.AnalyticsDtos.MatchdayDeckAnalyticsResponse;
import com.tournament.app.application.usecase.AuthenticateUserUseCase;
import com.tournament.app.application.usecase.CreateLeagueUseCase;
import com.tournament.app.application.usecase.CreateTournamentUseCase;
import com.tournament.app.application.usecase.GeneratePairingsUseCase;
import com.tournament.app.application.usecase.GetDeckAnalyticsUseCase;
import com.tournament.app.application.usecase.ManageScoringFormulaUseCase;
import com.tournament.app.application.usecase.RegisterPlayerUseCase;
import com.tournament.app.application.usecase.RegisterUserUseCase;
import com.tournament.app.application.usecase.ReportMatchResultUseCase;
import com.tournament.app.application.usecase.SaveProfileDeckUseCase;
import com.tournament.app.domain.repository.UserRepository;
import com.tournament.app.infrastructure.security.CustomUserDetailsService;
import com.tournament.app.infrastructure.security.JwtAuthenticationFilter;
import com.tournament.app.infrastructure.security.JwtTokenProvider;
import com.tournament.app.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        PlayerTournamentController.class,
        AdminTournamentController.class,
        AnalyticsController.class
})
@Import({
        SecurityConfig.class,
        JwtTokenProvider.class,
        JwtAuthenticationFilter.class,
        CustomUserDetailsService.class,
        GlobalExceptionHandler.class
})
class SecurityRbacTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RegisterUserUseCase registerUserUseCase;
    @MockBean
    private AuthenticateUserUseCase authenticateUserUseCase;
    @MockBean
    private SaveProfileDeckUseCase saveProfileDeckUseCase;
    @MockBean
    private RegisterPlayerUseCase registerPlayerUseCase;
    @MockBean
    private GetDeckAnalyticsUseCase getDeckAnalyticsUseCase;
    @MockBean
    private ManageScoringFormulaUseCase manageScoringFormulaUseCase;
    @MockBean
    private CreateLeagueUseCase createLeagueUseCase;
    @MockBean
    private CreateTournamentUseCase createTournamentUseCase;
    @MockBean
    private GeneratePairingsUseCase generatePairingsUseCase;
    @MockBean
    private ReportMatchResultUseCase reportMatchResultUseCase;

    @Test
    void domainLayerMustRemainCompletelyFreeOfFrameworkImports() throws IOException {
        Path domainRoot = Path.of("src/main/java/com/tournament/app/domain");
        try (Stream<Path> paths = Files.walk(domainRoot)) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
            assertThat(javaFiles).isNotEmpty();
            for (Path file : javaFiles) {
                String content = Files.readString(file);
                assertThat(content)
                        .as("Domain file %s must not import Spring or JPA", file.getFileName())
                        .doesNotContain("org.springframework.")
                        .doesNotContain("jakarta.persistence.");
            }
        }
    }

    @Test
    void publicAnalyticsEndpointShouldAllowAnonymousAccess() throws Exception {
        UUID tournamentId = UUID.randomUUID();
        when(getDeckAnalyticsUseCase.getMatchdayAnalytics(any(), anyBoolean()))
                .thenReturn(new MatchdayDeckAnalyticsResponse(
                        tournamentId, "MATCHDAY", 0, List.of(), Map.of(), List.of(), List.of()
                ));

        mockMvc.perform(get("/api/v1/public/analytics/tournaments/" + tournamentId))
                .andExpect(status().isOk());
    }

    @Test
    void playerEndpointShouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/player/decks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminMatchResultEndpointShouldReturn403WhenCalledByPlayerRole() throws Exception {
        String playerToken = jwtTokenProvider.generateToken(UUID.randomUUID(), "player1", "PLAYER");
        String payload = """
                {
                  "participantResults": [
                    { "userId": "%s", "placement": 1, "wins": 1, "losses": 0, "draws": 0, "kills": 2 }
                  ]
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/admin/matches/" + UUID.randomUUID() + "/result")
                        .header("Authorization", "Bearer " + playerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}

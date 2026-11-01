package com.tournament.app.application.dto.round;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.tournament.app.application.dto.auth.UserSummaryResponse;
import com.tournament.app.domain.model.MatchStatus;
import com.tournament.app.domain.model.RoundStatus;
import com.tournament.app.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoundDtoSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Test
    @DisplayName("MatchPairingResponse debe representar de forma precisa el estado de BYE")
    void shouldHandleByePairing() throws Exception {
        var player1 = new UserSummaryResponse(UUID.randomUUID(), "solitary_player", "p1@mtg.com", Set.of(UserRole.PLAYER), Instant.now());
        var byeMatch = new MatchPairingResponse(
            UUID.randomUUID(),
            1,
            player1,
            null, // No hay oponente en BYE
            true, // isBye = true
            MatchStatus.CONFIRMED,
            2,
            0,
            0
        );

        assertTrue(byeMatch.isBye());
        assertNull(byeMatch.player2());

        String json = mapper.writeValueAsString(byeMatch);
        var deserialized = mapper.readValue(json, MatchPairingResponse.class);

        assertTrue(deserialized.isBye());
        assertNull(deserialized.player2());
        assertEquals("solitary_player", deserialized.player1().username());
    }

    @Test
    @DisplayName("TournamentStandingsResponse debe contener clasificaciones ordenadas y coeficientes de desempate")
    void shouldSerializeStandings() throws Exception {
        var entry1 = new StandingEntryResponse(
            1,
            UUID.randomUUID(),
            "alice",
            9,
            "3-0-0",
            new BigDecimal("66.67"),
            new BigDecimal("75.00")
        );
        var entry2 = new StandingEntryResponse(
            2,
            UUID.randomUUID(),
            "bob",
            6,
            "2-1-0",
            new BigDecimal("55.56"),
            new BigDecimal("60.00")
        );

        var standings = new TournamentStandingsResponse(UUID.randomUUID(), 3, List.of(entry1, entry2));
        String json = mapper.writeValueAsString(standings);
        var deserialized = mapper.readValue(json, TournamentStandingsResponse.class);

        assertEquals(2, deserialized.standings().size());
        assertEquals("alice", deserialized.standings().getFirst().username());
        assertEquals(9, deserialized.standings().getFirst().matchPoints());
    }
}


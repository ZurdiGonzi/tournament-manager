package com.tournament.app.application.dto.league;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LeagueDtoSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Test
    @DisplayName("LeagueDetailResponse debe serializar torneos vinculados y clasificación acumulada")
    void shouldSerializeLeagueDetail() throws Exception {
        var standing1 = new LeagueStandingEntryResponse(1, UUID.randomUUID(), "carlos_mtg", 4, 36);
        var standing2 = new LeagueStandingEntryResponse(2, UUID.randomUUID(), "david_mtg", 4, 30);

        var league = new LeagueDetailResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Liga de Invierno Commander",
            Instant.parse("2026-11-01T00:00:00Z"),
            Instant.parse("2026-12-31T23:59:59Z"),
            List.of(),
            List.of(standing1, standing2)
        );

        String json = mapper.writeValueAsString(league);
        var deserialized = mapper.readValue(json, LeagueDetailResponse.class);

        assertEquals("Liga de Invierno Commander", deserialized.name());
        assertEquals(2, deserialized.standings().size());
        assertEquals("carlos_mtg", deserialized.standings().getFirst().username());
        assertEquals(36, deserialized.standings().getFirst().accumulatedPoints());
    }
}


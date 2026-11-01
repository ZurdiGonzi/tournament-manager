package com.tournament.app.application.dto.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.tournament.app.application.dto.deck.DeckCardDto;
import com.tournament.app.application.dto.deck.DeckSnapshotDto;
import com.tournament.app.domain.model.RegistrationStatus;
import com.tournament.app.domain.model.TournamentFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationDtoSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Test
    @DisplayName("TournamentRegistrationResponse y DeckSnapshotDto deben serializarse y deserializarse fielmente")
    void shouldSerializeAndDeserializeRegistrationResponse() throws Exception {
        var card = new DeckCardDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Counterspell",
            2,
            "{U}{U}",
            new BigDecimal("2.00"),
            "Instant",
            List.of("U"),
            List.of("U"),
            new BigDecimal("1.50"),
            false
        );

        var snapshot = new DeckSnapshotDto(
            UUID.randomUUID(),
            "Mono Blue Control",
            "https://www.moxfield.com/decks/sample123",
            TournamentFormat.COMMANDER,
            new BigDecimal("3.00"),
            Instant.parse("2026-11-01T15:00:00Z"),
            List.of(card)
        );

        var response = new TournamentRegistrationResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "gonzalo_player",
            RegistrationStatus.CONFIRMED,
            Instant.parse("2026-11-01T15:05:00Z"),
            snapshot
        );

        String json = mapper.writeValueAsString(response);
        assertNotNull(json);

        var deserialized = mapper.readValue(json, TournamentRegistrationResponse.class);
        assertEquals(response.id(), deserialized.id());
        assertEquals("gonzalo_player", deserialized.username());
        assertEquals(RegistrationStatus.CONFIRMED, deserialized.status());
        assertEquals(response.deckSnapshot().deckName(), deserialized.deckSnapshot().deckName());
        assertEquals(1, deserialized.deckSnapshot().cards().size());
        assertEquals("Counterspell", deserialized.deckSnapshot().cards().getFirst().name());
    }
}


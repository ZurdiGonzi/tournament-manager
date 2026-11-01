package com.tournament.app.application.dto.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsDtoSerializationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Test
    @DisplayName("MetagameAnalyticsResponse debe serializar reparto de colores y ranking Scryfall")
    void shouldSerializeAnalytics() throws Exception {
        var blue = new ColorBreakdownDto("U", "Blue", new BigDecimal("45.50"));
        var black = new ColorBreakdownDto("B", "Black", new BigDecimal("38.20"));

        var solRing = new CardUsageStatDto(
            UUID.randomUUID(),
            "Sol Ring",
            "{1}",
            "Artifact",
            24,
            new BigDecimal("75.00"),
            new BigDecimal("1.45")
        );

        var analytics = new MetagameAnalyticsResponse(
            "STORE",
            UUID.randomUUID(),
            32,
            new BigDecimal("85.75"),
            List.of(blue, black),
            List.of(solRing)
        );

        String json = mapper.writeValueAsString(analytics);
        var deserialized = mapper.readValue(json, MetagameAnalyticsResponse.class);

        assertEquals("STORE", deserialized.scope());
        assertEquals(32, deserialized.totalDecksAnalyzed());
        assertEquals(2, deserialized.colorDistribution().size());
        assertEquals("Sol Ring", deserialized.topPlayedCards().getFirst().cardName());
        assertEquals(0, new BigDecimal("75.00").compareTo(deserialized.topPlayedCards().getFirst().deckInclusionRate()));
    }
}


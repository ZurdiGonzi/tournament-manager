package com.tournament.app.infrastructure.adapter.scryfall;

import com.tournament.app.domain.model.ScryfallCard;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ScryfallClientTest {

    @Test
    void shouldFetchCardsBatchFromScryfallCollectionEndpointAndNormalizeDoubleFacedAndPrices() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.scryfall.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ScryfallClient client = new ScryfallClient(builder.build(), 0L);

        UUID cardId = UUID.fromString("c1f829b3-1111-2222-3333-444455556666");
        UUID oracleId = UUID.fromString("a1b2c3d4-1111-2222-3333-444455556666");

        String jsonResponse = """
            {
              "object": "list",
              "not_found": [],
              "data": [
                {
                  "id": "c1f829b3-1111-2222-3333-444455556666",
                  "oracle_id": "a1b2c3d4-1111-2222-3333-444455556666",
                  "name": "Sol Ring",
                  "mana_cost": "{1}",
                  "cmc": 1.0,
                  "type_line": "Artifact",
                  "colors": [],
                  "color_identity": [],
                  "rarity": "uncommon",
                  "legalities": {
                    "standard": "not_legal",
                    "commander": "legal"
                  },
                  "prices": {
                    "eur": "1.45",
                    "usd": "1.75"
                  },
                  "image_uris": {
                    "normal": "https://cards.scryfall.io/normal/sol_ring.jpg"
                  }
                }
              ]
            }
            """;

        server.expect(requestTo("https://api.scryfall.com/cards/collection"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<ScryfallCard> cards = client.fetchCardsByIds(List.of(cardId));

        server.verify();
        assertThat(cards).hasSize(1);
        ScryfallCard card = cards.get(0);
        assertThat(card.id()).isEqualTo(cardId);
        assertThat(card.oracleId()).isEqualTo(oracleId);
        assertThat(card.name()).isEqualTo("Sol Ring");
        assertThat(card.priceEur()).isEqualByComparingTo(new BigDecimal("1.45"));
        assertThat(card.priceUsd()).isEqualByComparingTo(new BigDecimal("1.75"));
        assertThat(card.legalities()).containsEntry("commander", "legal");
    }
}

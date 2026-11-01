package com.tournament.app.infrastructure.adapter.scryfall;

import com.tournament.app.domain.model.ScryfallCard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ScryfallClient {

    private static final int SCRYFALL_BATCH_LIMIT = 75;

    private final RestClient restClient;

    public ScryfallClient(@Value("${app.external.scryfall.base-url:https://api.scryfall.com}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<ScryfallCard> fetchCardsByIds(List<UUID> scryfallIds) {
        if (scryfallIds == null || scryfallIds.isEmpty()) {
            return List.of();
        }
        List<ScryfallCard> resolved = new ArrayList<>();
        for (int i = 0; i < scryfallIds.size(); i += SCRYFALL_BATCH_LIMIT) {
            List<UUID> batch = scryfallIds.subList(i, Math.min(i + SCRYFALL_BATCH_LIMIT, scryfallIds.size()));
            List<Map<String, String>> identifiers = batch.stream()
                    .map(id -> Map.of("id", id.toString()))
                    .toList();
            ScryfallCardResponse.ScryfallCollectionBatchResponse response = restClient.post()
                    .uri("/cards/collection")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("identifiers", identifiers))
                    .retrieve()
                    .body(ScryfallCardResponse.ScryfallCollectionBatchResponse.class);
            if (response != null && response.data() != null) {
                response.data().stream().map(ScryfallCardResponse::toDomain).forEach(resolved::add);
            }
        }
        return resolved;
    }
}

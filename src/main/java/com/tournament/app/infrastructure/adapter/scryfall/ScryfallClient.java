package com.tournament.app.infrastructure.adapter.scryfall;

import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.service.ScryfallCardGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * HTTP adapter communicating with Scryfall API (`POST /cards/collection` and `GET /cards/named`)
 * with batching (max 75 identifiers per call) and rate-limiting politeness.
 */
@Component
public class ScryfallClient implements ScryfallCardGateway {

    private static final int MAX_COLLECTION_BATCH_SIZE = 75;

    private final RestClient scryfallRestClient;
    private final long rateLimitMs;

    public ScryfallClient(
        RestClient scryfallRestClient,
        @Value("${scryfall.rate-limit-ms:100}") long rateLimitMs
    ) {
        this.scryfallRestClient = scryfallRestClient;
        this.rateLimitMs = rateLimitMs;
    }

    @Override
    public List<ScryfallCard> fetchCardsByIds(Collection<UUID> scryfallIds) {
        if (scryfallIds == null || scryfallIds.isEmpty()) {
            return List.of();
        }
        List<ScryfallCollectionRequest.CardIdentifier> identifiers = scryfallIds.stream()
            .distinct()
            .map(ScryfallCollectionRequest.CardIdentifier::byId)
            .toList();
        return executeCollectionBatches(identifiers);
    }

    @Override
    public List<ScryfallCard> fetchCardsByNames(Collection<String> cardNames) {
        if (cardNames == null || cardNames.isEmpty()) {
            return List.of();
        }
        List<ScryfallCollectionRequest.CardIdentifier> identifiers = cardNames.stream()
            .filter(n -> n != null && !n.isBlank())
            .map(String::trim)
            .distinct()
            .map(ScryfallCollectionRequest.CardIdentifier::byName)
            .toList();
        return executeCollectionBatches(identifiers);
    }

    @Override
    public Optional<ScryfallCard> fetchCardByExactName(String cardName) {
        if (cardName == null || cardName.isBlank()) {
            return Optional.empty();
        }
        try {
            ScryfallCardPayload payload = scryfallRestClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/cards/named")
                    .queryParam("exact", cardName.trim())
                    .build())
                .retrieve()
                .body(ScryfallCardPayload.class);
            if (payload == null || payload.id() == null) {
                return Optional.empty();
            }
            return Optional.of(payload.toDomain(Instant.now()));
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    private List<ScryfallCard> executeCollectionBatches(List<ScryfallCollectionRequest.CardIdentifier> identifiers) {
        List<ScryfallCard> result = new ArrayList<>();
        Instant now = Instant.now();

        for (int start = 0; start < identifiers.size(); start += MAX_COLLECTION_BATCH_SIZE) {
            if (start > 0 && rateLimitMs > 0) {
                sleepSilently(rateLimitMs);
            }
            int end = Math.min(start + MAX_COLLECTION_BATCH_SIZE, identifiers.size());
            List<ScryfallCollectionRequest.CardIdentifier> batch = identifiers.subList(start, end);

            try {
                ScryfallCollectionResponse response = scryfallRestClient.post()
                    .uri("/cards/collection")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ScryfallCollectionRequest(batch))
                    .retrieve()
                    .body(ScryfallCollectionResponse.class);

                if (response != null && response.data() != null) {
                    for (ScryfallCardPayload payload : response.data()) {
                        if (payload != null && payload.id() != null) {
                            result.add(payload.toDomain(now));
                        }
                    }
                }
            } catch (RestClientException ex) {
                // Controlled degradation (US3 Acceptance Scenario 3 / SC-005)
            }
        }
        return result;
    }

    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}

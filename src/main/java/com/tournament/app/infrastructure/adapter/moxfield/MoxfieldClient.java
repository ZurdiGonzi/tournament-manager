package com.tournament.app.infrastructure.adapter.moxfield;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MoxfieldClient {

    private static final Pattern MOXFIELD_URL_PATTERN =
            Pattern.compile("moxfield\\.com/decks/([A-Za-z0-9_-]+)");

    private final RestClient restClient;

    public MoxfieldClient(@Value("${app.external.moxfield.base-url:https://api2.moxfield.com/v2}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String extractDeckId(String moxfieldUrl) {
        if (moxfieldUrl == null || moxfieldUrl.isBlank()) {
            throw new DomainValidationException("Moxfield URL must not be blank");
        }
        Matcher matcher = MOXFIELD_URL_PATTERN.matcher(moxfieldUrl.trim());
        if (!matcher.find()) {
            throw new DomainValidationException("Invalid Moxfield deck URL: " + moxfieldUrl);
        }
        return matcher.group(1);
    }

    @SuppressWarnings("unchecked")
    public MoxfieldDeckResponse fetchDeck(String moxfieldUrl) {
        String deckId = extractDeckId(moxfieldUrl);
        Map<String, Object> raw = restClient.get()
                .uri("/decks/all/{deckId}", deckId)
                .retrieve()
                .body(Map.class);
        if (raw == null) {
            throw new DomainValidationException("Unable to read Moxfield deck: " + deckId);
        }
        String name = (String) raw.getOrDefault("name", "Moxfield Deck " + deckId);
        String format = ((String) raw.getOrDefault("format", "COMMANDER")).toUpperCase();
        Map<String, Object> boards = (Map<String, Object>) raw.getOrDefault("boards", Map.of());
        List<MoxfieldDeckResponse.MoxfieldCardEntry> commanders = parseBoard(boards, "commanders");
        List<MoxfieldDeckResponse.MoxfieldCardEntry> mainboard = parseBoard(boards, "mainboard");
        List<MoxfieldDeckResponse.MoxfieldCardEntry> sideboard = parseBoard(boards, "sideboard");
        return new MoxfieldDeckResponse(deckId, name, format, commanders, mainboard, sideboard);
    }

    @SuppressWarnings("unchecked")
    private List<MoxfieldDeckResponse.MoxfieldCardEntry> parseBoard(Map<String, Object> boards, String sectionName) {
        Object sectionObj = boards.get(sectionName);
        if (!(sectionObj instanceof Map<?, ?> sectionMap)) {
            return List.of();
        }
        Object cardsObj = sectionMap.get("cards");
        if (!(cardsObj instanceof Map<?, ?> cardsMap)) {
            return List.of();
        }
        List<MoxfieldDeckResponse.MoxfieldCardEntry> result = new ArrayList<>();
        for (Object entryValue : cardsMap.values()) {
            if (entryValue instanceof Map<?, ?> item) {
                int quantity = item.get("quantity") instanceof Number n ? n.intValue() : 1;
                Map<String, Object> card = (Map<String, Object>) item.get("card");
                if (card != null) {
                    String scryfallIdStr = (String) card.get("scryfall_id");
                    String cardName = (String) card.get("name");
                    if (scryfallIdStr != null && cardName != null) {
                        result.add(new MoxfieldDeckResponse.MoxfieldCardEntry(
                                UUID.fromString(scryfallIdStr),
                                cardName,
                                quantity
                        ));
                    }
                }
            }
        }
        return result;
    }
}

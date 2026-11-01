package com.tournament.app.infrastructure.adapter.scryfall;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

public record ScryfallCollectionRequest(
    List<CardIdentifier> identifiers
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CardIdentifier(
        UUID id,
        String name
    ) {
        public static CardIdentifier byId(UUID id) {
            return new CardIdentifier(id, null);
        }

        public static CardIdentifier byName(String name) {
            return new CardIdentifier(null, name);
        }
    }
}

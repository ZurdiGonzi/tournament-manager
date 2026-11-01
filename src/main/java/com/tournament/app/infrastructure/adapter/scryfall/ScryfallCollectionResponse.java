package com.tournament.app.infrastructure.adapter.scryfall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScryfallCollectionResponse(
    @JsonProperty("object") String object,
    @JsonProperty("not_found") List<ScryfallCollectionRequest.CardIdentifier> notFound,
    @JsonProperty("data") List<ScryfallCardPayload> data
) {
}

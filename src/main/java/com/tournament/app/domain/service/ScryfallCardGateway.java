package com.tournament.app.domain.service;

import com.tournament.app.domain.model.ScryfallCard;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port to communicate with Scryfall API when registering/submitting a deck.
 */
public interface ScryfallCardGateway {

    List<ScryfallCard> fetchCardsByIds(Collection<UUID> scryfallIds);

    List<ScryfallCard> fetchCardsByNames(Collection<String> cardNames);

    Optional<ScryfallCard> fetchCardByExactName(String cardName);
}

package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Deck;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckRepository {

    Optional<Deck> findById(UUID id);

    List<Deck> findByUserId(UUID userId);

    Optional<Deck> findByUserIdAndMoxfieldDeckId(UUID userId, String moxfieldDeckId);

    long countByUserId(UUID userId);

    Deck save(Deck deck);
}

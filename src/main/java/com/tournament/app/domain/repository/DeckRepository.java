package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Deck;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckRepository {
    Deck save(Deck deck);
    Optional<Deck> findById(UUID id);
    Optional<Deck> findByUserIdAndMoxfieldDeckId(UUID userId, String moxfieldDeckId);
    List<Deck> findByUserId(UUID userId);
    void deleteById(UUID id);
}

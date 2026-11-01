package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.repository.DeckRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.DeckEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class DeckRepositoryAdapter implements DeckRepository {

    private final SpringDataDeckJpaRepository deckJpaRepository;
    private final PersistenceEntityMapper mapper;

    public DeckRepositoryAdapter(SpringDataDeckJpaRepository deckJpaRepository, PersistenceEntityMapper mapper) {
        this.deckJpaRepository = deckJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Deck> findById(UUID id) {
        return deckJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Deck> findByUserId(UUID userId) {
        return deckJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Deck> findByUserIdAndMoxfieldDeckId(UUID userId, String moxfieldDeckId) {
        return deckJpaRepository.findByUserIdAndMoxfieldDeckId(userId, moxfieldDeckId)
            .map(mapper::toDomain);
    }

    @Override
    public long countByUserId(UUID userId) {
        return deckJpaRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public Deck save(Deck deck) {
        DeckEntity saved = deckJpaRepository.save(mapper.toEntity(deck));
        return mapper.toDomain(saved);
    }
}

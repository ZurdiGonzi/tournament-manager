package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataDeckJpaRepository extends JpaRepository<DeckEntity, UUID> {

    List<DeckEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<DeckEntity> findByUserIdAndMoxfieldDeckId(UUID userId, String moxfieldDeckId);

    long countByUserId(UUID userId);
}

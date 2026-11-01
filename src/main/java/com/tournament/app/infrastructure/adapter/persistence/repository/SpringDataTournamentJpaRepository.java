package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.TournamentStatus;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataTournamentJpaRepository extends JpaRepository<TournamentEntity, UUID> {

    List<TournamentEntity> findByStoreIdOrderByScheduledAtDesc(UUID storeId);

    List<TournamentEntity> findByStoreIdAndStatusOrderByScheduledAtDesc(UUID storeId, TournamentStatus status);

    List<TournamentEntity> findByLeagueIdOrderByScheduledAtDesc(UUID leagueId);

    boolean existsByTemplateIdAndScheduledAt(UUID templateId, Instant scheduledAt);

    long countByStoreId(UUID storeId);
}

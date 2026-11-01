package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataTournamentTemplateJpaRepository extends JpaRepository<TournamentTemplateEntity, UUID> {

    List<TournamentTemplateEntity> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    List<TournamentTemplateEntity> findByStoreIdAndActiveTrueOrderByCreatedAtDesc(UUID storeId);

    List<TournamentTemplateEntity> findByLeagueIdOrderByCreatedAtDesc(UUID leagueId);

    List<TournamentTemplateEntity> findByActiveTrue();
}

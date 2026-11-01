package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.LeagueStatus;
import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLeagueJpaRepository extends JpaRepository<LeagueEntity, UUID> {

    List<LeagueEntity> findByStoreIdOrderByStartDateDesc(UUID storeId);

    List<LeagueEntity> findByStoreIdAndStatusOrderByStartDateDesc(UUID storeId, LeagueStatus status);

    List<LeagueEntity> findByStoreIdAndFormatOrderByStartDateDesc(UUID storeId, MagicFormat format);
}

package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.infrastructure.adapter.persistence.jpa.ScryfallCardStatsViewEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.ScryfallCardStatsViewId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataScryfallCardStatsJpaRepository extends JpaRepository<ScryfallCardStatsViewEntity, ScryfallCardStatsViewId> {

    List<ScryfallCardStatsViewEntity> findByIdStoreIdOrderByTotalCopiesPlayedDesc(UUID storeId);

    List<ScryfallCardStatsViewEntity> findByIdStoreIdAndIdFormatOrderByTotalCopiesPlayedDesc(UUID storeId, MagicFormat format);

    List<ScryfallCardStatsViewEntity> findByIdLeagueIdOrderByTotalCopiesPlayedDesc(UUID leagueId);
}

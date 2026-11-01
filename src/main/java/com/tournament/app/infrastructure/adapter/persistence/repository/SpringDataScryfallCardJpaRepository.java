package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.ScryfallCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataScryfallCardJpaRepository extends JpaRepository<ScryfallCardEntity, UUID> {

    List<ScryfallCardEntity> findByOracleId(UUID oracleId);

    Optional<ScryfallCardEntity> findFirstByNameIgnoreCaseOrderByLastSyncedAtDesc(String name);

    List<ScryfallCardEntity> findByNameInIgnoreCase(Collection<String> names);
}

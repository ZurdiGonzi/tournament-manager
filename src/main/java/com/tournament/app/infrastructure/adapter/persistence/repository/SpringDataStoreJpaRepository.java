package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataStoreJpaRepository extends JpaRepository<StoreEntity, UUID> {

    Optional<StoreEntity> findByName(String name);

    List<StoreEntity> findByOwnerId(UUID ownerId);

    @Query("""
        SELECT s FROM StoreEntity s
        JOIN StoreMemberEntity sm ON sm.id.storeId = s.id
        WHERE sm.id.userId = :userId
        ORDER BY s.name ASC
        """)
    List<StoreEntity> findStoresByMemberUserId(@Param("userId") UUID userId);
}

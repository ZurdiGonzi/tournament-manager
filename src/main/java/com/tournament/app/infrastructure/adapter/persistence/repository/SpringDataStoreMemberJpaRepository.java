package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.StoreMemberEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.StoreMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataStoreMemberJpaRepository extends JpaRepository<StoreMemberEntity, StoreMemberId> {

    List<StoreMemberEntity> findByIdStoreIdOrderByJoinedAtAsc(UUID storeId);

    List<StoreMemberEntity> findByIdUserIdOrderByJoinedAtAsc(UUID userId);

    long countByIdStoreId(UUID storeId);
}

package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserJpaRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    @Query("""
        SELECT u FROM UserEntity u
        JOIN StoreMemberEntity sm ON sm.id.userId = u.id
        WHERE sm.id.storeId = :storeId
        ORDER BY sm.joinedAt ASC
        """)
    List<UserEntity> findUsersByStoreId(@Param("storeId") UUID storeId);
}

package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.RegistrationDeckCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataRegistrationDeckCardJpaRepository extends JpaRepository<RegistrationDeckCardEntity, UUID> {

    List<RegistrationDeckCardEntity> findByRegistrationId(UUID registrationId);

    @Query("""
        SELECT rdc FROM RegistrationDeckCardEntity rdc
        JOIN TournamentRegistrationEntity tr ON tr.id = rdc.registrationId
        WHERE tr.tournamentId = :tournamentId
        """)
    List<RegistrationDeckCardEntity> findByTournamentId(@Param("tournamentId") UUID tournamentId);
}

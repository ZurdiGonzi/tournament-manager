package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRegistrationJpaRepository extends JpaRepository<TournamentRegistrationEntity, UUID> {

    Optional<TournamentRegistrationEntity> findByTournamentIdAndUserId(UUID tournamentId, UUID userId);

    List<TournamentRegistrationEntity> findByTournamentIdOrderByRegisteredAtAsc(UUID tournamentId);

    List<TournamentRegistrationEntity> findByTournamentIdOrderByMatchPointsDescOmwPercentageDescRegisteredAtAsc(UUID tournamentId);

    List<TournamentRegistrationEntity> findByUserIdOrderByRegisteredAtDesc(UUID userId);

    long countByTournamentId(UUID tournamentId);
}

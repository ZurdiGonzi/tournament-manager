package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataRoundJpaRepository extends JpaRepository<RoundEntity, UUID> {

    Optional<RoundEntity> findByTournamentIdAndRoundNumber(UUID tournamentId, int roundNumber);

    List<RoundEntity> findByTournamentIdOrderByRoundNumberAsc(UUID tournamentId);
}

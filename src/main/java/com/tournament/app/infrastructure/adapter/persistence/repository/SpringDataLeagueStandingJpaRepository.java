package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueStandingEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueStandingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLeagueStandingJpaRepository extends JpaRepository<LeagueStandingEntity, LeagueStandingId> {

    List<LeagueStandingEntity> findByIdLeagueIdOrderByTotalPointsDescTournamentsPlayedDesc(UUID leagueId);

    List<LeagueStandingEntity> findByIdUserIdOrderByUpdatedAtDesc(UUID userId);
}

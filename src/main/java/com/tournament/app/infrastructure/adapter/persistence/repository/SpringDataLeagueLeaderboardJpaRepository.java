package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueLeaderboardViewEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueStandingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataLeagueLeaderboardJpaRepository extends JpaRepository<LeagueLeaderboardViewEntity, LeagueStandingId> {

    List<LeagueLeaderboardViewEntity> findByIdLeagueIdOrderByCurrentRankAsc(UUID leagueId);
}

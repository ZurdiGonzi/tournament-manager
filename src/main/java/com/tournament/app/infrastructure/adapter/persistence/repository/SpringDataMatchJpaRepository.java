package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.infrastructure.adapter.persistence.jpa.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataMatchJpaRepository extends JpaRepository<MatchEntity, UUID> {

    List<MatchEntity> findByRoundIdOrderByTableNumberAsc(UUID roundId);

    @Query("""
        SELECT m FROM MatchEntity m
        JOIN RoundEntity r ON r.id = m.roundId
        WHERE r.tournamentId = :tournamentId
        ORDER BY r.roundNumber ASC, m.tableNumber ASC
        """)
    List<MatchEntity> findByTournamentId(@Param("tournamentId") UUID tournamentId);
}

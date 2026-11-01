package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.LeagueStanding;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "league_standings")
public class LeagueStandingEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "total_league_points", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalLeaguePoints;

    @Column(name = "matchdays_played", nullable = false)
    private int matchdaysPlayed;

    @Column(name = "total_match_wins", nullable = false)
    private int totalMatchWins;

    @Column(name = "total_matches_played", nullable = false)
    private int totalMatchesPlayed;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LeagueStandingEntity() {
    }

    public static LeagueStandingEntity fromDomain(LeagueStanding standing) {
        LeagueStandingEntity e = new LeagueStandingEntity();
        e.id = standing.id();
        e.leagueId = standing.leagueId();
        e.userId = standing.userId();
        e.totalLeaguePoints = standing.totalLeaguePoints();
        e.matchdaysPlayed = standing.matchdaysPlayed();
        e.totalMatchWins = standing.totalMatchWins();
        e.totalMatchesPlayed = standing.totalMatchesPlayed();
        e.updatedAt = standing.updatedAt();
        return e;
    }

    public LeagueStanding toDomain() {
        return new LeagueStanding(
                id,
                leagueId,
                userId,
                totalLeaguePoints,
                matchdaysPlayed,
                totalMatchWins,
                totalMatchesPlayed,
                updatedAt
        );
    }

    public UUID getLeagueId() {
        return leagueId;
    }
}

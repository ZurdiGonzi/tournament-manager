package com.tournament.app.infrastructure.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Immutable
@Table(name = "v_league_leaderboard")
public class LeagueLeaderboardViewEntity {

    @EmbeddedId
    private LeagueStandingId id;

    @Column(name = "store_id", insertable = false, updatable = false)
    private UUID storeId;

    @Column(name = "league_name", insertable = false, updatable = false)
    private String leagueName;

    @Column(name = "username", insertable = false, updatable = false)
    private String username;

    @Column(name = "total_points", insertable = false, updatable = false)
    private long totalPoints;

    @Column(name = "tournaments_played", insertable = false, updatable = false)
    private long tournamentsPlayed;

    @Column(name = "current_rank", insertable = false, updatable = false)
    private long currentRank;

    public LeagueLeaderboardViewEntity() {
    }

    public LeagueStandingId getId() {
        return id;
    }

    public UUID getLeagueId() {
        return id != null ? id.getLeagueId() : null;
    }

    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public String getUsername() {
        return username;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public long getTournamentsPlayed() {
        return tournamentsPlayed;
    }

    public long getCurrentRank() {
        return currentRank;
    }
}

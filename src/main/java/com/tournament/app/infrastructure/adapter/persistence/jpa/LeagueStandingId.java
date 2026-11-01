package com.tournament.app.infrastructure.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LeagueStandingId implements Serializable {

    @Column(name = "league_id", nullable = false)
    private UUID leagueId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public LeagueStandingId() {
    }

    public LeagueStandingId(UUID leagueId, UUID userId) {
        this.leagueId = leagueId;
        this.userId = userId;
    }

    public UUID getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(UUID leagueId) {
        this.leagueId = leagueId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeagueStandingId that)) return false;
        return Objects.equals(leagueId, that.leagueId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leagueId, userId);
    }
}

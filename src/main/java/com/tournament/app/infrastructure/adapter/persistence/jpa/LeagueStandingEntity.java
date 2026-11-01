package com.tournament.app.infrastructure.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "league_standings")
public class LeagueStandingEntity {

    @EmbeddedId
    private LeagueStandingId id;

    @Column(name = "total_points", nullable = false)
    private int totalPoints;

    @Column(name = "tournaments_played", nullable = false)
    private int tournamentsPlayed;

    @Column(name = "matches_won", nullable = false)
    private int matchesWon;

    @Column(name = "matches_drawn", nullable = false)
    private int matchesDrawn;

    @Column(name = "matches_lost", nullable = false)
    private int matchesLost;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LeagueStandingEntity() {
    }

    public LeagueStandingEntity(
        UUID leagueId,
        UUID userId,
        int totalPoints,
        int tournamentsPlayed,
        int matchesWon,
        int matchesDrawn,
        int matchesLost,
        Instant updatedAt
    ) {
        this.id = new LeagueStandingId(leagueId, userId);
        this.totalPoints = totalPoints;
        this.tournamentsPlayed = tournamentsPlayed;
        this.matchesWon = matchesWon;
        this.matchesDrawn = matchesDrawn;
        this.matchesLost = matchesLost;
        this.updatedAt = updatedAt;
    }

    public LeagueStandingId getId() {
        return id;
    }

    public void setId(LeagueStandingId id) {
        this.id = id;
    }

    public UUID getLeagueId() {
        return id != null ? id.getLeagueId() : null;
    }

    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getTournamentsPlayed() {
        return tournamentsPlayed;
    }

    public void setTournamentsPlayed(int tournamentsPlayed) {
        this.tournamentsPlayed = tournamentsPlayed;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }

    public int getMatchesDrawn() {
        return matchesDrawn;
    }

    public void setMatchesDrawn(int matchesDrawn) {
        this.matchesDrawn = matchesDrawn;
    }

    public int getMatchesLost() {
        return matchesLost;
    }

    public void setMatchesLost(int matchesLost) {
        this.matchesLost = matchesLost;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

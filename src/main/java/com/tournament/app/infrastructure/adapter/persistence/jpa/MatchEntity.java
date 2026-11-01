package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "round_id", nullable = false)
    private UUID roundId;

    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    @Column(name = "player1_id", nullable = false)
    private UUID player1Id;

    @Column(name = "player2_id")
    private UUID player2Id;

    @Column(name = "player1_wins", nullable = false)
    private int player1Wins;

    @Column(name = "player2_wins", nullable = false)
    private int player2Wins;

    @Column(name = "draws", nullable = false)
    private int draws;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private MatchStatus status;

    @Column(name = "winner_id")
    private UUID winnerId;

    public MatchEntity() {
    }

    public MatchEntity(
        UUID id,
        UUID roundId,
        int tableNumber,
        UUID player1Id,
        UUID player2Id,
        int player1Wins,
        int player2Wins,
        int draws,
        MatchStatus status,
        UUID winnerId
    ) {
        this.id = id;
        this.roundId = roundId;
        this.tableNumber = tableNumber;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.player1Wins = player1Wins;
        this.player2Wins = player2Wins;
        this.draws = draws;
        this.status = status;
        this.winnerId = winnerId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRoundId() {
        return roundId;
    }

    public void setRoundId(UUID roundId) {
        this.roundId = roundId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(UUID player1Id) {
        this.player1Id = player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(UUID player2Id) {
        this.player2Id = player2Id;
    }

    public int getPlayer1Wins() {
        return player1Wins;
    }

    public void setPlayer1Wins(int player1Wins) {
        this.player1Wins = player1Wins;
    }

    public int getPlayer2Wins() {
        return player2Wins;
    }

    public void setPlayer2Wins(int player2Wins) {
        this.player2Wins = player2Wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }
}

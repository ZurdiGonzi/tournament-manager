package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchParticipant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.List;
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

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "winner_id")
    private UUID winnerId;

    protected MatchEntity() {
    }

    public static MatchEntity fromDomain(Match m) {
        MatchEntity e = new MatchEntity();
        e.id = m.id();
        e.roundId = m.roundId();
        e.tableNumber = m.tableNumber();
        e.player1Id = m.player1Id();
        e.player2Id = m.player2Id();
        e.player1Wins = m.player1Wins();
        e.player2Wins = m.player2Wins();
        e.draws = m.draws();
        e.status = m.status();
        e.winnerId = m.winnerId();
        return e;
    }

    public Match toDomain(List<MatchParticipant> participants) {
        return new Match(
                id,
                roundId,
                tableNumber,
                player1Id,
                player2Id,
                player1Wins,
                player2Wins,
                draws,
                status,
                winnerId,
                participants
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoundId() {
        return roundId;
    }
}

package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.Round;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rounds")
public class RoundEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    protected RoundEntity() {
    }

    public static RoundEntity fromDomain(Round round) {
        RoundEntity e = new RoundEntity();
        e.id = round.id();
        e.tournamentId = round.tournamentId();
        e.roundNumber = round.roundNumber();
        e.status = round.status();
        e.startedAt = round.startedAt();
        e.endedAt = round.endedAt();
        return e;
    }

    public Round toDomain() {
        return new Round(id, tournamentId, roundNumber, status, startedAt, endedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTournamentId() {
        return tournamentId;
    }
}

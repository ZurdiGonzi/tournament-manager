package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.MatchParticipant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "match_participants")
public class MatchParticipantEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "registration_id", nullable = false)
    private UUID registrationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    @Column(name = "commander_oracle_id")
    private UUID commanderOracleId;

    @Column(name = "commander_name", length = 200, nullable = false)
    private String commanderName;

    @Column(name = "placement", nullable = false)
    private int placement;

    @Column(name = "wins", nullable = false)
    private int wins;

    @Column(name = "losses", nullable = false)
    private int losses;

    @Column(name = "draws", nullable = false)
    private int draws;

    @Column(name = "kills", nullable = false)
    private int kills;

    @Column(name = "match_points_earned", precision = 10, scale = 2, nullable = false)
    private BigDecimal matchPointsEarned;

    protected MatchParticipantEntity() {
    }

    public static MatchParticipantEntity fromDomain(MatchParticipant mp) {
        MatchParticipantEntity e = new MatchParticipantEntity();
        e.id = mp.id();
        e.matchId = mp.matchId();
        e.registrationId = mp.registrationId();
        e.userId = mp.userId();
        e.deckId = mp.deckId();
        e.commanderOracleId = mp.commanderOracleId();
        e.commanderName = mp.commanderName();
        e.placement = mp.placement();
        e.wins = mp.wins();
        e.losses = mp.losses();
        e.draws = mp.draws();
        e.kills = mp.kills();
        e.matchPointsEarned = mp.matchPointsEarned();
        return e;
    }

    public MatchParticipant toDomain() {
        return new MatchParticipant(
                id,
                matchId,
                registrationId,
                userId,
                deckId,
                commanderOracleId,
                commanderName,
                placement,
                wins,
                losses,
                draws,
                kills,
                matchPointsEarned
        );
    }

    public UUID getMatchId() {
        return matchId;
    }

    public UUID getDeckId() {
        return deckId;
    }
}

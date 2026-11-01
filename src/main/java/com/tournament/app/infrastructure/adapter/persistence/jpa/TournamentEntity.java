package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.Tournament;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tournaments")
public class TournamentEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "league_id")
    private UUID leagueId;

    @Column(name = "matchday_number")
    private Integer matchdayNumber;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "entry_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal entryFee;

    @Column(name = "prize_description", columnDefinition = "text", nullable = false)
    private String prizeDescription;

    @Column(name = "format", length = 30, nullable = false)
    private String format;

    @Column(name = "max_deck_budget", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxDeckBudget;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "current_round", nullable = false)
    private int currentRound;

    @Column(name = "max_rounds", nullable = false)
    private int maxRounds;

    @Column(name = "match_points_formula_id")
    private UUID matchPointsFormulaId;

    @Column(name = "pairing_formula_id")
    private UUID pairingFormulaId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TournamentEntity() {
    }

    public static TournamentEntity fromDomain(Tournament t) {
        TournamentEntity e = new TournamentEntity();
        e.id = t.id();
        e.storeId = t.storeId();
        e.templateId = t.templateId();
        e.leagueId = t.leagueId();
        e.matchdayNumber = t.matchdayNumber();
        e.name = t.name();
        e.scheduledAt = t.scheduledAt();
        e.entryFee = t.entryFee();
        e.prizeDescription = t.prizeDescription();
        e.format = t.format();
        e.maxDeckBudget = t.maxDeckBudget();
        e.status = t.status();
        e.currentRound = t.currentRound();
        e.maxRounds = t.maxRounds();
        e.matchPointsFormulaId = t.matchPointsFormulaId();
        e.pairingFormulaId = t.pairingFormulaId();
        e.createdAt = t.createdAt();
        return e;
    }

    public Tournament toDomain() {
        return new Tournament(
                id,
                storeId,
                templateId,
                leagueId,
                matchdayNumber,
                name,
                scheduledAt,
                entryFee,
                prizeDescription,
                format,
                maxDeckBudget,
                status,
                currentRound,
                maxRounds,
                matchPointsFormulaId,
                pairingFormulaId,
                createdAt
        );
    }

    public UUID getStoreId() {
        return storeId;
    }

    public UUID getLeagueId() {
        return leagueId;
    }
}

package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.League;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leagues")
public class LeagueEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "format", length = 30, nullable = false)
    private String format;

    @Column(name = "max_deck_budget", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxDeckBudget;

    @Column(name = "match_points_formula_id", nullable = false)
    private UUID matchPointsFormulaId;

    @Column(name = "matchday_points_formula_id", nullable = false)
    private UUID matchdayPointsFormulaId;

    @Column(name = "pairing_formula_id", nullable = false)
    private UUID pairingFormulaId;

    @Column(name = "status", length = 25, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LeagueEntity() {
    }

    public static LeagueEntity fromDomain(League league) {
        LeagueEntity e = new LeagueEntity();
        e.id = league.id();
        e.storeId = league.storeId();
        e.name = league.name();
        e.format = league.format();
        e.maxDeckBudget = league.maxDeckBudget();
        e.matchPointsFormulaId = league.matchPointsFormulaId();
        e.matchdayPointsFormulaId = league.matchdayPointsFormulaId();
        e.pairingFormulaId = league.pairingFormulaId();
        e.status = league.status();
        e.createdAt = league.createdAt();
        return e;
    }

    public League toDomain() {
        return new League(
                id,
                storeId,
                name,
                format,
                maxDeckBudget,
                matchPointsFormulaId,
                matchdayPointsFormulaId,
                pairingFormulaId,
                status,
                createdAt
        );
    }

    public UUID getStoreId() {
        return storeId;
    }
}

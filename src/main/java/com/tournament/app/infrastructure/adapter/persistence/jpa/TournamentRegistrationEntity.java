package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.RegistrationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "tournament_registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tournament_id", "user_id"})
)
public class TournamentRegistrationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25, nullable = false)
    private RegistrationStatus status;

    @Column(name = "calculated_deck_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal calculatedDeckPrice;

    @Column(name = "match_points", nullable = false)
    private int matchPoints;

    @Column(name = "omw_percentage", precision = 5, scale = 4, nullable = false)
    private BigDecimal omwPercentage;

    @Column(name = "final_rank")
    private Integer finalRank;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "deck_snapshot", columnDefinition = "jsonb", nullable = false)
    private DeckSnapshot deckSnapshot;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    public TournamentRegistrationEntity() {
    }

    public TournamentRegistrationEntity(
        UUID id,
        UUID tournamentId,
        UUID userId,
        UUID deckId,
        RegistrationStatus status,
        BigDecimal calculatedDeckPrice,
        int matchPoints,
        BigDecimal omwPercentage,
        Integer finalRank,
        DeckSnapshot deckSnapshot,
        Instant registeredAt
    ) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.userId = userId;
        this.deckId = deckId;
        this.status = status;
        this.calculatedDeckPrice = calculatedDeckPrice;
        this.matchPoints = matchPoints;
        this.omwPercentage = omwPercentage;
        this.finalRank = finalRank;
        this.deckSnapshot = deckSnapshot;
        this.registeredAt = registeredAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(UUID tournamentId) {
        this.tournamentId = tournamentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDeckId() {
        return deckId;
    }

    public void setDeckId(UUID deckId) {
        this.deckId = deckId;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public BigDecimal getCalculatedDeckPrice() {
        return calculatedDeckPrice;
    }

    public void setCalculatedDeckPrice(BigDecimal calculatedDeckPrice) {
        this.calculatedDeckPrice = calculatedDeckPrice;
    }

    public int getMatchPoints() {
        return matchPoints;
    }

    public void setMatchPoints(int matchPoints) {
        this.matchPoints = matchPoints;
    }

    public BigDecimal getOmwPercentage() {
        return omwPercentage;
    }

    public void setOmwPercentage(BigDecimal omwPercentage) {
        this.omwPercentage = omwPercentage;
    }

    public Integer getFinalRank() {
        return finalRank;
    }

    public void setFinalRank(Integer finalRank) {
        this.finalRank = finalRank;
    }

    public DeckSnapshot getDeckSnapshot() {
        return deckSnapshot;
    }

    public void setDeckSnapshot(DeckSnapshot deckSnapshot) {
        this.deckSnapshot = deckSnapshot;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }
}

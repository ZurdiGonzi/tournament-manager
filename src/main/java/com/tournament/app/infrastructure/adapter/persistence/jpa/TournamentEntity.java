package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.domain.model.TournamentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "tournaments",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_tournament_template_schedule",
        columnNames = {"template_id", "scheduled_at"}
    )
)
public class TournamentEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "league_id")
    private UUID leagueId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "entry_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal entryFee;

    @Column(name = "prize_description", columnDefinition = "TEXT", nullable = false)
    private String prizeDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 30, nullable = false)
    private MagicFormat format;

    @Column(name = "max_deck_budget", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxDeckBudget;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private TournamentStatus status;

    @Column(name = "current_round", nullable = false)
    private int currentRound;

    @Column(name = "max_rounds", nullable = false)
    private int maxRounds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TournamentEntity() {
    }

    public TournamentEntity(
        UUID id,
        UUID storeId,
        UUID leagueId,
        UUID templateId,
        String name,
        Instant scheduledAt,
        BigDecimal entryFee,
        String prizeDescription,
        MagicFormat format,
        BigDecimal maxDeckBudget,
        int maxPlayers,
        TournamentStatus status,
        int currentRound,
        int maxRounds,
        Instant createdAt
    ) {
        this.id = id;
        this.storeId = storeId;
        this.leagueId = leagueId;
        this.templateId = templateId;
        this.name = name;
        this.scheduledAt = scheduledAt;
        this.entryFee = entryFee;
        this.prizeDescription = prizeDescription;
        this.format = format;
        this.maxDeckBudget = maxDeckBudget;
        this.maxPlayers = maxPlayers;
        this.status = status;
        this.currentRound = currentRound;
        this.maxRounds = maxRounds;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public UUID getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(UUID leagueId) {
        this.leagueId = leagueId;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public BigDecimal getEntryFee() {
        return entryFee;
    }

    public void setEntryFee(BigDecimal entryFee) {
        this.entryFee = entryFee;
    }

    public String getPrizeDescription() {
        return prizeDescription;
    }

    public void setPrizeDescription(String prizeDescription) {
        this.prizeDescription = prizeDescription;
    }

    public MagicFormat getFormat() {
        return format;
    }

    public void setFormat(MagicFormat format) {
        this.format = format;
    }

    public BigDecimal getMaxDeckBudget() {
        return maxDeckBudget;
    }

    public void setMaxDeckBudget(BigDecimal maxDeckBudget) {
        this.maxDeckBudget = maxDeckBudget;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

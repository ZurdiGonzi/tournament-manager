package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.MagicFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tournament_templates")
public class TournamentTemplateEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "league_id")
    private UUID leagueId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "cron_expression", length = 60, nullable = false)
    private String cronExpression;

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

    @Column(name = "max_rounds", nullable = false)
    private int maxRounds;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "last_generated_at")
    private Instant lastGeneratedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TournamentTemplateEntity() {
    }

    public TournamentTemplateEntity(
        UUID id,
        UUID storeId,
        UUID leagueId,
        String name,
        String cronExpression,
        BigDecimal entryFee,
        String prizeDescription,
        MagicFormat format,
        BigDecimal maxDeckBudget,
        int maxPlayers,
        int maxRounds,
        boolean active,
        Instant lastGeneratedAt,
        Instant createdAt
    ) {
        this.id = id;
        this.storeId = storeId;
        this.leagueId = leagueId;
        this.name = name;
        this.cronExpression = cronExpression;
        this.entryFee = entryFee;
        this.prizeDescription = prizeDescription;
        this.format = format;
        this.maxDeckBudget = maxDeckBudget;
        this.maxPlayers = maxPlayers;
        this.maxRounds = maxRounds;
        this.active = active;
        this.lastGeneratedAt = lastGeneratedAt;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
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

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastGeneratedAt() {
        return lastGeneratedAt;
    }

    public void setLastGeneratedAt(Instant lastGeneratedAt) {
        this.lastGeneratedAt = lastGeneratedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

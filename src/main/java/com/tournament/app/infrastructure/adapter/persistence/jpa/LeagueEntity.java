package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.LeagueStatus;
import com.tournament.app.domain.model.MagicFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 30, nullable = false)
    private MagicFormat format;

    @Column(name = "prize_description", columnDefinition = "TEXT")
    private String prizeDescription;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private LeagueStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public LeagueEntity() {
    }

    public LeagueEntity(
        UUID id,
        UUID storeId,
        String name,
        MagicFormat format,
        String prizeDescription,
        Instant startDate,
        Instant endDate,
        LeagueStatus status,
        Instant createdAt
    ) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.format = format;
        this.prizeDescription = prizeDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MagicFormat getFormat() {
        return format;
    }

    public void setFormat(MagicFormat format) {
        this.format = format;
    }

    public String getPrizeDescription() {
        return prizeDescription;
    }

    public void setPrizeDescription(String prizeDescription) {
        this.prizeDescription = prizeDescription;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public LeagueStatus getStatus() {
        return status;
    }

    public void setStatus(LeagueStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

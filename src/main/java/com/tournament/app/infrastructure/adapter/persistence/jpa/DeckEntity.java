package com.tournament.app.infrastructure.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "decks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "moxfield_deck_id"})
)
public class DeckEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "moxfield_url", length = 500, nullable = false)
    private String moxfieldUrl;

    @Column(name = "moxfield_deck_id", length = 64, nullable = false)
    private String moxfieldDeckId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DeckEntity() {
    }

    public DeckEntity(
        UUID id,
        UUID userId,
        String name,
        String moxfieldUrl,
        String moxfieldDeckId,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.moxfieldUrl = moxfieldUrl;
        this.moxfieldDeckId = moxfieldDeckId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMoxfieldUrl() {
        return moxfieldUrl;
    }

    public void setMoxfieldUrl(String moxfieldUrl) {
        this.moxfieldUrl = moxfieldUrl;
    }

    public String getMoxfieldDeckId() {
        return moxfieldDeckId;
    }

    public void setMoxfieldDeckId(String moxfieldDeckId) {
        this.moxfieldDeckId = moxfieldDeckId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

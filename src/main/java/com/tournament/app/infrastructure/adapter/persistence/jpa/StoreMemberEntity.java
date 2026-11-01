package com.tournament.app.infrastructure.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "store_members")
public class StoreMemberEntity {

    @EmbeddedId
    private StoreMemberId id;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public StoreMemberEntity() {
    }

    public StoreMemberEntity(UUID storeId, UUID userId, Instant joinedAt) {
        this.id = new StoreMemberId(storeId, userId);
        this.joinedAt = joinedAt;
    }

    public StoreMemberId getId() {
        return id;
    }

    public void setId(StoreMemberId id) {
        this.id = id;
    }

    public UUID getStoreId() {
        return id != null ? id.getStoreId() : null;
    }

    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}

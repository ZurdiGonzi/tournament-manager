package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stores")
public class StoreEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 120, nullable = false, unique = true)
    private String name;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected StoreEntity() {
    }

    public static StoreEntity fromDomain(Store store) {
        StoreEntity e = new StoreEntity();
        e.id = store.id();
        e.name = store.name();
        e.ownerId = store.ownerId();
        e.currency = store.currency();
        e.createdAt = store.createdAt();
        return e;
    }

    public Store toDomain() {
        return new Store(id, name, ownerId, currency, createdAt);
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}

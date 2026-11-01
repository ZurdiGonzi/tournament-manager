package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Store(
    UUID id,
    String name,
    UUID ownerId,
    CurrencyCode currency,
    Instant createdAt
) {
    public Store {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException("store name must be between 1 and 120 characters");
        }
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        currency = currency == null ? CurrencyCode.EUR : currency;
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}

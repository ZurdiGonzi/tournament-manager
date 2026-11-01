package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Deck(
    UUID id,
    UUID userId,
    String name,
    String moxfieldUrl,
    String moxfieldDeckId,
    Instant createdAt,
    Instant updatedAt
) {
    public Deck {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank() || name.length() > 150) {
            throw new IllegalArgumentException("deck name must be between 1 and 150 characters");
        }
        Objects.requireNonNull(moxfieldUrl, "moxfieldUrl must not be null");
        if (moxfieldUrl.isBlank() || moxfieldUrl.length() > 500) {
            throw new IllegalArgumentException("moxfieldUrl must be between 1 and 500 characters");
        }
        Objects.requireNonNull(moxfieldDeckId, "moxfieldDeckId must not be null");
        if (moxfieldDeckId.isBlank() || moxfieldDeckId.length() > 64) {
            throw new IllegalArgumentException("moxfieldDeckId must be between 1 and 64 characters");
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}

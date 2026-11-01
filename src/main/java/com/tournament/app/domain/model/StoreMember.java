package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StoreMember(
    UUID storeId,
    UUID userId,
    String username,
    UserRole role,
    Instant joinedAt
) {
    public StoreMember {
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(joinedAt, "joinedAt must not be null");
    }
}

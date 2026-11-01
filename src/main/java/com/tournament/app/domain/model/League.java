package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record League(
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
    public League {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank() || name.length() > 150) {
            throw new IllegalArgumentException("league name must be between 1 and 150 characters");
        }
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(startDate, "startDate must not be null");
        status = status == null ? LeagueStatus.IN_PROGRESS : status;
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}

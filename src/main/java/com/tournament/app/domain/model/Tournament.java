package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Tournament(
    UUID id,
    UUID storeId,
    UUID leagueId,
    UUID templateId,
    String name,
    Instant scheduledAt,
    BigDecimal entryFee,
    String prizeDescription,
    TournamentRestrictions restrictions,
    int maxPlayers,
    TournamentStatus status,
    int currentRound,
    int maxRounds,
    Instant createdAt
) {
    public Tournament {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank() || name.length() > 150) {
            throw new IllegalArgumentException("tournament name must be between 1 and 150 characters");
        }
        Objects.requireNonNull(scheduledAt, "scheduledAt must not be null");
        entryFee = entryFee == null ? BigDecimal.ZERO : entryFee;
        if (entryFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("entryFee must be >= 0");
        }
        Objects.requireNonNull(prizeDescription, "prizeDescription must not be null");
        Objects.requireNonNull(restrictions, "restrictions must not be null");
        if (maxPlayers < 2) {
            throw new IllegalArgumentException("maxPlayers must be >= 2");
        }
        status = status == null ? TournamentStatus.REGISTRATION_OPEN : status;
        if (currentRound < 0) {
            throw new IllegalArgumentException("currentRound must be >= 0");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("maxRounds must be > 0");
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public MagicFormat format() {
        return restrictions.format();
    }

    public BigDecimal maxDeckBudget() {
        return restrictions.maxDeckBudget();
    }
}

package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TournamentTemplate(
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
    public TournamentTemplate {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank() || name.length() > 150) {
            throw new IllegalArgumentException("template name must be between 1 and 150 characters");
        }
        Objects.requireNonNull(cronExpression, "cronExpression must not be null");
        if (cronExpression.isBlank() || cronExpression.length() > 60) {
            throw new IllegalArgumentException("cronExpression must be between 1 and 60 characters");
        }
        entryFee = entryFee == null ? BigDecimal.ZERO : entryFee;
        if (entryFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("entryFee must be >= 0");
        }
        Objects.requireNonNull(prizeDescription, "prizeDescription must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(maxDeckBudget, "maxDeckBudget must not be null");
        if (maxDeckBudget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("maxDeckBudget must be > 0");
        }
        if (maxPlayers < 2) {
            throw new IllegalArgumentException("maxPlayers must be >= 2");
        }
        if (maxRounds <= 0) {
            throw new IllegalArgumentException("maxRounds must be > 0");
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}

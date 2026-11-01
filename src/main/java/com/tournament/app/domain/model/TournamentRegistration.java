package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TournamentRegistration(
    UUID id,
    UUID tournamentId,
    UUID userId,
    UUID deckId,
    RegistrationStatus status,
    BigDecimal calculatedDeckPrice,
    int matchPoints,
    BigDecimal omwPercentage,
    Integer finalRank,
    DeckSnapshot deckSnapshot,
    Instant registeredAt
) {
    public TournamentRegistration {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tournamentId, "tournamentId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(deckId, "deckId must not be null");
        status = status == null ? RegistrationStatus.REGISTERED : status;
        Objects.requireNonNull(calculatedDeckPrice, "calculatedDeckPrice must not be null");
        if (calculatedDeckPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("calculatedDeckPrice must be >= 0");
        }
        if (matchPoints < 0) {
            throw new IllegalArgumentException("matchPoints must be >= 0");
        }
        omwPercentage = omwPercentage == null ? BigDecimal.ZERO : omwPercentage;
        if (finalRank != null && finalRank <= 0) {
            throw new IllegalArgumentException("finalRank must be > 0 when present");
        }
        Objects.requireNonNull(deckSnapshot, "deckSnapshot must not be null");
        Objects.requireNonNull(registeredAt, "registeredAt must not be null");
    }
}

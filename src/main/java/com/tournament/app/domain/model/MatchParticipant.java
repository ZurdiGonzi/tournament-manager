package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.UUID;

public record MatchParticipant(
        UUID id,
        UUID matchId,
        UUID registrationId,
        UUID userId,
        UUID deckId,
        UUID commanderOracleId,
        String commanderName,
        int placement,
        int wins,
        int losses,
        int draws,
        int kills,
        BigDecimal matchPointsEarned
) {
    public MatchParticipant {
        if (placement < 1) {
            throw new DomainValidationException("Match participant placement must be >= 1");
        }
        if (wins < 0 || losses < 0 || draws < 0 || kills < 0) {
            throw new DomainValidationException("Match participant counters cannot be negative");
        }
        if (commanderName == null || commanderName.isBlank()) {
            commanderName = "Unknown Deck";
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (matchPointsEarned == null) {
            matchPointsEarned = BigDecimal.ZERO;
        }
    }
}

package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.DomainExceptions.InvalidTournamentStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Tournament(
        UUID id,
        UUID storeId,
        UUID templateId,
        UUID leagueId,
        Integer matchdayNumber,
        String name,
        Instant scheduledAt,
        BigDecimal entryFee,
        String prizeDescription,
        String format,
        BigDecimal maxDeckBudget,
        String status,
        int currentRound,
        int maxRounds,
        UUID matchPointsFormulaId,
        UUID pairingFormulaId,
        Instant createdAt
) {
    private static final Set<String> VALID_STATUSES = Set.of(
            "DRAFT", "REGISTRATION_OPEN", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    );

    public Tournament {
        if (storeId == null) {
            throw new DomainValidationException("Tournament storeId is required");
        }
        if (name == null || name.isBlank() || name.length() > 150) {
            throw new DomainValidationException("Tournament name must be between 1 and 150 characters");
        }
        if (scheduledAt == null) {
            throw new DomainValidationException("Tournament scheduledAt is required");
        }
        if (entryFee == null || entryFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Tournament entryFee must be >= 0");
        }
        if (prizeDescription == null || prizeDescription.isBlank()) {
            throw new DomainValidationException("Tournament prizeDescription is required");
        }
        if (format == null || (!"STANDARD".equals(format) && !"COMMANDER".equals(format))) {
            throw new DomainValidationException("Tournament format must be STANDARD or COMMANDER");
        }
        if (maxDeckBudget == null || maxDeckBudget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("Tournament maxDeckBudget must be > 0");
        }
        if (maxRounds <= 0) {
            throw new DomainValidationException("Organizer must specify a positive number of rounds (maxRounds > 0)");
        }
        if (matchdayNumber != null && matchdayNumber <= 0) {
            throw new DomainValidationException("matchdayNumber must be > 0 when linked to a League");
        }
        if (status == null || !VALID_STATUSES.contains(status)) {
            status = "REGISTRATION_OPEN";
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Tournament advanceToRound(int nextRoundNumber) {
        if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
            throw new InvalidTournamentStateException("Cannot generate rounds for a tournament in state: " + status);
        }
        if (nextRoundNumber > maxRounds) {
            throw new InvalidTournamentStateException(
                    "Cannot exceed organizer configured maxRounds (" + maxRounds + ")");
        }
        return new Tournament(
                id, storeId, templateId, leagueId, matchdayNumber, name, scheduledAt,
                entryFee, prizeDescription, format, maxDeckBudget, "IN_PROGRESS",
                nextRoundNumber, maxRounds, matchPointsFormulaId, pairingFormulaId, createdAt
        );
    }

    public Tournament markCompletedIfFinalRound() {
        String nextStatus = (currentRound >= maxRounds) ? "COMPLETED" : status;
        return new Tournament(
                id, storeId, templateId, leagueId, matchdayNumber, name, scheduledAt,
                entryFee, prizeDescription, format, maxDeckBudget, nextStatus,
                currentRound, maxRounds, matchPointsFormulaId, pairingFormulaId, createdAt
        );
    }
}

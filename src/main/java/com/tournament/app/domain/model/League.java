package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record League(
        UUID id,
        UUID storeId,
        String name,
        String format,
        BigDecimal maxDeckBudget,
        UUID matchPointsFormulaId,
        UUID matchdayPointsFormulaId,
        UUID pairingFormulaId,
        String status,
        Instant createdAt
) {
    public League {
        if (storeId == null) {
            throw new DomainValidationException("League storeId is required");
        }
        if (name == null || name.isBlank() || name.length() > 150) {
            throw new DomainValidationException("League name must be between 1 and 150 characters");
        }
        if (format == null || (!"STANDARD".equals(format) && !"COMMANDER".equals(format))) {
            throw new DomainValidationException("League format must be STANDARD or COMMANDER");
        }
        if (maxDeckBudget == null || maxDeckBudget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainValidationException("League maxDeckBudget must be > 0");
        }
        if (matchPointsFormulaId == null || matchdayPointsFormulaId == null || pairingFormulaId == null) {
            throw new DomainValidationException("All three mathematical formula references are required for a League");
        }
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

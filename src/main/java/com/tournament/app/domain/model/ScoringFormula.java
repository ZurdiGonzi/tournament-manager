package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.time.Instant;
import java.util.UUID;

public record ScoringFormula(
        UUID id,
        UUID storeId,
        String name,
        FormulaType formulaType,
        String expression,
        String description,
        Instant updatedAt
) {
    public ScoringFormula {
        if (storeId == null) {
            throw new DomainValidationException("storeId is required for ScoringFormula");
        }
        if (name == null || name.isBlank() || name.length() > 120) {
            throw new DomainValidationException("Formula name must be between 1 and 120 characters");
        }
        if (formulaType == null) {
            throw new DomainValidationException("formulaType is required");
        }
        if (expression == null || expression.isBlank()) {
            throw new DomainValidationException("Mathematical expression must not be blank");
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }
}

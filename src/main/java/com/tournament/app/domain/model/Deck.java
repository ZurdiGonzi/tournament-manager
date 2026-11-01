package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Deck(
        UUID id,
        UUID userId,
        String name,
        String moxfieldUrl,
        String moxfieldDeckId,
        String format,
        UUID commanderOracleId,
        String commanderName,
        List<String> colorIdentity,
        BigDecimal lastCalculatedPrice,
        Instant createdAt,
        Instant updatedAt
) {
    public Deck {
        if (userId == null) {
            throw new DomainValidationException("Deck userId is required");
        }
        if (name == null || name.isBlank() || name.length() > 150) {
            throw new DomainValidationException("Deck name must be between 1 and 150 characters");
        }
        if (moxfieldUrl == null || moxfieldUrl.isBlank() || moxfieldUrl.length() > 500) {
            throw new DomainValidationException("Moxfield URL is required and must be <= 500 chars");
        }
        if (moxfieldDeckId == null || moxfieldDeckId.isBlank() || moxfieldDeckId.length() > 64) {
            throw new DomainValidationException("Moxfield deck ID is required");
        }
        if (format == null || (!"STANDARD".equals(format) && !"COMMANDER".equals(format))) {
            throw new DomainValidationException("Format must be STANDARD or COMMANDER");
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (colorIdentity == null) {
            colorIdentity = List.of();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }
}

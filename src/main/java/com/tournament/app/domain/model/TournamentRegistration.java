package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TournamentRegistration(
        UUID id,
        UUID tournamentId,
        UUID userId,
        UUID deckId,
        String status,
        BigDecimal calculatedDeckPrice,
        String commanderName,
        UUID commanderOracleId,
        List<String> colorIdentity,
        BigDecimal tournamentPoints,
        BigDecimal calculatedLeagueMatchdayPoints,
        DeckSnapshot deckSnapshot,
        Instant registeredAt
) {
    public TournamentRegistration {
        if (tournamentId == null || userId == null || deckId == null) {
            throw new DomainValidationException("tournamentId, userId, and deckId are required");
        }
        if (deckSnapshot == null) {
            throw new DomainValidationException("Immutable deckSnapshot is required");
        }
        if (calculatedDeckPrice == null || calculatedDeckPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("calculatedDeckPrice must be >= 0");
        }
        if (status == null || status.isBlank()) {
            status = "REGISTERED";
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (colorIdentity == null) {
            colorIdentity = List.of();
        }
        if (tournamentPoints == null) {
            tournamentPoints = BigDecimal.ZERO;
        }
        if (calculatedLeagueMatchdayPoints == null) {
            calculatedLeagueMatchdayPoints = BigDecimal.ZERO;
        }
        if (registeredAt == null) {
            registeredAt = Instant.now();
        }
    }

    public TournamentRegistration withUpdatedPoints(BigDecimal newTournamentPoints, BigDecimal newLeaguePoints) {
        return new TournamentRegistration(
                id,
                tournamentId,
                userId,
                deckId,
                status,
                calculatedDeckPrice,
                commanderName,
                commanderOracleId,
                colorIdentity,
                newTournamentPoints,
                newLeaguePoints,
                deckSnapshot,
                registeredAt
        );
    }
}

package com.tournament.app.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class PlayerDeckDtos {

    private PlayerDeckDtos() {
    }

    public record SaveProfileDeckRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Size(max = 500) String moxfieldUrl,
            @NotBlank String format
    ) {
    }

    public record ProfileDeckResponse(
            UUID deckId,
            String name,
            String moxfieldUrl,
            String format,
            String commanderName,
            UUID commanderOracleId,
            List<String> colorIdentity,
            BigDecimal lastCalculatedPrice,
            String currency,
            int cardCount
    ) {
    }

    public record NewDeckSubmission(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Size(max = 500) String moxfieldUrl,
            boolean saveToProfile
    ) {
    }

    public record TournamentRegistrationRequest(
            UUID savedDeckId,
            @Valid NewDeckSubmission newDeck
    ) {
    }

    public record TournamentRegistrationResponse(
            UUID registrationId,
            UUID tournamentId,
            UUID userId,
            UUID deckId,
            String commanderName,
            List<String> colorIdentity,
            BigDecimal calculatedDeckPrice,
            String status
    ) {
    }
}

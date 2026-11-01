package com.tournament.app.application.dto;

import com.tournament.app.domain.model.FormulaType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AdminTournamentDtos {

    private AdminTournamentDtos() {
    }

    public record CreateStoreRequest(
            @NotBlank @Size(max = 120) String name,
            String currency
    ) {
    }

    public record StoreResponse(
            UUID storeId,
            String name,
            UUID ownerId,
            String currency,
            Instant createdAt
    ) {
    }

    public record UpsertScoringFormulaRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull FormulaType formulaType,
            @NotBlank String expression,
            String description
    ) {
    }

    public record ScoringFormulaResponse(
            UUID formulaId,
            UUID storeId,
            String name,
            FormulaType formulaType,
            String expression,
            String description,
            Instant updatedAt
    ) {
    }

    public record CreateLeagueRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank String format,
            @NotNull @DecimalMin("0.01") BigDecimal maxDeckBudget,
            @NotNull UUID matchPointsFormulaId,
            @NotNull UUID matchdayPointsFormulaId,
            @NotNull UUID pairingFormulaId
    ) {
    }

    public record LeagueResponse(
            UUID leagueId,
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
    }

    public record CreateTournamentRequest(
            @NotBlank @Size(max = 150) String name,
            UUID leagueId,
            Integer matchdayNumber,
            @NotNull Instant scheduledAt,
            @NotNull @DecimalMin("0.00") BigDecimal entryFee,
            @NotBlank String prizeDescription,
            @NotBlank String format,
            @NotNull @DecimalMin("0.01") BigDecimal maxDeckBudget,
            @Min(1) int maxRounds,
            UUID matchPointsFormulaId,
            UUID pairingFormulaId
    ) {
    }

    public record TournamentResponse(
            UUID tournamentId,
            UUID storeId,
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
            UUID pairingFormulaId
    ) {
    }

    public record ParticipantPairingDto(
            UUID userId,
            UUID deckId,
            String commanderName,
            int placement,
            int wins,
            int losses,
            int draws,
            int kills,
            BigDecimal matchPointsEarned
    ) {
    }

    public record MatchPairingDto(
            UUID matchId,
            int tableNumber,
            String status,
            List<ParticipantPairingDto> participants
    ) {
    }

    public record RoundPairingsResponse(
            UUID tournamentId,
            UUID roundId,
            int roundNumber,
            int maxRounds,
            List<MatchPairingDto> matches
    ) {
    }

    public record ParticipantResultInput(
            @NotNull UUID userId,
            @Min(1) int placement,
            @Min(0) int wins,
            @Min(0) int losses,
            @Min(0) int draws,
            @Min(0) int kills
    ) {
    }

    public record RecordMatchResultRequest(
            @NotEmpty @Valid List<ParticipantResultInput> participantResults
    ) {
    }

    public record MatchdayStandingItem(
            int rank,
            UUID userId,
            String commanderName,
            BigDecimal tournamentPoints,
            BigDecimal calculatedLeagueMatchdayPoints
    ) {
    }

    public record LeagueStandingItem(
            int rank,
            UUID userId,
            BigDecimal totalLeaguePoints,
            int matchdaysPlayed,
            int totalMatchWins,
            int totalMatchesPlayed
    ) {
    }
}

package com.tournament.app.application.dto.tournament;

import com.tournament.app.application.dto.store.StoreSummaryResponse;
import com.tournament.app.domain.model.TournamentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Detalle completo de un torneo.
 */
public record TournamentDetailResponse(
    UUID id,
    StoreSummaryResponse store,
    String name,
    TournamentStatus status,
    Instant scheduledAt,
    BigDecimal entryFeeEur,
    String prizeDescription,
    int maxCapacity,
    int registeredCount,
    TournamentRestrictionsDto restrictions,
    Integer currentRoundNumber,
    int totalRounds,
    Instant createdAt
) {}


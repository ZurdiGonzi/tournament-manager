package com.tournament.app.application.dto.tournament;

import com.tournament.app.domain.model.TournamentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Resumen de torneo para listados y calendarios.
 */
public record TournamentSummaryResponse(
    UUID id,
    UUID storeId,
    String storeName,
    String name,
    TournamentStatus status,
    Instant scheduledAt,
    BigDecimal entryFeeEur,
    String prizeDescription,
    int registeredCount,
    int maxCapacity,
    TournamentRestrictionsDto restrictions
) {}


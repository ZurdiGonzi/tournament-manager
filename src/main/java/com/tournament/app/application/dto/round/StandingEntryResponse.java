package com.tournament.app.application.dto.round;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entrada individual en la tabla de clasificación de un torneo.
 */
public record StandingEntryResponse(
    int rank,
    UUID userId,
    String username,
    int matchPoints,
    String matchRecord,
    BigDecimal omwPercentage,
    BigDecimal gwPercentage
) {}


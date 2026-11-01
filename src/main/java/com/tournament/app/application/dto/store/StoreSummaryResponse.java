package com.tournament.app.application.dto.store;

import java.util.UUID;

/**
 * Resumen de tienda para listados y filtros.
 */
public record StoreSummaryResponse(
    UUID id,
    String name,
    String ownerUsername,
    int totalTournaments
) {}


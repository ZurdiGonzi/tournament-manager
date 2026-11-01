package com.tournament.app.application.dto.store;

import com.tournament.app.application.dto.auth.UserSummaryResponse;
import java.time.Instant;
import java.util.UUID;

/**
 * Detalle completo de una tienda, incluyendo propietario y métricas básicas.
 */
public record StoreDetailResponse(
    UUID id,
    String name,
    String description,
    String address,
    UserSummaryResponse owner,
    int totalTournaments,
    int totalMembers,
    Instant createdAt
) {}


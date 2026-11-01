package com.tournament.app.application.dto.auth;

import com.tournament.app.domain.model.UserRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Resumen seguro y público de un usuario.
 */
public record UserSummaryResponse(
    UUID id,
    String username,
    String email,
    Set<UserRole> roles,
    Instant createdAt
) {
    public UserSummaryResponse {
        if (roles == null) {
            roles = Set.of();
        }
    }
}


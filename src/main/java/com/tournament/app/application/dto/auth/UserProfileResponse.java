package com.tournament.app.application.dto.auth;

import com.tournament.app.domain.model.UserRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Perfil detallado de un usuario en la plataforma.
 */
public record UserProfileResponse(
    UUID id,
    String username,
    String email,
    Set<UserRole> roles,
    int totalDecks,
    int totalTournamentsPlayed,
    Instant createdAt
) {
    public UserProfileResponse {
        if (roles == null) {
            roles = Set.of();
        }
    }
}


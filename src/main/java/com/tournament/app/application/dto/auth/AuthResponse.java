package com.tournament.app.application.dto.auth;

/**
 * Respuesta de autenticación exitosa con token de acceso JWT.
 */
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    UserSummaryResponse user
) {
    public AuthResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
}


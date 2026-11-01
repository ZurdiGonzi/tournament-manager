package com.tournament.app.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterUserRequest(
            @NotBlank @Size(max = 60) String username,
            @NotBlank @Size(min = 6, max = 100) String password,
            String role
    ) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record AuthTokenResponse(
            UUID userId,
            String username,
            String role,
            String accessToken,
            long expiresInSeconds
    ) {
    }
}

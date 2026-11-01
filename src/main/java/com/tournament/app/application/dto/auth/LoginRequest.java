package com.tournament.app.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de autenticación de usuario.
 */
public record LoginRequest(
    @NotBlank(message = "El usuario o email es obligatorio")
    String usernameOrEmail,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}


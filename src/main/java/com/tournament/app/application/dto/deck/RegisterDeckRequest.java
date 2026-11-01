package com.tournament.app.application.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de registro de un nuevo mazo vinculando una URL de Moxfield.
 */
public record RegisterDeckRequest(
    @NotBlank(message = "El nombre del mazo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,

    @NotBlank(message = "La URL de Moxfield es obligatoria")
    @Pattern(
        regexp = "^https://(www\\.)?moxfield\\.com/decks/[a-zA-Z0-9_-]+$",
        message = "La URL debe ser un enlace válido a un mazo público de Moxfield"
    )
    String moxfieldUrl
) {}


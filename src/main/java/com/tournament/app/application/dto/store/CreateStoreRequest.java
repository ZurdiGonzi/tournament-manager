package com.tournament.app.application.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de creación de una nueva tienda física o comunidad.
 */
public record CreateStoreRequest(
    @NotBlank(message = "El nombre de la tienda es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    String description,

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    String address
) {}


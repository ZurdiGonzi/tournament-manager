package com.tournament.app.application.dto.common;

/**
 * Detalle de error para un campo específico de una solicitud.
 */
public record FieldErrorDto(
    String field,
    Object rejectedValue,
    String message
) {}


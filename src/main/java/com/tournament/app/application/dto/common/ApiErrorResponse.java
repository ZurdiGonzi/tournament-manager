package com.tournament.app.application.dto.common;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Estructura estándar de error HTTP según RFC 7807 (Problem Details).
 */
public record ApiErrorResponse(
    URI type,
    String title,
    int status,
    String detail,
    URI instance,
    Instant timestamp,
    List<FieldErrorDto> invalidParams
) {
    public ApiErrorResponse {
        if (invalidParams == null) {
            invalidParams = List.of();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}


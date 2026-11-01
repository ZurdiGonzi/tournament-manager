package com.tournament.app.application.dto.tournament;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Solicitud de creación de un nuevo torneo por el dueño de la tienda.
 */
public record CreateTournamentRequest(
    @NotNull(message = "El identificador de la tienda es obligatorio")
    UUID storeId,

    @NotBlank(message = "El nombre del torneo es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
    String name,

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Future(message = "La fecha y hora de inicio debe ser futura")
    Instant scheduledAt,

    @NotNull(message = "El precio de entrada es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio de entrada no puede ser negativo")
    BigDecimal entryFeeEur,

    @NotBlank(message = "La descripción del premio es obligatoria")
    @Size(max = 500, message = "La descripción del premio no puede superar los 500 caracteres")
    String prizeDescription,

    @NotNull(message = "El aforo máximo es obligatorio")
    @Min(value = 4, message = "El aforo mínimo de un torneo es de 4 jugadores")
    @Max(value = 256, message = "El aforo máximo no puede superar 256 jugadores")
    Integer maxCapacity,

    @NotNull(message = "Las restricciones del torneo son obligatorias")
    @Valid
    TournamentRestrictionsDto restrictions
) {}


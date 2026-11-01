package com.tournament.app.application.dto.tournament;

import com.tournament.app.domain.model.TournamentFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Restricciones competitivas de formato y presupuesto asociadas a un torneo.
 */
public record TournamentRestrictionsDto(
    @NotNull(message = "El formato de juego es obligatorio")
    TournamentFormat format,

    @DecimalMin(value = "0.00", message = "El límite de presupuesto no puede ser negativo")
    BigDecimal maxBudgetEur
) {}


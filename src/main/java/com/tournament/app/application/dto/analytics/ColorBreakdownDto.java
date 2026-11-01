package com.tournament.app.application.dto.analytics;

import java.math.BigDecimal;

/**
 * Porcentaje de distribución de un color de Magic en el metajuego.
 */
public record ColorBreakdownDto(
    String colorCode,
    String colorName,
    BigDecimal percentage
) {}


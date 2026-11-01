package com.tournament.app.application.dto.analytics;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Métrica de frecuencia de uso y coste medio de una carta en el metajuego según Scryfall.
 */
public record CardUsageStatDto(
    UUID scryfallId,
    String cardName,
    String manaCost,
    String typeLine,
    int totalCopies,
    BigDecimal deckInclusionRate,
    BigDecimal averagePriceEur
) {}


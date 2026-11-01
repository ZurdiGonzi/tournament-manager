package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record RegistrationDeckCard(
    UUID id,
    UUID registrationId,
    UUID scryfallCardId,
    BoardSection boardSection,
    int quantity,
    BigDecimal unitPriceSnapshot
) {
    public RegistrationDeckCard {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(registrationId, "registrationId must not be null");
        Objects.requireNonNull(scryfallCardId, "scryfallCardId must not be null");
        Objects.requireNonNull(boardSection, "boardSection must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        Objects.requireNonNull(unitPriceSnapshot, "unitPriceSnapshot must not be null");
        if (unitPriceSnapshot.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unitPriceSnapshot must be >= 0");
        }
    }
}

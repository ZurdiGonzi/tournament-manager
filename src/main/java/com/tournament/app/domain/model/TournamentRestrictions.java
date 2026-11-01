package com.tournament.app.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record TournamentRestrictions(
    MagicFormat format,
    BigDecimal maxDeckBudget
) {
    public TournamentRestrictions {
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(maxDeckBudget, "maxDeckBudget must not be null");
        if (maxDeckBudget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("maxDeckBudget must be > 0");
        }
    }
}

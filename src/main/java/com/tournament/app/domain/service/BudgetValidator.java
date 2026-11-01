package com.tournament.app.domain.service;

import com.tournament.app.domain.model.BoardSection;
import com.tournament.app.domain.model.CurrencyCode;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.TournamentRestrictions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pure domain service that validates a deck's format legality and maximum budget
 * against Scryfall card metadata and tournament restrictions.
 * Contains zero framework dependencies (Constitution Principle I).
 */
public class BudgetValidator {

    public record DeckCardSubmission(
        ScryfallCard card,
        BoardSection boardSection,
        int quantity
    ) {
        public DeckCardSubmission {
            Objects.requireNonNull(card, "card must not be null");
            Objects.requireNonNull(boardSection, "boardSection must not be null");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0");
            }
        }
    }

    public record ValidationResult(
        boolean valid,
        BigDecimal calculatedDeckPrice,
        BigDecimal maxAllowedBudget,
        List<String> illegalCardNames,
        List<String> violations
    ) {
        public ValidationResult {
            illegalCardNames = illegalCardNames == null ? List.of() : List.copyOf(illegalCardNames);
            violations = violations == null ? List.of() : List.copyOf(violations);
        }
    }

    public ValidationResult validate(
        List<DeckCardSubmission> deckCards,
        TournamentRestrictions restrictions,
        CurrencyCode storeCurrency
    ) {
        Objects.requireNonNull(deckCards, "deckCards must not be null");
        Objects.requireNonNull(restrictions, "restrictions must not be null");
        CurrencyCode currency = storeCurrency == null ? CurrencyCode.EUR : storeCurrency;

        List<String> illegalCards = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (deckCards.isEmpty()) {
            violations.add("El mazo no contiene cartas.");
            return new ValidationResult(false, totalPrice, restrictions.maxDeckBudget(), illegalCards, violations);
        }

        for (DeckCardSubmission entry : deckCards) {
            ScryfallCard card = entry.card();
            if (!card.isLegalIn(restrictions.format())) {
                illegalCards.add(card.name());
                violations.add(String.format(
                    "La carta '%s' no es legal en el formato %s",
                    card.name(),
                    restrictions.format()
                ));
            }
            BigDecimal unitPrice = card.resolvePriceForCurrency(currency).setScale(2, RoundingMode.HALF_UP);
            BigDecimal linePrice = unitPrice.multiply(BigDecimal.valueOf(entry.quantity()));
            totalPrice = totalPrice.add(linePrice);
        }

        if (totalPrice.compareTo(restrictions.maxDeckBudget()) > 0) {
            violations.add(String.format(
                "El precio calculado del mazo (%s %s) supera el presupuesto máximo permitido (%s %s)",
                totalPrice.toPlainString(),
                currency.name(),
                restrictions.maxDeckBudget().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                currency.name()
            ));
        }

        boolean valid = illegalCards.isEmpty() && violations.isEmpty();
        return new ValidationResult(valid, totalPrice, restrictions.maxDeckBudget(), illegalCards, violations);
    }
}

package com.tournament.app.domain.service;

import com.tournament.app.domain.model.BoardSection;
import com.tournament.app.domain.model.CurrencyCode;
import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.TournamentRestrictions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetValidatorTest {

    private final BudgetValidator validator = new BudgetValidator();

    @Test
    void shouldPassWhenAllCardsAreLegalAndTotalIsWithinMaxDeckBudget() {
        ScryfallCard solRing = new ScryfallCard(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Sol Ring",
            "{1}",
            BigDecimal.ONE,
            "Artifact",
            List.of(),
            List.of(),
            "uncommon",
            Map.of("commander", "legal", "standard", "not_legal"),
            new BigDecimal("1.50"),
            new BigDecimal("1.80"),
            "https://cards.scryfall.io/normal/sol_ring.jpg",
            Instant.now()
        );

        BudgetValidator.ValidationResult result = validator.validate(
            List.of(new BudgetValidator.DeckCardSubmission(solRing, BoardSection.MAINBOARD, 1)),
            new TournamentRestrictions(MagicFormat.COMMANDER, new BigDecimal("100.00")),
            CurrencyCode.EUR
        );

        assertThat(result.valid()).isTrue();
        assertThat(result.calculatedDeckPrice()).isEqualByComparingTo("1.50");
        assertThat(result.illegalCardNames()).isEmpty();
        assertThat(result.violations()).isEmpty();
    }

    @Test
    void shouldFailWhenCardIsIllegalOrBudgetExceeded() {
        ScryfallCard manaCrypt = new ScryfallCard(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Mana Crypt",
            "{0}",
            BigDecimal.ZERO,
            "Artifact",
            List.of(),
            List.of(),
            "mythic",
            Map.of("commander", "banned", "standard", "not_legal"),
            new BigDecimal("140.00"),
            new BigDecimal("160.00"),
            null,
            Instant.now()
        );

        BudgetValidator.ValidationResult result = validator.validate(
            List.of(new BudgetValidator.DeckCardSubmission(manaCrypt, BoardSection.MAINBOARD, 1)),
            new TournamentRestrictions(MagicFormat.COMMANDER, new BigDecimal("80.00")),
            CurrencyCode.EUR
        );

        assertThat(result.valid()).isFalse();
        assertThat(result.calculatedDeckPrice()).isEqualByComparingTo("140.00");
        assertThat(result.illegalCardNames()).containsExactly("Mana Crypt");
        assertThat(result.violations()).hasSize(2);
    }
}

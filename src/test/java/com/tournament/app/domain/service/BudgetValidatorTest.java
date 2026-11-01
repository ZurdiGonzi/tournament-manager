package com.tournament.app.domain.service;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.service.BudgetValidator.DeckCardInput;
import com.tournament.app.domain.service.BudgetValidator.ValidatedDeckResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BudgetValidatorTest {

    private final BudgetValidator validator = new BudgetValidator();

    @Test
    void shouldValidateCommanderDeckWithScryfallCardsWithinBudget() {
        ScryfallCard magda = new ScryfallCard(
                UUID.randomUUID(), UUID.randomUUID(), "Magda, Brazen Outlaw", "{1}{R}",
                new BigDecimal("2.0"), "Legendary Creature — Dwarf Berserker",
                List.of("R"), List.of("R"), "rare", Map.of("commander", "legal"),
                new BigDecimal("5.00"), new BigDecimal("5.50"), null, Instant.now()
        );
        ScryfallCard mountain = new ScryfallCard(
                UUID.randomUUID(), UUID.randomUUID(), "Mountain", "",
                BigDecimal.ZERO, "Basic Land — Mountain",
                List.of(), List.of("R"), "common", Map.of("commander", "legal"),
                new BigDecimal("0.20"), new BigDecimal("0.25"), null, Instant.now()
        );

        ValidatedDeckResult result = validator.validateAndBuildSnapshot(
                "magda123",
                "https://www.moxfield.com/decks/magda123",
                "Magda Dwarves",
                "COMMANDER",
                new BigDecimal("50.00"),
                "EUR",
                List.of(
                        new DeckCardInput(magda, 1, "COMMANDER"),
                        new DeckCardInput(mountain, 99, "MAINBOARD")
                )
        );

        assertThat(result.commanderName()).isEqualTo("Magda, Brazen Outlaw");
        assertThat(result.colorIdentity()).containsExactly("R");
        assertThat(result.totalPrice()).isEqualByComparingTo("24.80");
    }

    @Test
    void shouldRejectDeckWhenScryfallPriceExceedsMaxDeckBudget() {
        ScryfallCard expensiveCard = new ScryfallCard(
                UUID.randomUUID(), UUID.randomUUID(), "Mana Crypt", "{0}",
                BigDecimal.ZERO, "Artifact",
                List.of(), List.of(), "mythic", Map.of("standard", "legal"),
                new BigDecimal("180.00"), new BigDecimal("200.00"), null, Instant.now()
        );
        ScryfallCard island = new ScryfallCard(
                UUID.randomUUID(), UUID.randomUUID(), "Island", "",
                BigDecimal.ZERO, "Basic Land — Island",
                List.of(), List.of("U"), "common", Map.of("standard", "legal"),
                new BigDecimal("0.10"), new BigDecimal("0.10"), null, Instant.now()
        );

        assertThatThrownBy(() -> validator.validateAndBuildSnapshot(
                "std1", "https://www.moxfield.com/decks/std1", "Expensive Deck",
                "STANDARD", new BigDecimal("100.00"), "EUR",
                List.of(
                        new DeckCardInput(expensiveCard, 1, "MAINBOARD"),
                        new DeckCardInput(island, 59, "MAINBOARD")
                )
        )).isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("exceeds tournament max budget");
    }
}

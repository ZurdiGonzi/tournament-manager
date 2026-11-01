package com.tournament.app.application.dto.deck;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeckDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("RegisterDeckRequest debe validar el patrón oficial de URL de Moxfield")
    void shouldValidateMoxfieldUrl() {
        var invalidDomain = new RegisterDeckRequest("Mazo 1", "https://archidekt.com/decks/123");
        var violationsDomain = validator.validate(invalidDomain);
        assertTrue(violationsDomain.stream().anyMatch(v -> v.getPropertyPath().toString().equals("moxfieldUrl")));

        var invalidProtocol = new RegisterDeckRequest("Mazo 2", "http://moxfield.com/decks/123");
        var violationsProtocol = validator.validate(invalidProtocol);
        assertTrue(violationsProtocol.stream().anyMatch(v -> v.getPropertyPath().toString().equals("moxfieldUrl")));

        var valid = new RegisterDeckRequest("Golgari Midrange", "https://www.moxfield.com/decks/k8Jd_7La0k");
        assertTrue(validator.validate(valid).isEmpty());
    }

    @Test
    @DisplayName("DeckCardDto debe requerir una cantidad mínima de 1 copia")
    void shouldValidateCardQuantity() {
        var invalidCard = new DeckCardDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lightning Bolt",
            0,
            "{R}",
            BigDecimal.ONE,
            "Instant",
            List.of("R"),
            List.of("R"),
            new BigDecimal("1.20"),
            false
        );
        var violations = validator.validate(invalidCard);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));

        var validCard = new DeckCardDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Lightning Bolt",
            4,
            "{R}",
            BigDecimal.ONE,
            "Instant",
            List.of("R"),
            List.of("R"),
            new BigDecimal("1.20"),
            false
        );
        assertTrue(validator.validate(validCard).isEmpty());
    }
}


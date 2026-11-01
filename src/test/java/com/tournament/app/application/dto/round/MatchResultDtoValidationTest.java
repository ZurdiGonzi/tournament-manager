package com.tournament.app.application.dto.round;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MatchResultDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("ReportMatchResultRequest debe validar límites de victorias (0-2) y empates (0-3)")
    void shouldValidateMatchResultScoreBounds() {
        var negativeWins = new ReportMatchResultRequest(UUID.randomUUID(), -1, 1, 0);
        assertTrue(validator.validate(negativeWins).stream().anyMatch(v -> v.getPropertyPath().toString().equals("player1Wins")));

        var excessiveWins = new ReportMatchResultRequest(UUID.randomUUID(), 3, 0, 0);
        assertTrue(validator.validate(excessiveWins).stream().anyMatch(v -> v.getPropertyPath().toString().equals("player1Wins")));

        var excessiveDraws = new ReportMatchResultRequest(UUID.randomUUID(), 1, 1, 4);
        assertTrue(validator.validate(excessiveDraws).stream().anyMatch(v -> v.getPropertyPath().toString().equals("draws")));

        var validBestOfThree = new ReportMatchResultRequest(UUID.randomUUID(), 2, 1, 0);
        assertTrue(validator.validate(validBestOfThree).isEmpty());
    }
}


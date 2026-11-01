package com.tournament.app.application.dto.tournament;

import com.tournament.app.domain.model.TournamentFormat;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TournamentDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("TournamentRestrictionsDto debe rechazar presupuestos negativos")
    void shouldValidateRestrictions() {
        var negativeBudget = new TournamentRestrictionsDto(TournamentFormat.COMMANDER, new BigDecimal("-1.00"));
        var violations = validator.validate(negativeBudget);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("maxBudgetEur")));

        var valid = new TournamentRestrictionsDto(TournamentFormat.COMMANDER, new BigDecimal("75.00"));
        assertTrue(validator.validate(valid).isEmpty());
    }

    @Test
    @DisplayName("CreateTournamentRequest debe exigir fecha futura, precio no negativo y aforo entre 4 y 256")
    void shouldValidateCreateTournament() {
        var pastDate = Instant.now().minus(1, ChronoUnit.DAYS);
        var invalid = new CreateTournamentRequest(
            UUID.randomUUID(),
            "Torneo Pasado",
            pastDate,
            new BigDecimal("-5.00"),
            "Premio",
            2, // Menor que 4
            new TournamentRestrictionsDto(TournamentFormat.STANDARD, null)
        );

        var violations = validator.validate(invalid);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("scheduledAt")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("entryFeeEur")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("maxCapacity")));

        var futureDate = Instant.now().plus(7, ChronoUnit.DAYS);
        var valid = new CreateTournamentRequest(
            UUID.randomUUID(),
            "FNM Commander",
            futureDate,
            new BigDecimal("5.00"),
            "Promos al top 4",
            16,
            new TournamentRestrictionsDto(TournamentFormat.COMMANDER, new BigDecimal("100.00"))
        );
        assertTrue(validator.validate(valid).isEmpty());
    }
}


package com.tournament.app.application.dto.store;

import com.tournament.app.application.dto.auth.UserSummaryResponse;
import com.tournament.app.domain.model.UserRole;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StoreDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("CreateStoreRequest debe requerir nombre no vacío y entre 2 y 100 caracteres")
    void shouldValidateStoreName() {
        var blank = new CreateStoreRequest(" ", "desc", "addr");
        var blankViolations = validator.validate(blank);
        assertTrue(blankViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));

        var singleChar = new CreateStoreRequest("A", "desc", "addr");
        var shortViolations = validator.validate(singleChar);
        assertTrue(shortViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));

        var valid = new CreateStoreRequest("Magic Store Valencia", "Tienda oficial", "C/ Colon 10");
        assertTrue(validator.validate(valid).isEmpty());
    }

    @Test
    @DisplayName("StoreDetailResponse debe encapsular al propietario y los totales correctamente")
    void shouldInstantiateStoreDetail() {
        var owner = new UserSummaryResponse(UUID.randomUUID(), "ownerUser", "owner@store.com", Set.of(UserRole.ADMIN), Instant.now());
        var store = new StoreDetailResponse(
            UUID.randomUUID(),
            "MTG Hub",
            "Club de juego",
            "Av. Mayor 4",
            owner,
            12,
            45,
            Instant.now()
        );

        assertEquals("MTG Hub", store.name());
        assertEquals("ownerUser", store.owner().username());
        assertEquals(12, store.totalTournaments());
        assertEquals(45, store.totalMembers());
    }
}


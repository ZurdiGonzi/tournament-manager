package com.tournament.app.application.dto.auth;

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

class AuthDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("RegisterRequest debe asignar UserRole.PLAYER por defecto si role es nulo")
    void shouldDefaultRoleToPlayerWhenNull() {
        var request = new RegisterRequest("gonzalo_mtg", "gonzalo@example.com", "Password123!", null);
        assertEquals(UserRole.PLAYER, request.role());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("RegisterRequest debe fallar si username tiene formato o longitud inválida")
    void shouldFailWhenUsernameIsInvalid() {
        var invalidShort = new RegisterRequest("ab", "valid@email.com", "Password123!", UserRole.PLAYER);
        var violationsShort = validator.validate(invalidShort);
        assertTrue(violationsShort.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));

        var invalidChars = new RegisterRequest("user@name#bad", "valid@email.com", "Password123!", UserRole.PLAYER);
        var violationsChars = validator.validate(invalidChars);
        assertTrue(violationsChars.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("RegisterRequest debe fallar si email no es válido o password es muy corta")
    void shouldFailWhenEmailOrPasswordInvalid() {
        var invalid = new RegisterRequest("valid_user", "not-an-email", "short", UserRole.PLAYER);
        var violations = validator.validate(invalid);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("LoginRequest debe requerir identificador y contraseña no vacíos")
    void shouldValidateLoginRequest() {
        var empty = new LoginRequest("", "   ");
        var violations = validator.validate(empty);
        assertEquals(2, violations.size());

        var valid = new LoginRequest("gonzalo", "secretPass");
        assertTrue(validator.validate(valid).isEmpty());
    }

    @Test
    @DisplayName("AuthResponse debe asignar 'Bearer' por defecto como tokenType")
    void shouldDefaultTokenTypeInAuthResponse() {
        var user = new UserSummaryResponse(UUID.randomUUID(), "gonzalo", "g@ex.com", Set.of(UserRole.PLAYER), Instant.now());
        var auth = new AuthResponse("token123", null, 3600, user);
        assertEquals("Bearer", auth.tokenType());
    }
}


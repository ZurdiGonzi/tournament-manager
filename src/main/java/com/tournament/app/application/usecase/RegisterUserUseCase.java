package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AuthDtos.AuthTokenResponse;
import com.tournament.app.application.dto.AuthDtos.RegisterUserRequest;
import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.User;
import com.tournament.app.domain.repository.UserRepository;
import com.tournament.app.infrastructure.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthTokenResponse execute(RegisterUserRequest request) {
        String normalizedUsername = request.username().trim();
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new DomainValidationException("Username '" + normalizedUsername + "' is already registered");
        }
        String role = (request.role() == null || request.role().isBlank())
                ? "PLAYER"
                : request.role().trim().toUpperCase();
        User saved = userRepository.save(new User(
                UUID.randomUUID(),
                normalizedUsername,
                passwordEncoder.encode(request.password()),
                role,
                Instant.now()
        ));
        String token = jwtTokenProvider.generateToken(saved.id(), saved.username(), saved.role());
        return new AuthTokenResponse(
                saved.id(),
                saved.username(),
                saved.role(),
                token,
                jwtTokenProvider.getExpirationSeconds()
        );
    }
}

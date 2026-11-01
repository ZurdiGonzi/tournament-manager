package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AuthDtos.AuthTokenResponse;
import com.tournament.app.application.dto.AuthDtos.LoginRequest;
import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.User;
import com.tournament.app.domain.repository.UserRepository;
import com.tournament.app.infrastructure.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse execute(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new DomainValidationException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new DomainValidationException("Invalid username or password");
        }
        String token = jwtTokenProvider.generateToken(user.id(), user.username(), user.role());
        return new AuthTokenResponse(
                user.id(),
                user.username(),
                user.role(),
                token,
                jwtTokenProvider.getExpirationSeconds()
        );
    }
}

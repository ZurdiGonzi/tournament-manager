package com.tournament.app.presentation;

import com.tournament.app.application.dto.AuthDtos.AuthTokenResponse;
import com.tournament.app.application.dto.AuthDtos.LoginRequest;
import com.tournament.app.application.dto.AuthDtos.RegisterUserRequest;
import com.tournament.app.application.usecase.AuthenticateUserUseCase;
import com.tournament.app.application.usecase.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthTokenResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return registerUserUseCase.execute(request);
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticateUserUseCase.execute(request);
    }
}

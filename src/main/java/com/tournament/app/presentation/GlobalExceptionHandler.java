package com.tournament.app.presentation;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.DomainExceptions.IllegalPairingException;
import com.tournament.app.domain.model.DomainExceptions.InvalidTournamentStateException;
import com.tournament.app.domain.model.DomainExceptions.ResourceNotFoundException;
import com.tournament.app.domain.model.DomainExceptions.UnauthorizedStoreActionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainValidationException.class)
    public ProblemDetail handleDomainValidation(DomainValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Domain Validation Error");
        problem.setType(URI.create("https://tournament-manager.app/errors/domain-validation"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler({InvalidTournamentStateException.class, IllegalPairingException.class})
    public ProblemDetail handleTournamentState(RuntimeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Tournament Rule Conflict");
        problem.setType(URI.create("https://tournament-manager.app/errors/tournament-conflict"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://tournament-manager.app/errors/not-found"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(UnauthorizedStoreActionException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedStoreActionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Forbidden Action");
        problem.setType(URI.create("https://tournament-manager.app/errors/forbidden"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);
        problem.setTitle("Invalid Request Payload");
        problem.setType(URI.create("https://tournament-manager.app/errors/invalid-payload"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}

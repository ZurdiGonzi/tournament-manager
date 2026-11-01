package com.tournament.app.domain.model;

public final class DomainExceptions {

    private DomainExceptions() {
    }

    public static class DomainValidationException extends RuntimeException {
        public DomainValidationException(String message) {
            super(message);
        }
    }

    public static class InvalidTournamentStateException extends RuntimeException {
        public InvalidTournamentStateException(String message) {
            super(message);
        }
    }

    public static class IllegalPairingException extends RuntimeException {
        public IllegalPairingException(String message) {
            super(message);
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedStoreActionException extends RuntimeException {
        public UnauthorizedStoreActionException(String message) {
            super(message);
        }
    }
}

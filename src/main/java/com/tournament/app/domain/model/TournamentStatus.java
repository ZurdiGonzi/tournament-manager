package com.tournament.app.domain.model;

public enum TournamentStatus {
    DRAFT,
    REGISTRATION_OPEN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(TournamentStatus next) {
        return switch (this) {
            case DRAFT -> next == REGISTRATION_OPEN || next == CANCELLED;
            case REGISTRATION_OPEN -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}

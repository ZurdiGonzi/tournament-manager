package com.tournament.app.domain.model;

public enum MagicFormat {
    STANDARD,
    COMMANDER;

    public String scryfallKey() {
        return name().toLowerCase();
    }
}

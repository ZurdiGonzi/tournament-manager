package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record UserProfileSummary(
    UUID id,
    String username,
    UserRole role,
    Instant createdAt,
    List<Store> memberStores,
    long registeredDeckCount,
    List<UserLeagueParticipation> leagues
) {
    public UserProfileSummary {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        memberStores = memberStores == null ? List.of() : List.copyOf(memberStores);
        leagues = leagues == null ? List.of() : List.copyOf(leagues);
    }

    public record UserLeagueParticipation(
        UUID leagueId,
        String leagueName,
        UUID storeId,
        MagicFormat format,
        LeagueStatus status,
        int totalPoints,
        int tournamentsPlayed
    ) {}
}

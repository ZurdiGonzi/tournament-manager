package com.tournament.app.domain.model;

import java.util.List;
import java.util.Objects;

public record StoreDetailSummary(
    Store store,
    String ownerUsername,
    long totalTournaments,
    long totalMembers,
    List<StoreMember> members
) {
    public StoreDetailSummary {
        Objects.requireNonNull(store, "store must not be null");
        members = members == null ? List.of() : List.copyOf(members);
    }
}

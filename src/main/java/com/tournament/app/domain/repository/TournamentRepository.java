package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Tournament;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentRepository {
    Tournament save(Tournament tournament);
    Optional<Tournament> findById(UUID id);
    List<Tournament> findByStoreId(UUID storeId);
    List<Tournament> findByLeagueId(UUID leagueId);
    boolean existsByTemplateIdAndScheduledAt(UUID templateId, Instant scheduledAt);
}

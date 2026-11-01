package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentDetailSummary;
import com.tournament.app.domain.model.TournamentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentRepository {

    Optional<Tournament> findById(UUID id);

    List<Tournament> findByStoreIdOrderByScheduledAtDesc(UUID storeId);

    List<Tournament> findByStoreIdAndStatus(UUID storeId, TournamentStatus status);

    List<Tournament> findByLeagueIdOrderByScheduledAtDesc(UUID leagueId);

    boolean existsByTemplateIdAndScheduledAt(UUID templateId, Instant scheduledAt);

    long countByStoreId(UUID storeId);

    Optional<TournamentDetailSummary> findTournamentDetailById(UUID tournamentId);

    Tournament save(Tournament tournament);
}

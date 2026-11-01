package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.TournamentRegistration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository {
    TournamentRegistration save(TournamentRegistration registration);
    void saveAll(List<TournamentRegistration> registrations);
    Optional<TournamentRegistration> findByTournamentIdAndUserId(UUID tournamentId, UUID userId);
    List<TournamentRegistration> findByTournamentId(UUID tournamentId);
    List<TournamentRegistration> findByLeagueId(UUID leagueId);
    List<TournamentRegistration> findByUserIdAndDeckId(UUID userId, UUID deckId);
}

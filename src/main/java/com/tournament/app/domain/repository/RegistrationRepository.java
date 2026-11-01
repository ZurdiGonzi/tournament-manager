package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.RegistrationDeckCard;
import com.tournament.app.domain.model.TournamentRegistration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository {

    Optional<TournamentRegistration> findById(UUID id);

    Optional<TournamentRegistration> findByTournamentIdAndUserId(UUID tournamentId, UUID userId);

    List<TournamentRegistration> findByTournamentIdOrderByRegisteredAtAsc(UUID tournamentId);

    List<TournamentRegistration> findStandingsByTournamentId(UUID tournamentId);

    List<TournamentRegistration> findByUserId(UUID userId);

    List<RegistrationDeckCard> findDeckCardsByRegistrationId(UUID registrationId);

    List<RegistrationDeckCard> findDeckCardsByTournamentId(UUID tournamentId);

    long countByTournamentId(UUID tournamentId);

    TournamentRegistration save(TournamentRegistration registration, List<RegistrationDeckCard> deckCards);
}

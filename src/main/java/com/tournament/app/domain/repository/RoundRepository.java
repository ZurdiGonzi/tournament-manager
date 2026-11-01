package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.Round;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoundRepository {

    Optional<Round> findById(UUID roundId);

    Optional<Round> findByTournamentIdAndRoundNumber(UUID tournamentId, int roundNumber);

    List<Round> findByTournamentIdOrderByRoundNumberAsc(UUID tournamentId);

    Round save(Round round, List<Match> matches);
}

package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository {

    Optional<Match> findById(UUID matchId);

    List<Match> findByRoundIdOrderByTableNumberAsc(UUID roundId);

    List<Match> findByTournamentId(UUID tournamentId);

    Match save(Match match);
}

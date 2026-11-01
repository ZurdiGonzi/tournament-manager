package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.Round;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository {
    Round saveRound(Round round);
    List<Match> saveMatches(List<Match> matches);
    Match saveMatch(Match match);
    Optional<Match> findMatchById(UUID matchId);
    Optional<Round> findRoundById(UUID roundId);
    List<Match> findMatchesByTournamentId(UUID tournamentId);
    List<Match> findMatchesByLeagueId(UUID leagueId);
    List<Match> findMatchesByDeckId(UUID deckId);
    List<MatchParticipant> findParticipantsByTournamentId(UUID tournamentId);
}

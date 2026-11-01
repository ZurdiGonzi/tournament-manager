package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueStanding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueRepository {
    League save(League league);
    Optional<League> findById(UUID id);
    List<League> findByStoreId(UUID storeId);
    List<LeagueStanding> findStandingsByLeagueId(UUID leagueId);
    void saveStandings(List<LeagueStanding> standings);
}

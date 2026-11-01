package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueDetailSummary;
import com.tournament.app.domain.model.LeagueLeaderboardEntry;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.model.LeagueStatus;
import com.tournament.app.domain.model.MagicFormat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueRepository {

    Optional<League> findById(UUID id);

    List<League> findByStoreId(UUID storeId);

    List<League> findByStoreIdAndStatus(UUID storeId, LeagueStatus status);

    List<League> findByStoreIdAndFormat(UUID storeId, MagicFormat format);

    List<LeagueStanding> findStandingsByLeagueId(UUID leagueId);

    List<LeagueStanding> findStandingsByUserId(UUID userId);

    List<LeagueLeaderboardEntry> findLeaderboardByLeagueId(UUID leagueId);

    Optional<LeagueDetailSummary> findLeagueDetailById(UUID leagueId);

    League save(League league);

    LeagueStanding saveStanding(LeagueStanding standing);
}

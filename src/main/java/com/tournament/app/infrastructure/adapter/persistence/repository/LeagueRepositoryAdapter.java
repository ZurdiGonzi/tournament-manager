package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueDetailSummary;
import com.tournament.app.domain.model.LeagueLeaderboardEntry;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.model.LeagueStatus;
import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.repository.LeagueRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueStandingEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class LeagueRepositoryAdapter implements LeagueRepository {

    private final SpringDataLeagueJpaRepository leagueJpaRepository;
    private final SpringDataLeagueStandingJpaRepository leagueStandingJpaRepository;
    private final SpringDataLeagueLeaderboardJpaRepository leagueLeaderboardJpaRepository;
    private final SpringDataTournamentJpaRepository tournamentJpaRepository;
    private final PersistenceEntityMapper mapper;

    public LeagueRepositoryAdapter(
        SpringDataLeagueJpaRepository leagueJpaRepository,
        SpringDataLeagueStandingJpaRepository leagueStandingJpaRepository,
        SpringDataLeagueLeaderboardJpaRepository leagueLeaderboardJpaRepository,
        SpringDataTournamentJpaRepository tournamentJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.leagueJpaRepository = leagueJpaRepository;
        this.leagueStandingJpaRepository = leagueStandingJpaRepository;
        this.leagueLeaderboardJpaRepository = leagueLeaderboardJpaRepository;
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<League> findById(UUID id) {
        return leagueJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<League> findByStoreId(UUID storeId) {
        return leagueJpaRepository.findByStoreIdOrderByStartDateDesc(storeId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<League> findByStoreIdAndStatus(UUID storeId, LeagueStatus status) {
        return leagueJpaRepository.findByStoreIdAndStatusOrderByStartDateDesc(storeId, status).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<League> findByStoreIdAndFormat(UUID storeId, MagicFormat format) {
        return leagueJpaRepository.findByStoreIdAndFormatOrderByStartDateDesc(storeId, format).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<LeagueStanding> findStandingsByLeagueId(UUID leagueId) {
        return leagueStandingJpaRepository.findByIdLeagueIdOrderByTotalPointsDescTournamentsPlayedDesc(leagueId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<LeagueStanding> findStandingsByUserId(UUID userId) {
        return leagueStandingJpaRepository.findByIdUserIdOrderByUpdatedAtDesc(userId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<LeagueLeaderboardEntry> findLeaderboardByLeagueId(UUID leagueId) {
        return leagueLeaderboardJpaRepository.findByIdLeagueIdOrderByCurrentRankAsc(leagueId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<LeagueDetailSummary> findLeagueDetailById(UUID leagueId) {
        return leagueJpaRepository.findById(leagueId).map(entity -> {
            League league = mapper.toDomain(entity);
            List<Tournament> tournaments = tournamentJpaRepository.findByLeagueIdOrderByScheduledAtDesc(leagueId).stream()
                .map(mapper::toDomain)
                .toList();
            List<LeagueLeaderboardEntry> leaderboard = findLeaderboardByLeagueId(leagueId);
            return new LeagueDetailSummary(league, tournaments, leaderboard);
        });
    }

    @Override
    @Transactional
    public League save(League league) {
        LeagueEntity saved = leagueJpaRepository.save(mapper.toEntity(league));
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public LeagueStanding saveStanding(LeagueStanding standing) {
        LeagueStandingEntity saved = leagueStandingJpaRepository.save(mapper.toEntity(standing));
        return mapper.toDomain(saved);
    }
}

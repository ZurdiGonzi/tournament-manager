package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentDetailSummary;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.model.TournamentStatus;
import com.tournament.app.domain.repository.TournamentRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class TournamentRepositoryAdapter implements TournamentRepository {

    private final SpringDataTournamentJpaRepository tournamentJpaRepository;
    private final SpringDataRegistrationJpaRepository registrationJpaRepository;
    private final SpringDataRoundJpaRepository roundJpaRepository;
    private final SpringDataMatchJpaRepository matchJpaRepository;
    private final PersistenceEntityMapper mapper;

    public TournamentRepositoryAdapter(
        SpringDataTournamentJpaRepository tournamentJpaRepository,
        SpringDataRegistrationJpaRepository registrationJpaRepository,
        SpringDataRoundJpaRepository roundJpaRepository,
        SpringDataMatchJpaRepository matchJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.registrationJpaRepository = registrationJpaRepository;
        this.roundJpaRepository = roundJpaRepository;
        this.matchJpaRepository = matchJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Tournament> findById(UUID id) {
        return tournamentJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Tournament> findByStoreIdOrderByScheduledAtDesc(UUID storeId) {
        return tournamentJpaRepository.findByStoreIdOrderByScheduledAtDesc(storeId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Tournament> findByStoreIdAndStatus(UUID storeId, TournamentStatus status) {
        return tournamentJpaRepository.findByStoreIdAndStatusOrderByScheduledAtDesc(storeId, status).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Tournament> findByLeagueIdOrderByScheduledAtDesc(UUID leagueId) {
        return tournamentJpaRepository.findByLeagueIdOrderByScheduledAtDesc(leagueId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByTemplateIdAndScheduledAt(UUID templateId, Instant scheduledAt) {
        return tournamentJpaRepository.existsByTemplateIdAndScheduledAt(templateId, scheduledAt);
    }

    @Override
    public long countByStoreId(UUID storeId) {
        return tournamentJpaRepository.countByStoreId(storeId);
    }

    @Override
    public Optional<TournamentDetailSummary> findTournamentDetailById(UUID tournamentId) {
        return tournamentJpaRepository.findById(tournamentId).map(entity -> {
            Tournament tournament = mapper.toDomain(entity);
            List<TournamentRegistration> standings = registrationJpaRepository
                .findByTournamentIdOrderByMatchPointsDescOmwPercentageDescRegisteredAtAsc(tournamentId)
                .stream()
                .map(mapper::toDomain)
                .toList();

            List<Round> rounds = roundJpaRepository.findByTournamentIdOrderByRoundNumberAsc(tournamentId).stream()
                .map(mapper::toDomain)
                .toList();

            Map<UUID, List<Match>> matchesByRound = matchJpaRepository.findByTournamentId(tournamentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.groupingBy(Match::roundId));

            List<TournamentDetailSummary.RoundWithMatches> roundSummaries = new ArrayList<>();
            for (Round round : rounds) {
                roundSummaries.add(new TournamentDetailSummary.RoundWithMatches(
                    round,
                    matchesByRound.getOrDefault(round.id(), List.of())
                ));
            }

            return new TournamentDetailSummary(tournament, standings.size(), standings, roundSummaries);
        });
    }

    @Override
    @Transactional
    public Tournament save(Tournament tournament) {
        TournamentEntity saved = tournamentJpaRepository.save(mapper.toEntity(tournament));
        return mapper.toDomain(saved);
    }
}

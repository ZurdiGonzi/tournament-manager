package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.repository.RoundRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.MatchEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import com.tournament.app.infrastructure.adapter.persistence.jpa.RoundEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class RoundRepositoryAdapter implements RoundRepository {

    private final SpringDataRoundJpaRepository roundJpaRepository;
    private final SpringDataMatchJpaRepository matchJpaRepository;
    private final PersistenceEntityMapper mapper;

    public RoundRepositoryAdapter(
        SpringDataRoundJpaRepository roundJpaRepository,
        SpringDataMatchJpaRepository matchJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.roundJpaRepository = roundJpaRepository;
        this.matchJpaRepository = matchJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Round> findById(UUID roundId) {
        return roundJpaRepository.findById(roundId).map(mapper::toDomain);
    }

    @Override
    public Optional<Round> findByTournamentIdAndRoundNumber(UUID tournamentId, int roundNumber) {
        return roundJpaRepository.findByTournamentIdAndRoundNumber(tournamentId, roundNumber).map(mapper::toDomain);
    }

    @Override
    public List<Round> findByTournamentIdOrderByRoundNumberAsc(UUID tournamentId) {
        return roundJpaRepository.findByTournamentIdOrderByRoundNumberAsc(tournamentId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public Round save(Round round, List<Match> matches) {
        RoundEntity saved = roundJpaRepository.save(mapper.toEntity(round));
        if (matches != null && !matches.isEmpty()) {
            List<MatchEntity> matchEntities = matches.stream()
                .map(mapper::toEntity)
                .toList();
            matchJpaRepository.saveAll(matchEntities);
        }
        return mapper.toDomain(saved);
    }
}

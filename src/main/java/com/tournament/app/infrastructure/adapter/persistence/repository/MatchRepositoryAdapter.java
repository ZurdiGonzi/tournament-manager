package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.repository.MatchRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.MatchEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class MatchRepositoryAdapter implements MatchRepository {

    private final SpringDataMatchJpaRepository matchJpaRepository;
    private final PersistenceEntityMapper mapper;

    public MatchRepositoryAdapter(SpringDataMatchJpaRepository matchJpaRepository, PersistenceEntityMapper mapper) {
        this.matchJpaRepository = matchJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Match> findById(UUID matchId) {
        return matchJpaRepository.findById(matchId).map(mapper::toDomain);
    }

    @Override
    public List<Match> findByRoundIdOrderByTableNumberAsc(UUID roundId) {
        return matchJpaRepository.findByRoundIdOrderByTableNumberAsc(roundId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Match> findByTournamentId(UUID tournamentId) {
        return matchJpaRepository.findByTournamentId(tournamentId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public Match save(Match match) {
        MatchEntity saved = matchJpaRepository.save(mapper.toEntity(match));
        return mapper.toDomain(saved);
    }
}

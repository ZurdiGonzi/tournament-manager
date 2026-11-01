package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.TournamentTemplate;
import com.tournament.app.domain.repository.TournamentTemplateRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentTemplateEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class TournamentTemplateRepositoryAdapter implements TournamentTemplateRepository {

    private final SpringDataTournamentTemplateJpaRepository templateJpaRepository;
    private final PersistenceEntityMapper mapper;

    public TournamentTemplateRepositoryAdapter(
        SpringDataTournamentTemplateJpaRepository templateJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.templateJpaRepository = templateJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<TournamentTemplate> findById(UUID id) {
        return templateJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<TournamentTemplate> findByStoreId(UUID storeId) {
        return templateJpaRepository.findByStoreIdOrderByCreatedAtDesc(storeId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<TournamentTemplate> findByStoreIdAndActiveTrue(UUID storeId) {
        return templateJpaRepository.findByStoreIdAndActiveTrueOrderByCreatedAtDesc(storeId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<TournamentTemplate> findByLeagueId(UUID leagueId) {
        return templateJpaRepository.findByLeagueIdOrderByCreatedAtDesc(leagueId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<TournamentTemplate> findAllActive() {
        return templateJpaRepository.findByActiveTrue().stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public TournamentTemplate save(TournamentTemplate template) {
        TournamentTemplateEntity saved = templateJpaRepository.save(mapper.toEntity(template));
        return mapper.toDomain(saved);
    }
}

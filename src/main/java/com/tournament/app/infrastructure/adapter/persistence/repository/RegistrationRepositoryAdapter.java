package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.RegistrationDeckCard;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import com.tournament.app.infrastructure.adapter.persistence.jpa.RegistrationDeckCardEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentRegistrationEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class RegistrationRepositoryAdapter implements RegistrationRepository {

    private final SpringDataRegistrationJpaRepository registrationJpaRepository;
    private final SpringDataRegistrationDeckCardJpaRepository deckCardJpaRepository;
    private final PersistenceEntityMapper mapper;

    public RegistrationRepositoryAdapter(
        SpringDataRegistrationJpaRepository registrationJpaRepository,
        SpringDataRegistrationDeckCardJpaRepository deckCardJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.registrationJpaRepository = registrationJpaRepository;
        this.deckCardJpaRepository = deckCardJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<TournamentRegistration> findById(UUID id) {
        return registrationJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<TournamentRegistration> findByTournamentIdAndUserId(UUID tournamentId, UUID userId) {
        return registrationJpaRepository.findByTournamentIdAndUserId(tournamentId, userId).map(mapper::toDomain);
    }

    @Override
    public List<TournamentRegistration> findByTournamentIdOrderByRegisteredAtAsc(UUID tournamentId) {
        return registrationJpaRepository.findByTournamentIdOrderByRegisteredAtAsc(tournamentId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<TournamentRegistration> findStandingsByTournamentId(UUID tournamentId) {
        return registrationJpaRepository
            .findByTournamentIdOrderByMatchPointsDescOmwPercentageDescRegisteredAtAsc(tournamentId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<TournamentRegistration> findByUserId(UUID userId) {
        return registrationJpaRepository.findByUserIdOrderByRegisteredAtDesc(userId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<RegistrationDeckCard> findDeckCardsByRegistrationId(UUID registrationId) {
        return deckCardJpaRepository.findByRegistrationId(registrationId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<RegistrationDeckCard> findDeckCardsByTournamentId(UUID tournamentId) {
        return deckCardJpaRepository.findByTournamentId(tournamentId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public long countByTournamentId(UUID tournamentId) {
        return registrationJpaRepository.countByTournamentId(tournamentId);
    }

    @Override
    @Transactional
    public TournamentRegistration save(TournamentRegistration registration, List<RegistrationDeckCard> deckCards) {
        TournamentRegistrationEntity saved = registrationJpaRepository.save(mapper.toEntity(registration));
        if (deckCards != null && !deckCards.isEmpty()) {
            List<RegistrationDeckCardEntity> entities = deckCards.stream()
                .map(mapper::toEntity)
                .toList();
            deckCardJpaRepository.saveAll(entities);
        }
        return mapper.toDomain(saved);
    }
}

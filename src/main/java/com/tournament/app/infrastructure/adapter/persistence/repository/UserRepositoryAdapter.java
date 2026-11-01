package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.model.User;
import com.tournament.app.domain.model.UserProfileSummary;
import com.tournament.app.domain.repository.UserRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueStandingEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import com.tournament.app.infrastructure.adapter.persistence.jpa.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository userJpaRepository;
    private final SpringDataStoreJpaRepository storeJpaRepository;
    private final SpringDataDeckJpaRepository deckJpaRepository;
    private final SpringDataLeagueStandingJpaRepository leagueStandingJpaRepository;
    private final SpringDataLeagueJpaRepository leagueJpaRepository;
    private final PersistenceEntityMapper mapper;

    public UserRepositoryAdapter(
        SpringDataUserJpaRepository userJpaRepository,
        SpringDataStoreJpaRepository storeJpaRepository,
        SpringDataDeckJpaRepository deckJpaRepository,
        SpringDataLeagueStandingJpaRepository leagueStandingJpaRepository,
        SpringDataLeagueJpaRepository leagueJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.userJpaRepository = userJpaRepository;
        this.storeJpaRepository = storeJpaRepository;
        this.deckJpaRepository = deckJpaRepository;
        this.leagueStandingJpaRepository = leagueStandingJpaRepository;
        this.leagueJpaRepository = leagueJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .map(User::withoutPasswordHash)
            .toList();
    }

    @Override
    public List<User> findByStoreId(UUID storeId) {
        return userJpaRepository.findUsersByStoreId(storeId).stream()
            .map(mapper::toDomain)
            .map(User::withoutPasswordHash)
            .toList();
    }

    @Override
    public Optional<UserProfileSummary> findUserProfileSummaryById(UUID userId) {
        return userJpaRepository.findById(userId).map(userEntity -> {
            List<Store> memberStores = storeJpaRepository.findStoresByMemberUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
            long deckCount = deckJpaRepository.countByUserId(userId);
            List<LeagueStandingEntity> standings = leagueStandingJpaRepository.findByIdUserIdOrderByUpdatedAtDesc(userId);
            List<UUID> leagueIds = standings.stream().map(LeagueStandingEntity::getLeagueId).toList();
            Map<UUID, LeagueEntity> leaguesById = leagueJpaRepository.findAllById(leagueIds).stream()
                .collect(Collectors.toMap(LeagueEntity::getId, Function.identity()));

            List<UserProfileSummary.UserLeagueParticipation> participations = new ArrayList<>();
            for (LeagueStandingEntity standing : standings) {
                LeagueEntity league = leaguesById.get(standing.getLeagueId());
                if (league != null) {
                    participations.add(new UserProfileSummary.UserLeagueParticipation(
                        league.getId(),
                        league.getName(),
                        league.getStoreId(),
                        league.getFormat(),
                        league.getStatus(),
                        standing.getTotalPoints(),
                        standing.getTournamentsPlayed()
                    ));
                }
            }

            return new UserProfileSummary(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getRole(),
                userEntity.getCreatedAt(),
                memberStores,
                deckCount,
                participations
            );
        });
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity saved = userJpaRepository.save(mapper.toEntity(user));
        return mapper.toDomain(saved);
    }
}

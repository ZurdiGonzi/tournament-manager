package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.model.StoreDetailSummary;
import com.tournament.app.domain.model.StoreMember;
import com.tournament.app.domain.repository.StoreRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import com.tournament.app.infrastructure.adapter.persistence.jpa.StoreEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.StoreMemberEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class StoreRepositoryAdapter implements StoreRepository {

    private final SpringDataStoreJpaRepository storeJpaRepository;
    private final SpringDataStoreMemberJpaRepository storeMemberJpaRepository;
    private final SpringDataUserJpaRepository userJpaRepository;
    private final SpringDataTournamentJpaRepository tournamentJpaRepository;
    private final PersistenceEntityMapper mapper;

    public StoreRepositoryAdapter(
        SpringDataStoreJpaRepository storeJpaRepository,
        SpringDataStoreMemberJpaRepository storeMemberJpaRepository,
        SpringDataUserJpaRepository userJpaRepository,
        SpringDataTournamentJpaRepository tournamentJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.storeJpaRepository = storeJpaRepository;
        this.storeMemberJpaRepository = storeMemberJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Store> findById(UUID id) {
        return storeJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Store> findByName(String name) {
        return storeJpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Store> findAll() {
        return storeJpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Store> findByOwnerId(UUID ownerId) {
        return storeJpaRepository.findByOwnerId(ownerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<StoreMember> findMembersByStoreId(UUID storeId) {
        List<StoreMemberEntity> memberEntities = storeMemberJpaRepository.findByIdStoreIdOrderByJoinedAtAsc(storeId);
        List<UUID> userIds = memberEntities.stream().map(StoreMemberEntity::getUserId).toList();
        Map<UUID, UserEntity> usersById = userJpaRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return memberEntities.stream()
            .map(m -> mapper.toDomain(m, usersById.get(m.getUserId())))
            .toList();
    }

    @Override
    public List<Store> findStoresByMemberUserId(UUID userId) {
        return storeJpaRepository.findStoresByMemberUserId(userId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<StoreDetailSummary> findStoreDetailById(UUID storeId) {
        return storeJpaRepository.findById(storeId).map(storeEntity -> {
            Store store = mapper.toDomain(storeEntity);
            String ownerUsername = userJpaRepository.findById(storeEntity.getOwnerId())
                .map(UserEntity::getUsername)
                .orElse(null);
            long totalTournaments = tournamentJpaRepository.countByStoreId(storeId);
            List<StoreMember> members = findMembersByStoreId(storeId);
            return new StoreDetailSummary(store, ownerUsername, totalTournaments, members.size(), members);
        });
    }

    @Override
    @Transactional
    public Store save(Store store) {
        StoreEntity saved = storeJpaRepository.save(mapper.toEntity(store));
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public StoreMember addMember(UUID storeId, UUID userId) {
        StoreMemberEntity entity = new StoreMemberEntity(storeId, userId, Instant.now());
        StoreMemberEntity saved = storeMemberJpaRepository.save(entity);
        UserEntity user = userJpaRepository.findById(userId).orElse(null);
        return mapper.toDomain(saved, user);
    }
}

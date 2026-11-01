package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.model.StoreDetailSummary;
import com.tournament.app.domain.model.StoreMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {

    Optional<Store> findById(UUID id);

    Optional<Store> findByName(String name);

    List<Store> findAll();

    List<Store> findByOwnerId(UUID ownerId);

    List<StoreMember> findMembersByStoreId(UUID storeId);

    List<Store> findStoresByMemberUserId(UUID userId);

    Optional<StoreDetailSummary> findStoreDetailById(UUID storeId);

    Store save(Store store);

    StoreMember addMember(UUID storeId, UUID userId);
}

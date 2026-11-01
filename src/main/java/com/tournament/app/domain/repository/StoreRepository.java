package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.Store;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository {
    Store save(Store store);
    Optional<Store> findById(UUID id);
    List<Store> findByOwnerId(UUID ownerId);
    void addMember(UUID storeId, UUID userId);
}

package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.User;
import com.tournament.app.domain.model.UserProfileSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    List<User> findByStoreId(UUID storeId);

    Optional<UserProfileSummary> findUserProfileSummaryById(UUID userId);

    User save(User user);
}

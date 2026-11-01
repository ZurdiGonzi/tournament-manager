package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.User;
import com.tournament.app.domain.repository.DeckRepository;
import com.tournament.app.domain.repository.ScryfallCardRepository;
import com.tournament.app.domain.repository.UserRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.DeckEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.ScryfallCardEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
interface SpringDataUserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}

@Repository
interface SpringDataDeckJpaRepository extends JpaRepository<DeckEntity, UUID> {
    Optional<DeckEntity> findByUserIdAndMoxfieldDeckId(UUID userId, String moxfieldDeckId);
    List<DeckEntity> findByUserId(UUID userId);
}

@Repository
interface SpringDataScryfallCardJpaRepository extends JpaRepository<ScryfallCardEntity, UUID> {
}

@Component
class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository jpaRepository;

    UserRepositoryAdapter(SpringDataUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(UserEntity.fromDomain(user)).toDomain();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(UserEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}

@Component
class DeckRepositoryAdapter implements DeckRepository {

    private final SpringDataDeckJpaRepository jpaRepository;

    DeckRepositoryAdapter(SpringDataDeckJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Deck save(Deck deck) {
        return jpaRepository.save(DeckEntity.fromDomain(deck)).toDomain();
    }

    @Override
    public Optional<Deck> findById(UUID id) {
        return jpaRepository.findById(id).map(DeckEntity::toDomain);
    }

    @Override
    public Optional<Deck> findByUserIdAndMoxfieldDeckId(UUID userId, String moxfieldDeckId) {
        return jpaRepository.findByUserIdAndMoxfieldDeckId(userId, moxfieldDeckId).map(DeckEntity::toDomain);
    }

    @Override
    public List<Deck> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(DeckEntity::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

@Component
class ScryfallCardRepositoryAdapter implements ScryfallCardRepository {

    private final SpringDataScryfallCardJpaRepository jpaRepository;

    ScryfallCardRepositoryAdapter(SpringDataScryfallCardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void saveAll(List<ScryfallCard> cards) {
        if (cards != null && !cards.isEmpty()) {
            jpaRepository.saveAll(cards.stream().map(ScryfallCardEntity::fromDomain).toList());
        }
    }

    @Override
    public Map<UUID, ScryfallCard> findByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return jpaRepository.findAllById(ids).stream()
                .map(ScryfallCardEntity::toDomain)
                .collect(Collectors.toMap(ScryfallCard::id, Function.identity(), (a, b) -> a));
    }
}

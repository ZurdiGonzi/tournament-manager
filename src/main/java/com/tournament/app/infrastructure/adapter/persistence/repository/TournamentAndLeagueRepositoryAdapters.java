package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.MatchParticipant;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.model.ScoringFormula;
import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.repository.LeagueRepository;
import com.tournament.app.domain.repository.MatchRepository;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.domain.repository.ScoringFormulaRepository;
import com.tournament.app.domain.repository.StoreRepository;
import com.tournament.app.domain.repository.TournamentRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.LeagueStandingEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.MatchEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.MatchParticipantEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.RegistrationDeckCardEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.RoundEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.ScoringFormulaEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.StoreEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
interface SpringDataStoreJpaRepository extends JpaRepository<StoreEntity, UUID> {
    List<StoreEntity> findByOwnerId(UUID ownerId);
}

@Repository
interface SpringDataScoringFormulaJpaRepository extends JpaRepository<ScoringFormulaEntity, UUID> {
    List<ScoringFormulaEntity> findByStoreId(UUID storeId);
}

@Repository
interface SpringDataLeagueJpaRepository extends JpaRepository<LeagueEntity, UUID> {
    List<LeagueEntity> findByStoreId(UUID storeId);
}

@Repository
interface SpringDataLeagueStandingJpaRepository extends JpaRepository<LeagueStandingEntity, UUID> {
    List<LeagueStandingEntity> findByLeagueIdOrderByTotalLeaguePointsDesc(UUID leagueId);
    void deleteByLeagueId(UUID leagueId);
}

@Repository
interface SpringDataTournamentJpaRepository extends JpaRepository<TournamentEntity, UUID> {
    List<TournamentEntity> findByStoreId(UUID storeId);
    List<TournamentEntity> findByLeagueIdOrderByMatchdayNumberAsc(UUID leagueId);
    boolean existsByTemplateIdAndScheduledAt(UUID templateId, Instant scheduledAt);
}

@Repository
interface SpringDataRegistrationJpaRepository extends JpaRepository<TournamentRegistrationEntity, UUID> {
    Optional<TournamentRegistrationEntity> findByTournamentIdAndUserId(UUID tournamentId, UUID userId);
    List<TournamentRegistrationEntity> findByTournamentId(UUID tournamentId);
    List<TournamentRegistrationEntity> findByTournamentIdIn(List<UUID> tournamentIds);
    List<TournamentRegistrationEntity> findByUserIdAndDeckId(UUID userId, UUID deckId);
}

@Repository
interface SpringDataRegistrationDeckCardJpaRepository extends JpaRepository<RegistrationDeckCardEntity, UUID> {
    void deleteByRegistrationId(UUID registrationId);
}

@Repository
interface SpringDataRoundJpaRepository extends JpaRepository<RoundEntity, UUID> {
    List<RoundEntity> findByTournamentId(UUID tournamentId);
    List<RoundEntity> findByTournamentIdIn(List<UUID> tournamentIds);
}

@Repository
interface SpringDataMatchJpaRepository extends JpaRepository<MatchEntity, UUID> {
    List<MatchEntity> findByRoundIdIn(List<UUID> roundIds);
}

@Repository
interface SpringDataMatchParticipantJpaRepository extends JpaRepository<MatchParticipantEntity, UUID> {
    List<MatchParticipantEntity> findByMatchId(UUID matchId);
    List<MatchParticipantEntity> findByMatchIdIn(List<UUID> matchIds);
    List<MatchParticipantEntity> findByDeckId(UUID deckId);
    void deleteByMatchId(UUID matchId);
}

@Component
class StoreRepositoryAdapter implements StoreRepository {

    private final SpringDataStoreJpaRepository jpaRepository;
    private final JdbcTemplate jdbcTemplate;

    StoreRepositoryAdapter(SpringDataStoreJpaRepository jpaRepository, JdbcTemplate jdbcTemplate) {
        this.jpaRepository = jpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Store save(Store store) {
        return jpaRepository.save(StoreEntity.fromDomain(store)).toDomain();
    }

    @Override
    public Optional<Store> findById(UUID id) {
        return jpaRepository.findById(id).map(StoreEntity::toDomain);
    }

    @Override
    public List<Store> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream().map(StoreEntity::toDomain).toList();
    }

    @Override
    public void addMember(UUID storeId, UUID userId) {
        jdbcTemplate.update(
                "INSERT INTO store_members (store_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                storeId, userId
        );
    }
}

@Component
class ScoringFormulaRepositoryAdapter implements ScoringFormulaRepository {

    private final SpringDataScoringFormulaJpaRepository jpaRepository;

    ScoringFormulaRepositoryAdapter(SpringDataScoringFormulaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ScoringFormula save(ScoringFormula formula) {
        return jpaRepository.save(ScoringFormulaEntity.fromDomain(formula)).toDomain();
    }

    @Override
    public Optional<ScoringFormula> findById(UUID id) {
        return jpaRepository.findById(id).map(ScoringFormulaEntity::toDomain);
    }

    @Override
    public List<ScoringFormula> findByStoreId(UUID storeId) {
        return jpaRepository.findByStoreId(storeId).stream().map(ScoringFormulaEntity::toDomain).toList();
    }
}

@Component
class LeagueRepositoryAdapter implements LeagueRepository {

    private final SpringDataLeagueJpaRepository leagueJpa;
    private final SpringDataLeagueStandingJpaRepository standingJpa;

    LeagueRepositoryAdapter(
            SpringDataLeagueJpaRepository leagueJpa,
            SpringDataLeagueStandingJpaRepository standingJpa) {
        this.leagueJpa = leagueJpa;
        this.standingJpa = standingJpa;
    }

    @Override
    public League save(League league) {
        return leagueJpa.save(LeagueEntity.fromDomain(league)).toDomain();
    }

    @Override
    public Optional<League> findById(UUID id) {
        return leagueJpa.findById(id).map(LeagueEntity::toDomain);
    }

    @Override
    public List<League> findByStoreId(UUID storeId) {
        return leagueJpa.findByStoreId(storeId).stream().map(LeagueEntity::toDomain).toList();
    }

    @Override
    public List<LeagueStanding> findStandingsByLeagueId(UUID leagueId) {
        return standingJpa.findByLeagueIdOrderByTotalLeaguePointsDesc(leagueId)
                .stream()
                .map(LeagueStandingEntity::toDomain)
                .toList();
    }

    @Override
    public void saveStandings(List<LeagueStanding> standings) {
        if (standings == null || standings.isEmpty()) {
            return;
        }
        UUID leagueId = standings.getFirst().leagueId();
        standingJpa.deleteByLeagueId(leagueId);
        standingJpa.saveAll(standings.stream().map(LeagueStandingEntity::fromDomain).toList());
    }
}

@Component
class TournamentRepositoryAdapter implements TournamentRepository {

    private final SpringDataTournamentJpaRepository jpaRepository;

    TournamentRepositoryAdapter(SpringDataTournamentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Tournament save(Tournament tournament) {
        return jpaRepository.save(TournamentEntity.fromDomain(tournament)).toDomain();
    }

    @Override
    public Optional<Tournament> findById(UUID id) {
        return jpaRepository.findById(id).map(TournamentEntity::toDomain);
    }

    @Override
    public List<Tournament> findByStoreId(UUID storeId) {
        return jpaRepository.findByStoreId(storeId).stream().map(TournamentEntity::toDomain).toList();
    }

    @Override
    public List<Tournament> findByLeagueId(UUID leagueId) {
        return jpaRepository.findByLeagueIdOrderByMatchdayNumberAsc(leagueId)
                .stream()
                .map(TournamentEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTemplateIdAndScheduledAt(UUID templateId, Instant scheduledAt) {
        return jpaRepository.existsByTemplateIdAndScheduledAt(templateId, scheduledAt);
    }
}

@Component
class RegistrationRepositoryAdapter implements RegistrationRepository {

    private final SpringDataRegistrationJpaRepository regJpa;
    private final SpringDataRegistrationDeckCardJpaRepository deckCardJpa;
    private final SpringDataTournamentJpaRepository tournamentJpa;

    RegistrationRepositoryAdapter(
            SpringDataRegistrationJpaRepository regJpa,
            SpringDataRegistrationDeckCardJpaRepository deckCardJpa,
            SpringDataTournamentJpaRepository tournamentJpa) {
        this.regJpa = regJpa;
        this.deckCardJpa = deckCardJpa;
        this.tournamentJpa = tournamentJpa;
    }

    @Override
    public TournamentRegistration save(TournamentRegistration registration) {
        TournamentRegistration saved = regJpa.save(TournamentRegistrationEntity.fromDomain(registration)).toDomain();
        persistAnalyticalDeckCards(saved);
        return saved;
    }

    @Override
    public void saveAll(List<TournamentRegistration> registrations) {
        if (registrations != null && !registrations.isEmpty()) {
            regJpa.saveAll(registrations.stream().map(TournamentRegistrationEntity::fromDomain).toList());
        }
    }

    @Override
    public Optional<TournamentRegistration> findByTournamentIdAndUserId(UUID tournamentId, UUID userId) {
        return regJpa.findByTournamentIdAndUserId(tournamentId, userId).map(TournamentRegistrationEntity::toDomain);
    }

    @Override
    public List<TournamentRegistration> findByTournamentId(UUID tournamentId) {
        return regJpa.findByTournamentId(tournamentId).stream().map(TournamentRegistrationEntity::toDomain).toList();
    }

    @Override
    public List<TournamentRegistration> findByLeagueId(UUID leagueId) {
        List<UUID> tournamentIds = tournamentJpa.findByLeagueIdOrderByMatchdayNumberAsc(leagueId)
                .stream()
                .map(t -> t.toDomain().id())
                .toList();
        if (tournamentIds.isEmpty()) {
            return List.of();
        }
        return regJpa.findByTournamentIdIn(tournamentIds)
                .stream()
                .map(TournamentRegistrationEntity::toDomain)
                .toList();
    }

    @Override
    public List<TournamentRegistration> findByUserIdAndDeckId(UUID userId, UUID deckId) {
        return regJpa.findByUserIdAndDeckId(userId, deckId)
                .stream()
                .map(TournamentRegistrationEntity::toDomain)
                .toList();
    }

    private void persistAnalyticalDeckCards(TournamentRegistration reg) {
        DeckSnapshot snap = reg.deckSnapshot();
        if (snap == null) {
            return;
        }
        deckCardJpa.deleteByRegistrationId(reg.id());
        List<RegistrationDeckCardEntity> rows = new ArrayList<>();
        appendSection(rows, reg.id(), "COMMANDER", snap.commanders());
        appendSection(rows, reg.id(), "MAINBOARD", snap.mainboard());
        appendSection(rows, reg.id(), "SIDEBOARD", snap.sideboard());
        if (!rows.isEmpty()) {
            deckCardJpa.saveAll(rows);
        }
    }

    private void appendSection(
            List<RegistrationDeckCardEntity> rows,
            UUID registrationId,
            String section,
            List<DeckSnapshot.CardEntry> entries) {
        if (entries == null) {
            return;
        }
        for (DeckSnapshot.CardEntry entry : entries) {
            rows.add(new RegistrationDeckCardEntity(
                    UUID.randomUUID(),
                    registrationId,
                    entry.scryfallId(),
                    section,
                    entry.quantity(),
                    entry.unitPrice()
            ));
        }
    }
}

@Component
class MatchRepositoryAdapter implements MatchRepository {

    private final SpringDataRoundJpaRepository roundJpa;
    private final SpringDataMatchJpaRepository matchJpa;
    private final SpringDataMatchParticipantJpaRepository participantJpa;
    private final SpringDataTournamentJpaRepository tournamentJpa;

    MatchRepositoryAdapter(
            SpringDataRoundJpaRepository roundJpa,
            SpringDataMatchJpaRepository matchJpa,
            SpringDataMatchParticipantJpaRepository participantJpa,
            SpringDataTournamentJpaRepository tournamentJpa) {
        this.roundJpa = roundJpa;
        this.matchJpa = matchJpa;
        this.participantJpa = participantJpa;
        this.tournamentJpa = tournamentJpa;
    }

    @Override
    public Round saveRound(Round round) {
        return roundJpa.save(RoundEntity.fromDomain(round)).toDomain();
    }

    @Override
    public List<Match> saveMatches(List<Match> matches) {
        List<Match> saved = new ArrayList<>();
        for (Match match : matches) {
            saved.add(saveMatch(match));
        }
        return saved;
    }

    @Override
    public Match saveMatch(Match match) {
        MatchEntity savedMatch = matchJpa.save(MatchEntity.fromDomain(match));
        participantJpa.deleteByMatchId(savedMatch.getId());
        List<MatchParticipantEntity> savedParticipants = participantJpa.saveAll(
                match.participants().stream().map(MatchParticipantEntity::fromDomain).toList()
        );
        return savedMatch.toDomain(savedParticipants.stream().map(MatchParticipantEntity::toDomain).toList());
    }

    @Override
    public Optional<Match> findMatchById(UUID matchId) {
        return matchJpa.findById(matchId).map(me -> {
            List<MatchParticipant> parts = participantJpa.findByMatchId(matchId)
                    .stream()
                    .map(MatchParticipantEntity::toDomain)
                    .toList();
            return me.toDomain(parts);
        });
    }

    @Override
    public Optional<Round> findRoundById(UUID roundId) {
        return roundJpa.findById(roundId).map(RoundEntity::toDomain);
    }

    @Override
    public List<Match> findMatchesByTournamentId(UUID tournamentId) {
        List<UUID> roundIds = roundJpa.findByTournamentId(tournamentId).stream().map(RoundEntity::getId).toList();
        return loadMatchesForRounds(roundIds);
    }

    @Override
    public List<Match> findMatchesByLeagueId(UUID leagueId) {
        List<UUID> tournamentIds = tournamentJpa.findByLeagueIdOrderByMatchdayNumberAsc(leagueId)
                .stream()
                .map(t -> t.toDomain().id())
                .toList();
        if (tournamentIds.isEmpty()) {
            return List.of();
        }
        List<UUID> roundIds = roundJpa.findByTournamentIdIn(tournamentIds).stream().map(RoundEntity::getId).toList();
        return loadMatchesForRounds(roundIds);
    }

    @Override
    public List<Match> findMatchesByDeckId(UUID deckId) {
        List<UUID> matchIds = participantJpa.findByDeckId(deckId)
                .stream()
                .map(MatchParticipantEntity::getMatchId)
                .distinct()
                .toList();
        if (matchIds.isEmpty()) {
            return List.of();
        }
        List<MatchEntity> matchEntities = matchJpa.findAllById(matchIds);
        return hydrateMatches(matchEntities);
    }

    @Override
    public List<MatchParticipant> findParticipantsByTournamentId(UUID tournamentId) {
        return findMatchesByTournamentId(tournamentId).stream()
                .filter(m -> "CONFIRMED".equalsIgnoreCase(m.status()))
                .flatMap(m -> m.participants().stream())
                .toList();
    }

    private List<Match> loadMatchesForRounds(List<UUID> roundIds) {
        if (roundIds == null || roundIds.isEmpty()) {
            return List.of();
        }
        List<MatchEntity> matchEntities = matchJpa.findByRoundIdIn(roundIds);
        return hydrateMatches(matchEntities);
    }

    private List<Match> hydrateMatches(List<MatchEntity> matchEntities) {
        if (matchEntities.isEmpty()) {
            return List.of();
        }
        List<UUID> matchIds = matchEntities.stream().map(MatchEntity::getId).toList();
        Map<UUID, List<MatchParticipant>> partsByMatch = participantJpa.findByMatchIdIn(matchIds)
                .stream()
                .map(MatchParticipantEntity::toDomain)
                .collect(Collectors.groupingBy(MatchParticipant::matchId));
        return matchEntities.stream()
                .map(me -> me.toDomain(partsByMatch.getOrDefault(me.getId(), List.of())))
                .toList();
    }
}

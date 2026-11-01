package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.League;
import com.tournament.app.domain.model.LeagueLeaderboardEntry;
import com.tournament.app.domain.model.LeagueStanding;
import com.tournament.app.domain.model.Match;
import com.tournament.app.domain.model.RegistrationDeckCard;
import com.tournament.app.domain.model.Round;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.ScryfallCardStats;
import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.model.StoreMember;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.model.TournamentRestrictions;
import com.tournament.app.domain.model.TournamentTemplate;
import com.tournament.app.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PersistenceEntityMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return new User(
            entity.getId(),
            entity.getUsername(),
            entity.getPasswordHash(),
            entity.getRole(),
            entity.getCreatedAt()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;
        return new UserEntity(
            domain.id(),
            domain.username(),
            domain.passwordHash() != null ? domain.passwordHash() : "",
            domain.role(),
            domain.createdAt()
        );
    }

    public Store toDomain(StoreEntity entity) {
        if (entity == null) return null;
        return new Store(
            entity.getId(),
            entity.getName(),
            entity.getOwnerId(),
            entity.getCurrency(),
            entity.getCreatedAt()
        );
    }

    public StoreEntity toEntity(Store domain) {
        if (domain == null) return null;
        return new StoreEntity(
            domain.id(),
            domain.name(),
            domain.ownerId(),
            domain.currency(),
            domain.createdAt()
        );
    }

    public StoreMember toDomain(StoreMemberEntity memberEntity, UserEntity userEntity) {
        if (memberEntity == null) return null;
        return new StoreMember(
            memberEntity.getStoreId(),
            memberEntity.getUserId(),
            userEntity != null ? userEntity.getUsername() : null,
            userEntity != null ? userEntity.getRole() : null,
            memberEntity.getJoinedAt()
        );
    }

    public Deck toDomain(DeckEntity entity) {
        if (entity == null) return null;
        return new Deck(
            entity.getId(),
            entity.getUserId(),
            entity.getName(),
            entity.getMoxfieldUrl(),
            entity.getMoxfieldDeckId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public DeckEntity toEntity(Deck domain) {
        if (domain == null) return null;
        return new DeckEntity(
            domain.id(),
            domain.userId(),
            domain.name(),
            domain.moxfieldUrl(),
            domain.moxfieldDeckId(),
            domain.createdAt(),
            domain.updatedAt()
        );
    }

    public League toDomain(LeagueEntity entity) {
        if (entity == null) return null;
        return new League(
            entity.getId(),
            entity.getStoreId(),
            entity.getName(),
            entity.getFormat(),
            entity.getPrizeDescription(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getStatus(),
            entity.getCreatedAt()
        );
    }

    public LeagueEntity toEntity(League domain) {
        if (domain == null) return null;
        return new LeagueEntity(
            domain.id(),
            domain.storeId(),
            domain.name(),
            domain.format(),
            domain.prizeDescription(),
            domain.startDate(),
            domain.endDate(),
            domain.status(),
            domain.createdAt()
        );
    }

    public LeagueStanding toDomain(LeagueStandingEntity entity) {
        if (entity == null) return null;
        return new LeagueStanding(
            entity.getLeagueId(),
            entity.getUserId(),
            entity.getTotalPoints(),
            entity.getTournamentsPlayed(),
            entity.getMatchesWon(),
            entity.getMatchesDrawn(),
            entity.getMatchesLost(),
            entity.getUpdatedAt()
        );
    }

    public LeagueStandingEntity toEntity(LeagueStanding domain) {
        if (domain == null) return null;
        return new LeagueStandingEntity(
            domain.leagueId(),
            domain.userId(),
            domain.totalPoints(),
            domain.tournamentsPlayed(),
            domain.matchesWon(),
            domain.matchesDrawn(),
            domain.matchesLost(),
            domain.updatedAt()
        );
    }

    public LeagueLeaderboardEntry toDomain(LeagueLeaderboardViewEntity entity) {
        if (entity == null) return null;
        return new LeagueLeaderboardEntry(
            entity.getLeagueId(),
            entity.getStoreId(),
            entity.getLeagueName(),
            entity.getUserId(),
            entity.getUsername(),
            entity.getTotalPoints(),
            entity.getTournamentsPlayed(),
            entity.getCurrentRank()
        );
    }

    public TournamentTemplate toDomain(TournamentTemplateEntity entity) {
        if (entity == null) return null;
        return new TournamentTemplate(
            entity.getId(),
            entity.getStoreId(),
            entity.getLeagueId(),
            entity.getName(),
            entity.getCronExpression(),
            entity.getEntryFee(),
            entity.getPrizeDescription(),
            entity.getFormat(),
            entity.getMaxDeckBudget(),
            entity.getMaxPlayers(),
            entity.getMaxRounds(),
            entity.isActive(),
            entity.getLastGeneratedAt(),
            entity.getCreatedAt()
        );
    }

    public TournamentTemplateEntity toEntity(TournamentTemplate domain) {
        if (domain == null) return null;
        return new TournamentTemplateEntity(
            domain.id(),
            domain.storeId(),
            domain.leagueId(),
            domain.name(),
            domain.cronExpression(),
            domain.entryFee(),
            domain.prizeDescription(),
            domain.format(),
            domain.maxDeckBudget(),
            domain.maxPlayers(),
            domain.maxRounds(),
            domain.active(),
            domain.lastGeneratedAt(),
            domain.createdAt()
        );
    }

    public Tournament toDomain(TournamentEntity entity) {
        if (entity == null) return null;
        return new Tournament(
            entity.getId(),
            entity.getStoreId(),
            entity.getLeagueId(),
            entity.getTemplateId(),
            entity.getName(),
            entity.getScheduledAt(),
            entity.getEntryFee(),
            entity.getPrizeDescription(),
            new TournamentRestrictions(entity.getFormat(), entity.getMaxDeckBudget()),
            entity.getMaxPlayers(),
            entity.getStatus(),
            entity.getCurrentRound(),
            entity.getMaxRounds(),
            entity.getCreatedAt()
        );
    }

    public TournamentEntity toEntity(Tournament domain) {
        if (domain == null) return null;
        return new TournamentEntity(
            domain.id(),
            domain.storeId(),
            domain.leagueId(),
            domain.templateId(),
            domain.name(),
            domain.scheduledAt(),
            domain.entryFee(),
            domain.prizeDescription(),
            domain.format(),
            domain.maxDeckBudget(),
            domain.maxPlayers(),
            domain.status(),
            domain.currentRound(),
            domain.maxRounds(),
            domain.createdAt()
        );
    }

    public ScryfallCard toDomain(ScryfallCardEntity entity) {
        if (entity == null) return null;
        return new ScryfallCard(
            entity.getId(),
            entity.getOracleId(),
            entity.getName(),
            entity.getManaCost(),
            entity.getCmc(),
            entity.getTypeLine(),
            entity.getColors() != null ? entity.getColors() : List.of(),
            entity.getColorIdentity() != null ? entity.getColorIdentity() : List.of(),
            entity.getRarity(),
            entity.getLegalities() != null ? entity.getLegalities() : Map.of(),
            entity.getPriceEur(),
            entity.getPriceUsd(),
            entity.getImageUri(),
            entity.getLastSyncedAt()
        );
    }

    public ScryfallCardEntity toEntity(ScryfallCard domain) {
        if (domain == null) return null;
        return new ScryfallCardEntity(
            domain.id(),
            domain.oracleId(),
            domain.name(),
            domain.manaCost(),
            domain.cmc(),
            domain.typeLine(),
            domain.colors(),
            domain.colorIdentity(),
            domain.rarity(),
            domain.legalities(),
            domain.priceEur(),
            domain.priceUsd(),
            domain.imageUri(),
            domain.lastSyncedAt()
        );
    }

    public ScryfallCardStats toDomain(ScryfallCardStatsViewEntity entity) {
        if (entity == null) return null;
        return new ScryfallCardStats(
            entity.getStoreId(),
            entity.getLeagueId(),
            entity.getFormat(),
            entity.getOracleId(),
            entity.getCardName(),
            entity.getTypeLine(),
            entity.getManaCost(),
            entity.getCmc(),
            entity.getColors() != null ? entity.getColors() : List.of(),
            entity.getColorIdentity() != null ? entity.getColorIdentity() : List.of(),
            entity.getRarity(),
            entity.getDecksIncludingCard(),
            entity.getTotalCopiesPlayed(),
            entity.getAvgUnitPrice()
        );
    }

    public TournamentRegistration toDomain(TournamentRegistrationEntity entity) {
        if (entity == null) return null;
        return new TournamentRegistration(
            entity.getId(),
            entity.getTournamentId(),
            entity.getUserId(),
            entity.getDeckId(),
            entity.getStatus(),
            entity.getCalculatedDeckPrice(),
            entity.getMatchPoints(),
            entity.getOmwPercentage(),
            entity.getFinalRank(),
            entity.getDeckSnapshot(),
            entity.getRegisteredAt()
        );
    }

    public TournamentRegistrationEntity toEntity(TournamentRegistration domain) {
        if (domain == null) return null;
        return new TournamentRegistrationEntity(
            domain.id(),
            domain.tournamentId(),
            domain.userId(),
            domain.deckId(),
            domain.status(),
            domain.calculatedDeckPrice(),
            domain.matchPoints(),
            domain.omwPercentage(),
            domain.finalRank(),
            domain.deckSnapshot(),
            domain.registeredAt()
        );
    }

    public RegistrationDeckCard toDomain(RegistrationDeckCardEntity entity) {
        if (entity == null) return null;
        return new RegistrationDeckCard(
            entity.getId(),
            entity.getRegistrationId(),
            entity.getScryfallCardId(),
            entity.getBoardSection(),
            entity.getQuantity(),
            entity.getUnitPriceSnapshot()
        );
    }

    public RegistrationDeckCardEntity toEntity(RegistrationDeckCard domain) {
        if (domain == null) return null;
        return new RegistrationDeckCardEntity(
            domain.id(),
            domain.registrationId(),
            domain.scryfallCardId(),
            domain.boardSection(),
            domain.quantity(),
            domain.unitPriceSnapshot()
        );
    }

    public Round toDomain(RoundEntity entity) {
        if (entity == null) return null;
        return new Round(
            entity.getId(),
            entity.getTournamentId(),
            entity.getRoundNumber(),
            entity.getStatus(),
            entity.getStartedAt(),
            entity.getEndedAt()
        );
    }

    public RoundEntity toEntity(Round domain) {
        if (domain == null) return null;
        return new RoundEntity(
            domain.id(),
            domain.tournamentId(),
            domain.roundNumber(),
            domain.status(),
            domain.startedAt(),
            domain.endedAt()
        );
    }

    public Match toDomain(MatchEntity entity) {
        if (entity == null) return null;
        return new Match(
            entity.getId(),
            entity.getRoundId(),
            entity.getTableNumber(),
            entity.getPlayer1Id(),
            entity.getPlayer2Id(),
            entity.getPlayer1Wins(),
            entity.getPlayer2Wins(),
            entity.getDraws(),
            entity.getStatus(),
            entity.getWinnerId()
        );
    }

    public MatchEntity toEntity(Match domain) {
        if (domain == null) return null;
        return new MatchEntity(
            domain.id(),
            domain.roundId(),
            domain.tableNumber(),
            domain.player1Id(),
            domain.player2Id(),
            domain.player1Wins(),
            domain.player2Wins(),
            domain.draws(),
            domain.status(),
            domain.winnerId()
        );
    }
}

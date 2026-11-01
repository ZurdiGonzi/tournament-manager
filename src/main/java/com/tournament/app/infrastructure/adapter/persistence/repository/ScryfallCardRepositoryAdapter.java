package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.domain.model.MetagameAnalyticsSummary;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.ScryfallCardStats;
import com.tournament.app.domain.repository.ScryfallCardRepository;
import com.tournament.app.infrastructure.adapter.persistence.jpa.PersistenceEntityMapper;
import com.tournament.app.infrastructure.adapter.persistence.jpa.ScryfallCardEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentEntity;
import com.tournament.app.infrastructure.adapter.persistence.jpa.TournamentRegistrationEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class ScryfallCardRepositoryAdapter implements ScryfallCardRepository {

    private final SpringDataScryfallCardJpaRepository cardJpaRepository;
    private final SpringDataScryfallCardStatsJpaRepository cardStatsJpaRepository;
    private final SpringDataTournamentJpaRepository tournamentJpaRepository;
    private final SpringDataRegistrationJpaRepository registrationJpaRepository;
    private final PersistenceEntityMapper mapper;

    public ScryfallCardRepositoryAdapter(
        SpringDataScryfallCardJpaRepository cardJpaRepository,
        SpringDataScryfallCardStatsJpaRepository cardStatsJpaRepository,
        SpringDataTournamentJpaRepository tournamentJpaRepository,
        SpringDataRegistrationJpaRepository registrationJpaRepository,
        PersistenceEntityMapper mapper
    ) {
        this.cardJpaRepository = cardJpaRepository;
        this.cardStatsJpaRepository = cardStatsJpaRepository;
        this.tournamentJpaRepository = tournamentJpaRepository;
        this.registrationJpaRepository = registrationJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ScryfallCard> findById(UUID scryfallId) {
        return cardJpaRepository.findById(scryfallId).map(mapper::toDomain);
    }

    @Override
    public List<ScryfallCard> findByIdIn(Collection<UUID> scryfallIds) {
        if (scryfallIds == null || scryfallIds.isEmpty()) {
            return List.of();
        }
        return cardJpaRepository.findAllById(scryfallIds).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<ScryfallCard> findByOracleId(UUID oracleId) {
        return cardJpaRepository.findByOracleId(oracleId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<ScryfallCard> findFirstByNameIgnoreCase(String name) {
        return cardJpaRepository.findFirstByNameIgnoreCaseOrderByLastSyncedAtDesc(name)
            .map(mapper::toDomain);
    }

    @Override
    public List<ScryfallCard> findByNameInIgnoreCase(Collection<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }
        return cardJpaRepository.findByNameInIgnoreCase(names).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<ScryfallCard> findByColorContaining(String colorCode) {
        if (colorCode == null || colorCode.isBlank()) {
            return List.of();
        }
        String normalized = colorCode.trim().toUpperCase();
        return cardJpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .filter(c -> c.colors().contains(normalized) || c.colorIdentity().contains(normalized))
            .toList();
    }

    @Override
    public List<ScryfallCard> findLegalByFormat(MagicFormat format) {
        if (format == null) {
            return List.of();
        }
        return cardJpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .filter(c -> c.isLegalIn(format))
            .toList();
    }

    @Override
    @Transactional
    public List<ScryfallCard> saveAll(Collection<ScryfallCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return List.of();
        }
        List<ScryfallCardEntity> entities = cards.stream()
            .map(mapper::toEntity)
            .toList();
        return cardJpaRepository.saveAll(entities).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<ScryfallCardStats> findCardStatsByStoreIdAndFormat(UUID storeId, MagicFormat format) {
        if (format == null) {
            return cardStatsJpaRepository.findByIdStoreIdOrderByTotalCopiesPlayedDesc(storeId).stream()
                .map(mapper::toDomain)
                .toList();
        }
        return cardStatsJpaRepository.findByIdStoreIdAndIdFormatOrderByTotalCopiesPlayedDesc(storeId, format).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<ScryfallCardStats> findCardStatsByLeagueId(UUID leagueId) {
        return cardStatsJpaRepository.findByIdLeagueIdOrderByTotalCopiesPlayedDesc(leagueId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public MetagameAnalyticsSummary computeMetagameAnalytics(
        UUID storeId,
        UUID leagueId,
        UUID tournamentId,
        MagicFormat format
    ) {
        List<TournamentEntity> matchingTournaments = tournamentJpaRepository.findByStoreIdOrderByScheduledAtDesc(storeId)
            .stream()
            .filter(t -> leagueId == null || leagueId.equals(t.getLeagueId()))
            .filter(t -> tournamentId == null || tournamentId.equals(t.getId()))
            .filter(t -> format == null || format == t.getFormat())
            .toList();

        Set<UUID> tournamentIds = matchingTournaments.stream()
            .map(TournamentEntity::getId)
            .collect(Collectors.toSet());

        List<TournamentRegistrationEntity> registrations = new ArrayList<>();
        for (UUID tId : tournamentIds) {
            registrations.addAll(registrationJpaRepository.findByTournamentIdOrderByRegisteredAtAsc(tId));
        }

        long totalDecks = registrations.size();
        BigDecimal avgPrice = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Map<String, Long> colorCounts = new LinkedHashMap<>(Map.of("W", 0L, "U", 0L, "B", 0L, "R", 0L, "G", 0L));
        Map<String, Long> manaCurveCounts = new LinkedHashMap<>(Map.of("1", 0L, "2", 0L, "3", 0L, "4", 0L, "5+", 0L));
        long totalColorInstances = 0;

        if (totalDecks > 0) {
            BigDecimal sumPrices = BigDecimal.ZERO;
            for (TournamentRegistrationEntity reg : registrations) {
                sumPrices = sumPrices.add(reg.getCalculatedDeckPrice());
                DeckSnapshot snapshot = reg.getDeckSnapshot();
                if (snapshot != null && snapshot.cards() != null) {
                    for (DeckSnapshot.SnapshotCardEntry cardEntry : snapshot.cards()) {
                        for (String color : cardEntry.colors()) {
                            String upper = color.toUpperCase();
                            if (colorCounts.containsKey(upper)) {
                                colorCounts.put(upper, colorCounts.get(upper) + cardEntry.quantity());
                                totalColorInstances += cardEntry.quantity();
                            }
                        }
                        int cmcBucket = cardEntry.cmc() != null ? cardEntry.cmc().intValue() : 0;
                        if (cmcBucket >= 1 && cmcBucket <= 4) {
                            String key = String.valueOf(cmcBucket);
                            manaCurveCounts.put(key, manaCurveCounts.get(key) + cardEntry.quantity());
                        } else if (cmcBucket >= 5) {
                            manaCurveCounts.put("5+", manaCurveCounts.get("5+") + cardEntry.quantity());
                        }
                    }
                }
            }
            avgPrice = sumPrices.divide(BigDecimal.valueOf(totalDecks), 2, RoundingMode.HALF_UP);
        }

        Map<String, BigDecimal> colorDistPercent = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : colorCounts.entrySet()) {
            if (totalColorInstances > 0) {
                BigDecimal pct = BigDecimal.valueOf(entry.getValue() * 100.0)
                    .divide(BigDecimal.valueOf(totalColorInstances), 2, RoundingMode.HALF_UP);
                colorDistPercent.put(entry.getKey(), pct);
            } else {
                colorDistPercent.put(entry.getKey(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
        }

        Map<String, BigDecimal> manaCurveAvg = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : manaCurveCounts.entrySet()) {
            if (totalDecks > 0) {
                BigDecimal avg = BigDecimal.valueOf(entry.getValue())
                    .divide(BigDecimal.valueOf(totalDecks), 2, RoundingMode.HALF_UP);
                manaCurveAvg.put(entry.getKey(), avg);
            } else {
                manaCurveAvg.put(entry.getKey(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
        }

        List<ScryfallCardStats> rawStats = leagueId != null
            ? findCardStatsByLeagueId(leagueId)
            : findCardStatsByStoreIdAndFormat(storeId, format);

        List<MetagameAnalyticsSummary.CardUsageSummary> mostPlayed = rawStats.stream()
            .filter(s -> format == null || s.format() == format)
            .map(s -> {
                BigDecimal inclusionRate = totalDecks > 0
                    ? BigDecimal.valueOf(s.decksIncludingCard() * 100.0)
                        .divide(BigDecimal.valueOf(totalDecks), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                return new MetagameAnalyticsSummary.CardUsageSummary(
                    s.oracleId(),
                    s.cardName(),
                    s.typeLine(),
                    s.manaCost(),
                    s.rarity(),
                    s.decksIncludingCard(),
                    s.totalCopiesPlayed(),
                    inclusionRate,
                    s.avgUnitPrice()
                );
            })
            .toList();

        return new MetagameAnalyticsSummary(
            storeId,
            leagueId,
            tournamentId,
            format,
            totalDecks,
            avgPrice,
            colorDistPercent,
            manaCurveAvg,
            mostPlayed
        );
    }
}

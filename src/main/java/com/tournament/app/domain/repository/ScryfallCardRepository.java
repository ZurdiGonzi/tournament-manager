package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.MagicFormat;
import com.tournament.app.domain.model.MetagameAnalyticsSummary;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.ScryfallCardStats;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScryfallCardRepository {

    Optional<ScryfallCard> findById(UUID scryfallId);

    List<ScryfallCard> findByIdIn(Collection<UUID> scryfallIds);

    List<ScryfallCard> findByOracleId(UUID oracleId);

    Optional<ScryfallCard> findFirstByNameIgnoreCase(String name);

    List<ScryfallCard> findByNameInIgnoreCase(Collection<String> names);

    List<ScryfallCard> findByColorContaining(String colorCode);

    List<ScryfallCard> findLegalByFormat(MagicFormat format);

    List<ScryfallCard> saveAll(Collection<ScryfallCard> cards);

    List<ScryfallCardStats> findCardStatsByStoreIdAndFormat(UUID storeId, MagicFormat format);

    List<ScryfallCardStats> findCardStatsByLeagueId(UUID leagueId);

    MetagameAnalyticsSummary computeMetagameAnalytics(
        UUID storeId,
        UUID leagueId,
        UUID tournamentId,
        MagicFormat format
    );
}

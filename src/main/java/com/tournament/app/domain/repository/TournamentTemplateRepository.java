package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.TournamentTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentTemplateRepository {

    Optional<TournamentTemplate> findById(UUID id);

    List<TournamentTemplate> findByStoreId(UUID storeId);

    List<TournamentTemplate> findByStoreIdAndActiveTrue(UUID storeId);

    List<TournamentTemplate> findByLeagueId(UUID leagueId);

    List<TournamentTemplate> findAllActive();

    TournamentTemplate save(TournamentTemplate template);
}

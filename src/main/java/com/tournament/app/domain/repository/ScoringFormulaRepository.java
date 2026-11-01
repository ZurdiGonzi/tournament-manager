package com.tournament.app.domain.repository;

import com.tournament.app.domain.model.ScoringFormula;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoringFormulaRepository {
    ScoringFormula save(ScoringFormula formula);
    Optional<ScoringFormula> findById(UUID id);
    List<ScoringFormula> findByStoreId(UUID storeId);
}

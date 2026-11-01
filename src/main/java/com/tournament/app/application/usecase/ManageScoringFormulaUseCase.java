package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.AdminTournamentDtos.CreateStoreRequest;
import com.tournament.app.application.dto.AdminTournamentDtos.ScoringFormulaResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.StoreResponse;
import com.tournament.app.application.dto.AdminTournamentDtos.UpsertScoringFormulaRequest;
import com.tournament.app.domain.model.DomainExceptions.ResourceNotFoundException;
import com.tournament.app.domain.model.DomainExceptions.UnauthorizedStoreActionException;
import com.tournament.app.domain.model.ScoringFormula;
import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.repository.ScoringFormulaRepository;
import com.tournament.app.domain.repository.StoreRepository;
import com.tournament.app.domain.service.FormulaEvaluationEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ManageScoringFormulaUseCase {

    private final StoreRepository storeRepository;
    private final ScoringFormulaRepository scoringFormulaRepository;
    private final FormulaEvaluationEngine formulaEvaluationEngine;

    public ManageScoringFormulaUseCase(
            StoreRepository storeRepository,
            ScoringFormulaRepository scoringFormulaRepository,
            FormulaEvaluationEngine formulaEvaluationEngine) {
        this.storeRepository = storeRepository;
        this.scoringFormulaRepository = scoringFormulaRepository;
        this.formulaEvaluationEngine = formulaEvaluationEngine;
    }

    @Transactional
    public StoreResponse createStore(UUID ownerId, CreateStoreRequest request) {
        String currency = (request.currency() == null || request.currency().isBlank())
                ? "EUR"
                : request.currency().trim().toUpperCase();
        Store saved = storeRepository.save(new Store(
                UUID.randomUUID(),
                request.name().trim(),
                ownerId,
                currency,
                Instant.now()
        ));
        storeRepository.addMember(saved.id(), ownerId);
        return new StoreResponse(saved.id(), saved.name(), saved.ownerId(), saved.currency(), saved.createdAt());
    }

    @Transactional
    public ScoringFormulaResponse createFormula(
            UUID currentUserId,
            UUID storeId,
            UpsertScoringFormulaRequest request) {
        verifyStoreOwnership(storeId, currentUserId);
        formulaEvaluationEngine.validateSyntax(request.expression());
        ScoringFormula saved = scoringFormulaRepository.save(new ScoringFormula(
                UUID.randomUUID(),
                storeId,
                request.name().trim(),
                request.formulaType(),
                request.expression().trim(),
                request.description(),
                Instant.now()
        ));
        return toResponse(saved);
    }

    @Transactional
    public ScoringFormulaResponse updateFormula(
            UUID currentUserId,
            UUID storeId,
            UUID formulaId,
            UpsertScoringFormulaRequest request) {
        verifyStoreOwnership(storeId, currentUserId);
        ScoringFormula existing = scoringFormulaRepository.findById(formulaId)
                .orElseThrow(() -> new ResourceNotFoundException("ScoringFormula not found: " + formulaId));
        if (!existing.storeId().equals(storeId)) {
            throw new UnauthorizedStoreActionException("Formula does not belong to store: " + storeId);
        }
        formulaEvaluationEngine.validateSyntax(request.expression());
        ScoringFormula updated = scoringFormulaRepository.save(new ScoringFormula(
                existing.id(),
                storeId,
                request.name().trim(),
                request.formulaType(),
                request.expression().trim(),
                request.description(),
                Instant.now()
        ));
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ScoringFormulaResponse> listStoreFormulas(UUID storeId) {
        return scoringFormulaRepository.findByStoreId(storeId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Store verifyStoreOwnership(UUID storeId, UUID currentUserId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found: " + storeId));
        if (!store.isOwnedBy(currentUserId)) {
            throw new UnauthorizedStoreActionException("Only the store owner can perform this operation");
        }
        return store;
    }

    private ScoringFormulaResponse toResponse(ScoringFormula f) {
        return new ScoringFormulaResponse(
                f.id(),
                f.storeId(),
                f.name(),
                f.formulaType(),
                f.expression(),
                f.description(),
                f.updatedAt()
        );
    }
}

package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.FormulaType;
import com.tournament.app.domain.model.ScoringFormula;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scoring_formulas")
public class ScoringFormulaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "formula_type", length = 35, nullable = false)
    private FormulaType formulaType;

    @Column(name = "expression", columnDefinition = "text", nullable = false)
    private String expression;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ScoringFormulaEntity() {
    }

    public static ScoringFormulaEntity fromDomain(ScoringFormula formula) {
        ScoringFormulaEntity e = new ScoringFormulaEntity();
        e.id = formula.id();
        e.storeId = formula.storeId();
        e.name = formula.name();
        e.formulaType = formula.formulaType();
        e.expression = formula.expression();
        e.description = formula.description();
        e.updatedAt = formula.updatedAt();
        return e;
    }

    public ScoringFormula toDomain() {
        return new ScoringFormula(id, storeId, name, formulaType, expression, description, updatedAt);
    }

    public UUID getStoreId() {
        return storeId;
    }
}

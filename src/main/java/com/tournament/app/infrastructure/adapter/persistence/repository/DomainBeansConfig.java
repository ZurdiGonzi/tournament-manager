package com.tournament.app.infrastructure.adapter.persistence.repository;

import com.tournament.app.domain.service.BudgetValidator;
import com.tournament.app.domain.service.DeckAnalyticsService;
import com.tournament.app.domain.service.FormulaEvaluationEngine;
import com.tournament.app.domain.service.LeagueScoringService;
import com.tournament.app.domain.service.SwissPairingEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeansConfig {

    @Bean
    public FormulaEvaluationEngine formulaEvaluationEngine() {
        return new FormulaEvaluationEngine();
    }

    @Bean
    public SwissPairingEngine swissPairingEngine(FormulaEvaluationEngine formulaEvaluationEngine) {
        return new SwissPairingEngine(formulaEvaluationEngine);
    }

    @Bean
    public LeagueScoringService leagueScoringService(FormulaEvaluationEngine formulaEvaluationEngine) {
        return new LeagueScoringService(formulaEvaluationEngine);
    }

    @Bean
    public BudgetValidator budgetValidator() {
        return new BudgetValidator();
    }

    @Bean
    public DeckAnalyticsService deckAnalyticsService() {
        return new DeckAnalyticsService();
    }
}

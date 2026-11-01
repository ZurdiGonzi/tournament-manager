package com.tournament.app.application.usecase;

import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScheduleTournamentUseCase {

    private final TournamentRepository tournamentRepository;

    public ScheduleTournamentUseCase(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional
    public Optional<Tournament> generateIfNotExists(
            UUID storeId,
            UUID templateId,
            String name,
            Instant scheduledAt,
            BigDecimal entryFee,
            String prizeDescription,
            String format,
            BigDecimal maxDeckBudget,
            int maxRounds) {
        if (tournamentRepository.existsByTemplateIdAndScheduledAt(templateId, scheduledAt)) {
            return Optional.empty();
        }
        Tournament created = tournamentRepository.save(new Tournament(
                UUID.randomUUID(),
                storeId,
                templateId,
                null,
                null,
                name,
                scheduledAt,
                entryFee,
                prizeDescription,
                format,
                maxDeckBudget,
                "REGISTRATION_OPEN",
                0,
                maxRounds,
                null,
                null,
                Instant.now()
        ));
        return Optional.of(created);
    }
}

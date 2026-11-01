package com.tournament.app.infrastructure.scheduler;

import com.tournament.app.application.usecase.ScheduleTournamentUseCase;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class TournamentAutoScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final ScheduleTournamentUseCase scheduleTournamentUseCase;

    public TournamentAutoScheduler(
            JdbcTemplate jdbcTemplate,
            ScheduleTournamentUseCase scheduleTournamentUseCase) {
        this.jdbcTemplate = jdbcTemplate;
        this.scheduleTournamentUseCase = scheduleTournamentUseCase;
    }

    @Scheduled(cron = "${tournament.scheduler.recurring-cron:0 0 2 * * *}")
    public void generateRecurringTournaments() {
        Instant nextScheduledSlot = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS);
        jdbcTemplate.query(
                "SELECT id, store_id, name, entry_fee, prize_description, format, max_deck_budget, max_rounds " +
                        "FROM tournament_templates WHERE active = true",
                rs -> {
                    UUID templateId = UUID.fromString(rs.getString("id"));
                    UUID storeId = UUID.fromString(rs.getString("store_id"));
                    String name = rs.getString("name");
                    BigDecimal entryFee = rs.getBigDecimal("entry_fee");
                    String prize = rs.getString("prize_description");
                    String format = rs.getString("format");
                    BigDecimal budget = rs.getBigDecimal("max_deck_budget");
                    int maxRounds = rs.getInt("max_rounds");
                    scheduleTournamentUseCase.generateIfNotExists(
                            storeId, templateId, name, nextScheduledSlot, entryFee, prize, format, budget, maxRounds
                    );
                }
        );
    }

    @Configuration
    @EnableScheduling
    public static class SchedulingConfig {
    }
}

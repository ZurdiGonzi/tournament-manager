package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.TournamentRegistration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tournament_registrations")
public class TournamentRegistrationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "deck_id", nullable = false)
    private UUID deckId;

    @Column(name = "status", length = 25, nullable = false)
    private String status;

    @Column(name = "calculated_deck_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal calculatedDeckPrice;

    @Column(name = "commander_name", length = 200)
    private String commanderName;

    @Column(name = "commander_oracle_id")
    private UUID commanderOracleId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "color_identity", columnDefinition = "text[]", nullable = false)
    private String[] colorIdentity;

    @Column(name = "tournament_points", precision = 10, scale = 2, nullable = false)
    private BigDecimal tournamentPoints;

    @Column(name = "calculated_league_matchday_points", precision = 10, scale = 2, nullable = false)
    private BigDecimal calculatedLeagueMatchdayPoints;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "deck_snapshot", columnDefinition = "jsonb", nullable = false)
    private DeckSnapshot deckSnapshot;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    protected TournamentRegistrationEntity() {
    }

    public static TournamentRegistrationEntity fromDomain(TournamentRegistration r) {
        TournamentRegistrationEntity e = new TournamentRegistrationEntity();
        e.id = r.id();
        e.tournamentId = r.tournamentId();
        e.userId = r.userId();
        e.deckId = r.deckId();
        e.status = r.status();
        e.calculatedDeckPrice = r.calculatedDeckPrice();
        e.commanderName = r.commanderName();
        e.commanderOracleId = r.commanderOracleId();
        e.colorIdentity = r.colorIdentity() == null ? new String[0] : r.colorIdentity().toArray(String[]::new);
        e.tournamentPoints = r.tournamentPoints();
        e.calculatedLeagueMatchdayPoints = r.calculatedLeagueMatchdayPoints();
        e.deckSnapshot = r.deckSnapshot();
        e.registeredAt = r.registeredAt();
        return e;
    }

    public TournamentRegistration toDomain() {
        List<String> colors = colorIdentity == null ? List.of() : Arrays.asList(colorIdentity);
        return new TournamentRegistration(
                id,
                tournamentId,
                userId,
                deckId,
                status,
                calculatedDeckPrice,
                commanderName,
                commanderOracleId,
                colors,
                tournamentPoints,
                calculatedLeagueMatchdayPoints,
                deckSnapshot,
                registeredAt
        );
    }

    public UUID getTournamentId() {
        return tournamentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getDeckId() {
        return deckId;
    }
}

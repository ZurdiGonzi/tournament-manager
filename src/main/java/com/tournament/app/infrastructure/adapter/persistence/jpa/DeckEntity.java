package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.Deck;
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
@Table(name = "decks")
public class DeckEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "moxfield_url", length = 500, nullable = false)
    private String moxfieldUrl;

    @Column(name = "moxfield_deck_id", length = 64, nullable = false)
    private String moxfieldDeckId;

    @Column(name = "format", length = 30, nullable = false)
    private String format;

    @Column(name = "commander_oracle_id")
    private UUID commanderOracleId;

    @Column(name = "commander_name", length = 200)
    private String commanderName;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "color_identity", columnDefinition = "text[]", nullable = false)
    private String[] colorIdentity;

    @Column(name = "last_calculated_price", precision = 10, scale = 2)
    private BigDecimal lastCalculatedPrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DeckEntity() {
    }

    public static DeckEntity fromDomain(Deck deck) {
        DeckEntity e = new DeckEntity();
        e.id = deck.id();
        e.userId = deck.userId();
        e.name = deck.name();
        e.moxfieldUrl = deck.moxfieldUrl();
        e.moxfieldDeckId = deck.moxfieldDeckId();
        e.format = deck.format();
        e.commanderOracleId = deck.commanderOracleId();
        e.commanderName = deck.commanderName();
        e.colorIdentity = deck.colorIdentity() == null
                ? new String[0]
                : deck.colorIdentity().toArray(String[]::new);
        e.lastCalculatedPrice = deck.lastCalculatedPrice();
        e.createdAt = deck.createdAt();
        e.updatedAt = deck.updatedAt();
        return e;
    }

    public Deck toDomain() {
        List<String> colors = colorIdentity == null ? List.of() : Arrays.asList(colorIdentity);
        return new Deck(
                id,
                userId,
                name,
                moxfieldUrl,
                moxfieldDeckId,
                format,
                commanderOracleId,
                commanderName,
                colors,
                lastCalculatedPrice,
                createdAt,
                updatedAt
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMoxfieldDeckId() {
        return moxfieldDeckId;
    }
}

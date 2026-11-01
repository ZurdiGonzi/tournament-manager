package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.ScryfallCard;
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
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "scryfall_cards")
public class ScryfallCardEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "oracle_id", nullable = false)
    private UUID oracleId;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "mana_cost", length = 60)
    private String manaCost;

    @Column(name = "cmc", precision = 5, scale = 2, nullable = false)
    private BigDecimal cmc;

    @Column(name = "type_line", length = 200, nullable = false)
    private String typeLine;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "colors", columnDefinition = "text[]", nullable = false)
    private String[] colors;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "color_identity", columnDefinition = "text[]", nullable = false)
    private String[] colorIdentity;

    @Column(name = "rarity", length = 20, nullable = false)
    private String rarity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "legalities", columnDefinition = "jsonb", nullable = false)
    private Map<String, String> legalities;

    @Column(name = "price_eur", precision = 10, scale = 2)
    private BigDecimal priceEur;

    @Column(name = "price_usd", precision = 10, scale = 2)
    private BigDecimal priceUsd;

    @Column(name = "image_uri", length = 500)
    private String imageUri;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    protected ScryfallCardEntity() {
    }

    public static ScryfallCardEntity fromDomain(ScryfallCard card) {
        ScryfallCardEntity e = new ScryfallCardEntity();
        e.id = card.id();
        e.oracleId = card.oracleId();
        e.name = card.name();
        e.manaCost = card.manaCost();
        e.cmc = card.cmc();
        e.typeLine = card.typeLine();
        e.colors = card.colors() == null ? new String[0] : card.colors().toArray(String[]::new);
        e.colorIdentity = card.colorIdentity() == null ? new String[0] : card.colorIdentity().toArray(String[]::new);
        e.rarity = card.rarity();
        e.legalities = card.legalities();
        e.priceEur = card.priceEur();
        e.priceUsd = card.priceUsd();
        e.imageUri = card.imageUri();
        e.lastSyncedAt = card.lastSyncedAt();
        return e;
    }

    public ScryfallCard toDomain() {
        return new ScryfallCard(
                id,
                oracleId,
                name,
                manaCost,
                cmc,
                typeLine,
                colors == null ? List.of() : Arrays.asList(colors),
                colorIdentity == null ? List.of() : Arrays.asList(colorIdentity),
                rarity,
                legalities == null ? Map.of() : legalities,
                priceEur,
                priceUsd,
                imageUri,
                lastSyncedAt
        );
    }
}

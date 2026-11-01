package com.tournament.app.infrastructure.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
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
    private List<String> colors;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "color_identity", columnDefinition = "text[]", nullable = false)
    private List<String> colorIdentity;

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

    public ScryfallCardEntity() {
    }

    public ScryfallCardEntity(
        UUID id,
        UUID oracleId,
        String name,
        String manaCost,
        BigDecimal cmc,
        String typeLine,
        List<String> colors,
        List<String> colorIdentity,
        String rarity,
        Map<String, String> legalities,
        BigDecimal priceEur,
        BigDecimal priceUsd,
        String imageUri,
        Instant lastSyncedAt
    ) {
        this.id = id;
        this.oracleId = oracleId;
        this.name = name;
        this.manaCost = manaCost;
        this.cmc = cmc;
        this.typeLine = typeLine;
        this.colors = colors;
        this.colorIdentity = colorIdentity;
        this.rarity = rarity;
        this.legalities = legalities;
        this.priceEur = priceEur;
        this.priceUsd = priceUsd;
        this.imageUri = imageUri;
        this.lastSyncedAt = lastSyncedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOracleId() {
        return oracleId;
    }

    public void setOracleId(UUID oracleId) {
        this.oracleId = oracleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManaCost() {
        return manaCost;
    }

    public void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    public BigDecimal getCmc() {
        return cmc;
    }

    public void setCmc(BigDecimal cmc) {
        this.cmc = cmc;
    }

    public String getTypeLine() {
        return typeLine;
    }

    public void setTypeLine(String typeLine) {
        this.typeLine = typeLine;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public List<String> getColorIdentity() {
        return colorIdentity;
    }

    public void setColorIdentity(List<String> colorIdentity) {
        this.colorIdentity = colorIdentity;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public Map<String, String> getLegalities() {
        return legalities;
    }

    public void setLegalities(Map<String, String> legalities) {
        this.legalities = legalities;
    }

    public BigDecimal getPriceEur() {
        return priceEur;
    }

    public void setPriceEur(BigDecimal priceEur) {
        this.priceEur = priceEur;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(BigDecimal priceUsd) {
        this.priceUsd = priceUsd;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}

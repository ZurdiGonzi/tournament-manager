package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.MagicFormat;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "v_scryfall_card_stats")
public class ScryfallCardStatsViewEntity {

    @EmbeddedId
    private ScryfallCardStatsViewId id;

    @Column(name = "card_name", insertable = false, updatable = false)
    private String cardName;

    @Column(name = "type_line", insertable = false, updatable = false)
    private String typeLine;

    @Column(name = "mana_cost", insertable = false, updatable = false)
    private String manaCost;

    @Column(name = "cmc", insertable = false, updatable = false)
    private BigDecimal cmc;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "colors", columnDefinition = "text[]", insertable = false, updatable = false)
    private List<String> colors;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "color_identity", columnDefinition = "text[]", insertable = false, updatable = false)
    private List<String> colorIdentity;

    @Column(name = "rarity", insertable = false, updatable = false)
    private String rarity;

    @Column(name = "decks_including_card", insertable = false, updatable = false)
    private long decksIncludingCard;

    @Column(name = "total_copies_played", insertable = false, updatable = false)
    private long totalCopiesPlayed;

    @Column(name = "avg_unit_price", insertable = false, updatable = false)
    private BigDecimal avgUnitPrice;

    public ScryfallCardStatsViewEntity() {
    }

    public ScryfallCardStatsViewId getId() {
        return id;
    }

    public UUID getStoreId() {
        return id != null ? id.getStoreId() : null;
    }

    public UUID getLeagueId() {
        return id != null ? id.getLeagueId() : null;
    }

    public MagicFormat getFormat() {
        return id != null ? id.getFormat() : null;
    }

    public UUID getOracleId() {
        return id != null ? id.getOracleId() : null;
    }

    public String getCardName() {
        return cardName;
    }

    public String getTypeLine() {
        return typeLine;
    }

    public String getManaCost() {
        return manaCost;
    }

    public BigDecimal getCmc() {
        return cmc;
    }

    public List<String> getColors() {
        return colors;
    }

    public List<String> getColorIdentity() {
        return colorIdentity;
    }

    public String getRarity() {
        return rarity;
    }

    public long getDecksIncludingCard() {
        return decksIncludingCard;
    }

    public long getTotalCopiesPlayed() {
        return totalCopiesPlayed;
    }

    public BigDecimal getAvgUnitPrice() {
        return avgUnitPrice;
    }
}

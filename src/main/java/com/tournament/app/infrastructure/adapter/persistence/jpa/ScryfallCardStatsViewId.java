package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.MagicFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ScryfallCardStatsViewId implements Serializable {

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "league_id")
    private UUID leagueId;

    @Enumerated(EnumType.STRING)
    @Column(name = "format")
    private MagicFormat format;

    @Column(name = "oracle_id")
    private UUID oracleId;

    public ScryfallCardStatsViewId() {
    }

    public ScryfallCardStatsViewId(UUID storeId, UUID leagueId, MagicFormat format, UUID oracleId) {
        this.storeId = storeId;
        this.leagueId = leagueId;
        this.format = format;
        this.oracleId = oracleId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public UUID getLeagueId() {
        return leagueId;
    }

    public MagicFormat getFormat() {
        return format;
    }

    public UUID getOracleId() {
        return oracleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScryfallCardStatsViewId that)) return false;
        return Objects.equals(storeId, that.storeId)
            && Objects.equals(leagueId, that.leagueId)
            && format == that.format
            && Objects.equals(oracleId, that.oracleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, leagueId, format, oracleId);
    }
}

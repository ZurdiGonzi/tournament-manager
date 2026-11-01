package com.tournament.app.infrastructure.adapter.persistence.jpa;

import com.tournament.app.domain.model.BoardSection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "registration_deck_cards")
public class RegistrationDeckCardEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "registration_id", nullable = false)
    private UUID registrationId;

    @Column(name = "scryfall_card_id", nullable = false)
    private UUID scryfallCardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_section", length = 20, nullable = false)
    private BoardSection boardSection;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPriceSnapshot;

    public RegistrationDeckCardEntity() {
    }

    public RegistrationDeckCardEntity(
        UUID id,
        UUID registrationId,
        UUID scryfallCardId,
        BoardSection boardSection,
        int quantity,
        BigDecimal unitPriceSnapshot
    ) {
        this.id = id;
        this.registrationId = registrationId;
        this.scryfallCardId = scryfallCardId;
        this.boardSection = boardSection;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(UUID registrationId) {
        this.registrationId = registrationId;
    }

    public UUID getScryfallCardId() {
        return scryfallCardId;
    }

    public void setScryfallCardId(UUID scryfallCardId) {
        this.scryfallCardId = scryfallCardId;
    }

    public BoardSection getBoardSection() {
        return boardSection;
    }

    public void setBoardSection(BoardSection boardSection) {
        this.boardSection = boardSection;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPriceSnapshot() {
        return unitPriceSnapshot;
    }

    public void setUnitPriceSnapshot(BigDecimal unitPriceSnapshot) {
        this.unitPriceSnapshot = unitPriceSnapshot;
    }
}

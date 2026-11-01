package com.tournament.app.infrastructure.adapter.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "board_section", length = 20, nullable = false)
    private String boardSection;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPriceSnapshot;

    protected RegistrationDeckCardEntity() {
    }

    public RegistrationDeckCardEntity(
            UUID id,
            UUID registrationId,
            UUID scryfallCardId,
            String boardSection,
            int quantity,
            BigDecimal unitPriceSnapshot) {
        this.id = id;
        this.registrationId = registrationId;
        this.scryfallCardId = scryfallCardId;
        this.boardSection = boardSection;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
    }
}

package com.tournament.app.domain.service;

import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.ScryfallCard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BudgetValidator {

    public record DeckCardInput(
            ScryfallCard card,
            int quantity,
            String boardSection
    ) {
    }

    public record ValidatedDeckResult(
            DeckSnapshot snapshot,
            BigDecimal totalPrice,
            UUID commanderOracleId,
            String commanderName,
            List<String> colorIdentity
    ) {
    }

    public ValidatedDeckResult validateAndBuildSnapshot(
            String moxfieldDeckId,
            String moxfieldUrl,
            String deckName,
            String requiredFormat,
            BigDecimal maxDeckBudget,
            String currency,
            List<DeckCardInput> entries) {

        if (entries == null || entries.isEmpty()) {
            throw new DomainValidationException("Deck cannot be empty");
        }

        List<DeckSnapshot.CardEntry> commanders = new ArrayList<>();
        List<DeckSnapshot.CardEntry> mainboard = new ArrayList<>();
        List<DeckSnapshot.CardEntry> sideboard = new ArrayList<>();

        Map<UUID, Integer> copiesByOracleId = new HashMap<>();
        Map<UUID, String> nameByOracleId = new HashMap<>();
        Set<String> deckColorIdentity = new LinkedHashSet<>();
        Set<String> commanderColorIdentity = new LinkedHashSet<>();

        BigDecimal totalPrice = BigDecimal.ZERO;
        int mainboardCount = 0;
        int commanderCount = 0;
        int sideboardCount = 0;

        for (DeckCardInput input : entries) {
            ScryfallCard card = input.card();
            if (card == null) {
                throw new DomainValidationException("Missing Scryfall metadata for a card in the deck");
            }
            if (!card.isLegalIn(requiredFormat)) {
                throw new DomainValidationException(
                        "Card '" + card.name() + "' is not legal in format " + requiredFormat);
            }

            BigDecimal unitPrice = card.resolvePrice(currency).setScale(2, RoundingMode.HALF_UP);
            BigDecimal linePrice = unitPrice.multiply(BigDecimal.valueOf(input.quantity()));
            totalPrice = totalPrice.add(linePrice);

            DeckSnapshot.CardEntry snapshotEntry = new DeckSnapshot.CardEntry(
                    card.id(),
                    card.oracleId(),
                    card.name(),
                    input.quantity(),
                    card.manaCost(),
                    card.cmc(),
                    card.typeLine(),
                    card.colors(),
                    card.colorIdentity(),
                    card.rarity(),
                    unitPrice
            );

            if (card.colorIdentity() != null) {
                deckColorIdentity.addAll(card.colorIdentity());
            }

            String section = input.boardSection() == null ? "MAINBOARD" : input.boardSection().toUpperCase();
            switch (section) {
                case "COMMANDER" -> {
                    commanders.add(snapshotEntry);
                    commanderCount += input.quantity();
                    if (card.colorIdentity() != null) {
                        commanderColorIdentity.addAll(card.colorIdentity());
                    }
                }
                case "SIDEBOARD" -> {
                    sideboard.add(snapshotEntry);
                    sideboardCount += input.quantity();
                }
                default -> {
                    mainboard.add(snapshotEntry);
                    mainboardCount += input.quantity();
                }
            }

            if (!card.isBasicLand()) {
                copiesByOracleId.merge(card.oracleId(), input.quantity(), Integer::sum);
                nameByOracleId.put(card.oracleId(), card.name());
            }
        }

        if ("COMMANDER".equalsIgnoreCase(requiredFormat)) {
            if (commanderCount < 1 || commanderCount > 2) {
                throw new DomainValidationException("Commander deck must have 1 or 2 commanders");
            }
            int totalCommanderCards = commanderCount + mainboardCount;
            if (totalCommanderCards != 100) {
                throw new DomainValidationException(
                        "Commander deck must contain exactly 100 cards (found " + totalCommanderCards + ")");
            }
            for (Map.Entry<UUID, Integer> entry : copiesByOracleId.entrySet()) {
                if (entry.getValue() > 1) {
                    throw new DomainValidationException(
                            "Commander singleton rule violated by card: " + nameByOracleId.get(entry.getKey()));
                }
            }
            if (!commanderColorIdentity.containsAll(deckColorIdentity)) {
                throw new DomainValidationException(
                        "Deck contains cards outside the commander's color identity: " + deckColorIdentity);
            }
        } else if ("STANDARD".equalsIgnoreCase(requiredFormat)) {
            if (mainboardCount < 60) {
                throw new DomainValidationException(
                        "Standard mainboard must contain at least 60 cards (found " + mainboardCount + ")");
            }
            if (sideboardCount > 15) {
                throw new DomainValidationException(
                        "Standard sideboard cannot exceed 15 cards (found " + sideboardCount + ")");
            }
            for (Map.Entry<UUID, Integer> entry : copiesByOracleId.entrySet()) {
                if (entry.getValue() > 4) {
                    throw new DomainValidationException(
                            "Standard 4-copy limit violated by card: " + nameByOracleId.get(entry.getKey()));
                }
            }
        }

        if (maxDeckBudget != null && totalPrice.compareTo(maxDeckBudget) > 0) {
            throw new DomainValidationException(String.format(
                    "Deck price (%.2f %s) exceeds tournament max budget (%.2f %s)",
                    totalPrice, currency, maxDeckBudget, currency));
        }

        UUID commanderOracleId = commanders.isEmpty() ? null : commanders.getFirst().oracleId();
        String commanderName = commanders.isEmpty() ? deckName : commanders.getFirst().name();
        List<String> finalColorIdentity = !commanderColorIdentity.isEmpty()
                ? List.copyOf(commanderColorIdentity)
                : List.copyOf(deckColorIdentity);

        DeckSnapshot snapshot = new DeckSnapshot(
                moxfieldDeckId,
                moxfieldUrl,
                deckName,
                requiredFormat.toUpperCase(),
                totalPrice.setScale(2, RoundingMode.HALF_UP),
                currency,
                Instant.now(),
                List.copyOf(commanders),
                List.copyOf(mainboard),
                List.copyOf(sideboard)
        );

        return new ValidatedDeckResult(
                snapshot,
                totalPrice.setScale(2, RoundingMode.HALF_UP),
                commanderOracleId,
                commanderName,
                finalColorIdentity
        );
    }
}

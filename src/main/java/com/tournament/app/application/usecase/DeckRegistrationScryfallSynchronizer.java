package com.tournament.app.application.usecase;

import com.tournament.app.domain.model.BoardSection;
import com.tournament.app.domain.model.CurrencyCode;
import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.DeckSnapshot;
import com.tournament.app.domain.model.RegistrationDeckCard;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.model.TournamentRestrictions;
import com.tournament.app.domain.repository.ScryfallCardRepository;
import com.tournament.app.domain.service.BudgetValidator;
import com.tournament.app.domain.service.ScryfallCardGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Application service invoked when registering/submitting a deck:
 * 1. Resolves fresh cards from the local `scryfall_cards` table.
 * 2. Communicates with Scryfall API (`ScryfallCardGateway`) for missing or expired cards.
 * 3. Persists newly synced cards into `scryfall_cards`.
 * 4. Validates format legality and budget via `BudgetValidator`.
 * 5. Builds the immutable `DeckSnapshot` and relational `RegistrationDeckCard` rows.
 */
@Service
public class DeckRegistrationScryfallSynchronizer {

    private final ScryfallCardRepository scryfallCardRepository;
    private final ScryfallCardGateway scryfallCardGateway;
    private final BudgetValidator budgetValidator;
    private final Duration cacheTtl;

    public DeckRegistrationScryfallSynchronizer(
        ScryfallCardRepository scryfallCardRepository,
        ScryfallCardGateway scryfallCardGateway,
        @Value("${scryfall.cache-ttl-hours:24}") long cacheTtlHours
    ) {
        this.scryfallCardRepository = scryfallCardRepository;
        this.scryfallCardGateway = scryfallCardGateway;
        this.budgetValidator = new BudgetValidator();
        this.cacheTtl = Duration.ofHours(cacheTtlHours);
    }

    public record RawDeckCardLine(
        UUID scryfallId,
        String cardName,
        BoardSection boardSection,
        int quantity
    ) {
        public RawDeckCardLine {
            if (scryfallId == null && (cardName == null || cardName.isBlank())) {
                throw new IllegalArgumentException("Either scryfallId or cardName must be provided");
            }
            Objects.requireNonNull(boardSection, "boardSection must not be null");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0");
            }
        }
    }

    public record SynchronizedDeckRegistrationResult(
        BudgetValidator.ValidationResult validationResult,
        DeckSnapshot deckSnapshot,
        List<RegistrationDeckCard> registrationDeckCards,
        List<ScryfallCard> resolvedScryfallCards,
        List<String> unresolvedCards
    ) {}

    @Transactional
    public SynchronizedDeckRegistrationResult synchronizeAndValidateDeck(
        UUID registrationId,
        Deck deck,
        List<RawDeckCardLine> rawLines,
        TournamentRestrictions restrictions,
        CurrencyCode storeCurrency
    ) {
        Objects.requireNonNull(deck, "deck must not be null");
        Objects.requireNonNull(rawLines, "rawLines must not be null");
        Objects.requireNonNull(restrictions, "restrictions must not be null");
        CurrencyCode currency = storeCurrency == null ? CurrencyCode.EUR : storeCurrency;
        Instant validThreshold = Instant.now().minus(cacheTtl);

        Set<UUID> requestedIds = new LinkedHashSet<>();
        Set<String> requestedNames = new LinkedHashSet<>();
        for (RawDeckCardLine line : rawLines) {
            if (line.scryfallId() != null) {
                requestedIds.add(line.scryfallId());
            } else if (line.cardName() != null) {
                requestedNames.add(line.cardName().trim());
            }
        }

        Map<UUID, ScryfallCard> cardsById = new HashMap<>();
        Map<String, ScryfallCard> cardsByLowerName = new HashMap<>();

        for (ScryfallCard cached : scryfallCardRepository.findByIdIn(requestedIds)) {
            cardsById.put(cached.id(), cached);
            cardsByLowerName.put(cached.name().toLowerCase(Locale.ROOT), cached);
        }
        for (ScryfallCard cached : scryfallCardRepository.findByNameInIgnoreCase(requestedNames)) {
            cardsById.put(cached.id(), cached);
            cardsByLowerName.put(cached.name().toLowerCase(Locale.ROOT), cached);
        }

        List<UUID> idsToSync = requestedIds.stream()
            .filter(id -> {
                ScryfallCard c = cardsById.get(id);
                return c == null || c.lastSyncedAt().isBefore(validThreshold);
            })
            .toList();

        List<String> namesToSync = requestedNames.stream()
            .filter(name -> {
                ScryfallCard c = cardsByLowerName.get(name.toLowerCase(Locale.ROOT));
                return c == null || c.lastSyncedAt().isBefore(validThreshold);
            })
            .toList();

        List<ScryfallCard> freshlyFetched = new ArrayList<>();
        if (!idsToSync.isEmpty()) {
            freshlyFetched.addAll(scryfallCardGateway.fetchCardsByIds(idsToSync));
        }
        if (!namesToSync.isEmpty()) {
            freshlyFetched.addAll(scryfallCardGateway.fetchCardsByNames(namesToSync));
        }

        if (!freshlyFetched.isEmpty()) {
            List<ScryfallCard> persisted = scryfallCardRepository.saveAll(freshlyFetched);
            for (ScryfallCard card : persisted) {
                cardsById.put(card.id(), card);
                cardsByLowerName.put(card.name().toLowerCase(Locale.ROOT), card);
            }
        }

        List<BudgetValidator.DeckCardSubmission> submissions = new ArrayList<>();
        List<DeckSnapshot.SnapshotCardEntry> snapshotEntries = new ArrayList<>();
        List<RegistrationDeckCard> registrationCards = new ArrayList<>();
        List<ScryfallCard> resolvedCards = new ArrayList<>();
        List<String> unresolved = new ArrayList<>();

        UUID effectiveRegistrationId = registrationId != null ? registrationId : UUID.randomUUID();

        for (RawDeckCardLine line : rawLines) {
            ScryfallCard card = line.scryfallId() != null
                ? cardsById.get(line.scryfallId())
                : cardsByLowerName.get(line.cardName().trim().toLowerCase(Locale.ROOT));

            if (card == null) {
                unresolved.add(line.cardName() != null ? line.cardName() : line.scryfallId().toString());
                continue;
            }

            resolvedCards.add(card);
            submissions.add(new BudgetValidator.DeckCardSubmission(card, line.boardSection(), line.quantity()));
            BigDecimal unitPrice = card.resolvePriceForCurrency(currency).setScale(2, RoundingMode.HALF_UP);

            snapshotEntries.add(new DeckSnapshot.SnapshotCardEntry(
                card.id(),
                card.oracleId(),
                card.name(),
                line.boardSection(),
                line.quantity(),
                card.typeLine(),
                card.manaCost(),
                card.cmc(),
                card.colors(),
                unitPrice
            ));

            registrationCards.add(new RegistrationDeckCard(
                UUID.randomUUID(),
                effectiveRegistrationId,
                card.id(),
                line.boardSection(),
                line.quantity(),
                unitPrice
            ));
        }

        BudgetValidator.ValidationResult validationResult = budgetValidator.validate(
            submissions,
            restrictions,
            currency
        );

        DeckSnapshot snapshot = new DeckSnapshot(
            deck.id(),
            deck.name(),
            deck.moxfieldDeckId(),
            restrictions.format(),
            validationResult.calculatedDeckPrice(),
            Instant.now(),
            snapshotEntries
        );

        return new SynchronizedDeckRegistrationResult(
            validationResult,
            snapshot,
            registrationCards,
            resolvedCards,
            unresolved
        );
    }
}

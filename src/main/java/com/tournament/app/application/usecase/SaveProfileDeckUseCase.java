package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.PlayerDeckDtos.ProfileDeckResponse;
import com.tournament.app.application.dto.PlayerDeckDtos.SaveProfileDeckRequest;
import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.DomainExceptions.ResourceNotFoundException;
import com.tournament.app.domain.model.DomainExceptions.UnauthorizedStoreActionException;
import com.tournament.app.domain.model.ScryfallCard;
import com.tournament.app.domain.repository.DeckRepository;
import com.tournament.app.domain.repository.ScryfallCardRepository;
import com.tournament.app.domain.service.BudgetValidator;
import com.tournament.app.domain.service.BudgetValidator.DeckCardInput;
import com.tournament.app.domain.service.BudgetValidator.ValidatedDeckResult;
import com.tournament.app.infrastructure.adapter.moxfield.MoxfieldClient;
import com.tournament.app.infrastructure.adapter.moxfield.MoxfieldDeckResponse;
import com.tournament.app.infrastructure.adapter.moxfield.MoxfieldDeckResponse.MoxfieldCardEntry;
import com.tournament.app.infrastructure.adapter.scryfall.ScryfallClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SaveProfileDeckUseCase {

    private final DeckRepository deckRepository;
    private final ScryfallCardRepository scryfallCardRepository;
    private final MoxfieldClient moxfieldClient;
    private final ScryfallClient scryfallClient;
    private final BudgetValidator budgetValidator;

    public SaveProfileDeckUseCase(
            DeckRepository deckRepository,
            ScryfallCardRepository scryfallCardRepository,
            MoxfieldClient moxfieldClient,
            ScryfallClient scryfallClient,
            BudgetValidator budgetValidator) {
        this.deckRepository = deckRepository;
        this.scryfallCardRepository = scryfallCardRepository;
        this.moxfieldClient = moxfieldClient;
        this.scryfallClient = scryfallClient;
        this.budgetValidator = budgetValidator;
    }

    @Transactional
    public ProfileDeckResponse saveToProfile(UUID userId, SaveProfileDeckRequest request) {
        String format = request.format().trim().toUpperCase();
        MoxfieldDeckResponse moxfieldDeck = moxfieldClient.fetchDeck(request.moxfieldUrl());
        List<DeckCardInput> resolvedEntries = syncAndResolveScryfallCards(moxfieldDeck);

        ValidatedDeckResult validated = budgetValidator.validateAndBuildSnapshot(
                moxfieldDeck.moxfieldDeckId(),
                request.moxfieldUrl().trim(),
                request.name().trim(),
                format,
                null,
                "EUR",
                resolvedEntries
        );

        UUID existingId = deckRepository.findByUserIdAndMoxfieldDeckId(userId, moxfieldDeck.moxfieldDeckId())
                .map(Deck::id)
                .orElse(UUID.randomUUID());

        Deck saved = deckRepository.save(new Deck(
                existingId,
                userId,
                request.name().trim(),
                request.moxfieldUrl().trim(),
                moxfieldDeck.moxfieldDeckId(),
                format,
                validated.commanderOracleId(),
                validated.commanderName(),
                validated.colorIdentity(),
                validated.totalPrice(),
                Instant.now(),
                Instant.now()
        ));

        int totalCards = resolvedEntries.stream().mapToInt(DeckCardInput::quantity).sum();
        return toResponse(saved, totalCards);
    }

    @Transactional(readOnly = true)
    public List<ProfileDeckResponse> listProfileDecks(UUID userId) {
        return deckRepository.findByUserId(userId).stream()
                .map(d -> toResponse(d, "COMMANDER".equals(d.format()) ? 100 : 60))
                .toList();
    }

    @Transactional
    public void deleteProfileDeck(UUID userId, UUID deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + deckId));
        if (!deck.userId().equals(userId)) {
            throw new UnauthorizedStoreActionException("You do not own this deck");
        }
        deckRepository.deleteById(deckId);
    }

    public List<DeckCardInput> syncAndResolveScryfallCards(MoxfieldDeckResponse moxfieldDeck) {
        Set<UUID> allCardIds = new LinkedHashSet<>();
        moxfieldDeck.commanders().forEach(c -> allCardIds.add(c.scryfallId()));
        moxfieldDeck.mainboard().forEach(c -> allCardIds.add(c.scryfallId()));
        moxfieldDeck.sideboard().forEach(c -> allCardIds.add(c.scryfallId()));

        List<UUID> idList = new ArrayList<>(allCardIds);
        Map<UUID, ScryfallCard> cached = new HashMap<>(scryfallCardRepository.findByIds(idList));

        List<UUID> missingIds = idList.stream().filter(id -> !cached.containsKey(id)).toList();
        if (!missingIds.isEmpty()) {
            List<ScryfallCard> fetched = scryfallClient.fetchCardsByIds(missingIds);
            scryfallCardRepository.saveAll(fetched);
            fetched.forEach(c -> cached.put(c.id(), c));
        }

        List<DeckCardInput> inputs = new ArrayList<>();
        appendSection(inputs, moxfieldDeck.commanders(), cached, "COMMANDER");
        appendSection(inputs, moxfieldDeck.mainboard(), cached, "MAINBOARD");
        appendSection(inputs, moxfieldDeck.sideboard(), cached, "SIDEBOARD");
        return inputs;
    }

    private void appendSection(
            List<DeckCardInput> inputs,
            List<MoxfieldCardEntry> entries,
            Map<UUID, ScryfallCard> cached,
            String section) {
        if (entries == null) {
            return;
        }
        for (MoxfieldCardEntry entry : entries) {
            inputs.add(new DeckCardInput(cached.get(entry.scryfallId()), entry.quantity(), section));
        }
    }

    private ProfileDeckResponse toResponse(Deck deck, int cardCount) {
        return new ProfileDeckResponse(
                deck.id(),
                deck.name(),
                deck.moxfieldUrl(),
                deck.format(),
                deck.commanderName(),
                deck.commanderOracleId(),
                deck.colorIdentity(),
                deck.lastCalculatedPrice(),
                "EUR",
                cardCount
        );
    }
}

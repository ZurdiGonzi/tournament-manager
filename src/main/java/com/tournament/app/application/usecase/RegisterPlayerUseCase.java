package com.tournament.app.application.usecase;

import com.tournament.app.application.dto.PlayerDeckDtos.TournamentRegistrationRequest;
import com.tournament.app.application.dto.PlayerDeckDtos.TournamentRegistrationResponse;
import com.tournament.app.domain.model.Deck;
import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;
import com.tournament.app.domain.model.DomainExceptions.InvalidTournamentStateException;
import com.tournament.app.domain.model.DomainExceptions.ResourceNotFoundException;
import com.tournament.app.domain.model.DomainExceptions.UnauthorizedStoreActionException;
import com.tournament.app.domain.model.Store;
import com.tournament.app.domain.model.Tournament;
import com.tournament.app.domain.model.TournamentRegistration;
import com.tournament.app.domain.repository.DeckRepository;
import com.tournament.app.domain.repository.RegistrationRepository;
import com.tournament.app.domain.repository.StoreRepository;
import com.tournament.app.domain.repository.TournamentRepository;
import com.tournament.app.domain.service.BudgetValidator;
import com.tournament.app.domain.service.BudgetValidator.DeckCardInput;
import com.tournament.app.domain.service.BudgetValidator.ValidatedDeckResult;
import com.tournament.app.infrastructure.adapter.moxfield.MoxfieldClient;
import com.tournament.app.infrastructure.adapter.moxfield.MoxfieldDeckResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RegisterPlayerUseCase {

    private final TournamentRepository tournamentRepository;
    private final StoreRepository storeRepository;
    private final DeckRepository deckRepository;
    private final RegistrationRepository registrationRepository;
    private final MoxfieldClient moxfieldClient;
    private final SaveProfileDeckUseCase saveProfileDeckUseCase;
    private final BudgetValidator budgetValidator;

    public RegisterPlayerUseCase(
            TournamentRepository tournamentRepository,
            StoreRepository storeRepository,
            DeckRepository deckRepository,
            RegistrationRepository registrationRepository,
            MoxfieldClient moxfieldClient,
            SaveProfileDeckUseCase saveProfileDeckUseCase,
            BudgetValidator budgetValidator) {
        this.tournamentRepository = tournamentRepository;
        this.storeRepository = storeRepository;
        this.deckRepository = deckRepository;
        this.registrationRepository = registrationRepository;
        this.moxfieldClient = moxfieldClient;
        this.saveProfileDeckUseCase = saveProfileDeckUseCase;
        this.budgetValidator = budgetValidator;
    }

    @Transactional
    public TournamentRegistrationResponse execute(
            UUID userId,
            UUID tournamentId,
            TournamentRegistrationRequest request) {

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found: " + tournamentId));

        if (!"REGISTRATION_OPEN".equals(tournament.status())) {
            throw new InvalidTournamentStateException(
                    "Tournament is not open for registration (current state: " + tournament.status() + ")");
        }

        if (registrationRepository.findByTournamentIdAndUserId(tournamentId, userId).isPresent()) {
            throw new DomainValidationException("Player is already registered in this tournament");
        }

        String currency = storeRepository.findById(tournament.storeId())
                .map(Store::currency)
                .orElse("EUR");

        String moxfieldUrl;
        String deckName;
        UUID existingDeckId = null;

        if (request.savedDeckId() != null) {
            Deck savedDeck = deckRepository.findById(request.savedDeckId())
                    .orElseThrow(() -> new ResourceNotFoundException("Saved profile deck not found: " + request.savedDeckId()));
            if (!savedDeck.userId().equals(userId)) {
                throw new UnauthorizedStoreActionException("Cannot register with another user's deck");
            }
            moxfieldUrl = savedDeck.moxfieldUrl();
            deckName = savedDeck.name();
            existingDeckId = savedDeck.id();
        } else if (request.newDeck() != null) {
            moxfieldUrl = request.newDeck().moxfieldUrl().trim();
            deckName = request.newDeck().name().trim();
        } else {
            throw new DomainValidationException("Must provide either savedDeckId or newDeck to register");
        }

        MoxfieldDeckResponse moxfieldDeck = moxfieldClient.fetchDeck(moxfieldUrl);
        List<DeckCardInput> resolvedEntries = saveProfileDeckUseCase.syncAndResolveScryfallCards(moxfieldDeck);

        ValidatedDeckResult validated = budgetValidator.validateAndBuildSnapshot(
                moxfieldDeck.moxfieldDeckId(),
                moxfieldUrl,
                deckName,
                tournament.format(),
                tournament.maxDeckBudget(),
                currency,
                resolvedEntries
        );

        UUID finalDeckId = existingDeckId;
        if (finalDeckId == null) {
            UUID candidateId = deckRepository.findByUserIdAndMoxfieldDeckId(userId, moxfieldDeck.moxfieldDeckId())
                    .map(Deck::id)
                    .orElse(UUID.randomUUID());
            Deck savedDeck = deckRepository.save(new Deck(
                    candidateId,
                    userId,
                    deckName,
                    moxfieldUrl,
                    moxfieldDeck.moxfieldDeckId(),
                    tournament.format(),
                    validated.commanderOracleId(),
                    validated.commanderName(),
                    validated.colorIdentity(),
                    validated.totalPrice(),
                    Instant.now(),
                    Instant.now()
            ));
            finalDeckId = savedDeck.id();
        }

        storeRepository.addMember(tournament.storeId(), userId);

        TournamentRegistration registration = registrationRepository.save(new TournamentRegistration(
                UUID.randomUUID(),
                tournamentId,
                userId,
                finalDeckId,
                "REGISTERED",
                validated.totalPrice(),
                validated.commanderName(),
                validated.commanderOracleId(),
                validated.colorIdentity(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                validated.snapshot(),
                Instant.now()
        ));

        return new TournamentRegistrationResponse(
                registration.id(),
                registration.tournamentId(),
                registration.userId(),
                registration.deckId(),
                registration.commanderName(),
                registration.colorIdentity(),
                registration.calculatedDeckPrice(),
                registration.status()
        );
    }
}

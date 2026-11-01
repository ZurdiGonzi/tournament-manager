# Contract: Declaraciones Java 21 Record para DTOs del Backend

**Package Base**: `com.tournament.app.application.dto`

Este contrato formaliza la declaración exacta de cada `record` de Java 21, anotaciones de Jakarta Validation y paquetes correspondientes para su implementación directa en el backend.

---

## 1. Subpaquete `auth`

```java
package com.tournament.app.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.tournament.app.domain.model.UserRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RegisterRequest(
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    String username,

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 255, message = "El correo electrónico no puede superar los 255 caracteres")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    String password,

    UserRole role
) {
    public RegisterRequest {
        if (role == null) {
            role = UserRole.PLAYER;
        }
    }
}

public record LoginRequest(
    @NotBlank(message = "El usuario o email es obligatorio")
    String usernameOrEmail,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    UserSummaryResponse user
) {}

public record UserSummaryResponse(
    UUID id,
    String username,
    String email,
    Set<UserRole> roles,
    Instant createdAt
) {}

public record UserProfileResponse(
    UUID id,
    String username,
    String email,
    Set<UserRole> roles,
    int totalDecks,
    int totalTournamentsPlayed,
    Instant createdAt
) {}
```

---

## 2. Subpaquete `store`

```java
package com.tournament.app.application.dto.store;

import com.tournament.app.application.dto.auth.UserSummaryResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateStoreRequest(
    @NotBlank(message = "El nombre de la tienda es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    String description,

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    String address
) {}

public record StoreSummaryResponse(
    UUID id,
    String name,
    String ownerUsername,
    int totalTournaments
) {}

public record StoreDetailResponse(
    UUID id,
    String name,
    String description,
    String address,
    UserSummaryResponse owner,
    int totalTournaments,
    int totalMembers,
    Instant createdAt
) {}
```

---

## 3. Subpaquete `deck`

```java
package com.tournament.app.application.dto.deck;

import com.tournament.app.domain.model.TournamentFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RegisterDeckRequest(
    @NotBlank(message = "El nombre del mazo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String name,

    @NotBlank(message = "La URL de Moxfield es obligatoria")
    @Pattern(
        regexp = "^https://(www\\.)?moxfield\\.com/decks/[a-zA-Z0-9_-]+$",
        message = "La URL debe ser un enlace válido a un mazo de Moxfield"
    )
    String moxfieldUrl
) {}

public record DeckSummaryResponse(
    UUID id,
    String name,
    String moxfieldUrl,
    String moxfieldId,
    Instant createdAt
) {}

public record DeckCardDto(
    UUID scryfallId,
    UUID oracleId,
    String name,
    @Min(1) int quantity,
    String manaCost,
    BigDecimal manaValue,
    String typeLine,
    List<String> colors,
    List<String> colorIdentity,
    BigDecimal unitPriceEur,
    boolean isCommander
) {}

public record DeckSnapshotDto(
    UUID deckId,
    String deckName,
    String moxfieldUrl,
    TournamentFormat format,
    BigDecimal totalPriceEur,
    Instant capturedAt,
    List<DeckCardDto> cards
) {}

public record DeckValidationFailureResponse(
    String reason,
    BigDecimal maxAllowedBudgetEur,
    BigDecimal evaluatedBudgetEur,
    List<String> violatingCards
) {}
```

---

## 4. Subpaquete `tournament`

```java
package com.tournament.app.application.dto.tournament;

import com.tournament.app.application.dto.store.StoreSummaryResponse;
import com.tournament.app.domain.model.TournamentFormat;
import com.tournament.app.domain.model.TournamentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TournamentRestrictionsDto(
    @NotNull(message = "El formato de juego es obligatorio")
    TournamentFormat format,

    @DecimalMin(value = "0.00", message = "El límite de presupuesto no puede ser negativo")
    BigDecimal maxBudgetEur
) {}

public record CreateTournamentRequest(
    @NotNull(message = "El identificador de la tienda es obligatorio")
    UUID storeId,

    @NotBlank(message = "El nombre del torneo es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres")
    String name,

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Future(message = "La fecha y hora de inicio debe ser futura")
    Instant scheduledAt,

    @NotNull(message = "El precio de entrada es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio de entrada no puede ser negativo")
    BigDecimal entryFeeEur,

    @NotBlank(message = "La descripción del premio es obligatoria")
    @Size(max = 500, message = "La descripción del premio no puede superar los 500 caracteres")
    String prizeDescription,

    @NotNull(message = "El aforo máximo es obligatorio")
    @Min(value = 4, message = "El aforo mínimo de un torneo es de 4 jugadores")
    @Max(value = 256, message = "El aforo máximo no puede superar 256 jugadores")
    Integer maxCapacity,

    @NotNull(message = "Las restricciones del torneo son obligatorias")
    @Valid
    TournamentRestrictionsDto restrictions
) {}

public record TournamentSummaryResponse(
    UUID id,
    UUID storeId,
    String storeName,
    String name,
    TournamentStatus status,
    Instant scheduledAt,
    BigDecimal entryFeeEur,
    String prizeDescription,
    int registeredCount,
    int maxCapacity,
    TournamentRestrictionsDto restrictions
) {}

public record TournamentDetailResponse(
    UUID id,
    StoreSummaryResponse store,
    String name,
    TournamentStatus status,
    Instant scheduledAt,
    BigDecimal entryFeeEur,
    String prizeDescription,
    int maxCapacity,
    int registeredCount,
    TournamentRestrictionsDto restrictions,
    Integer currentRoundNumber,
    int totalRounds,
    Instant createdAt
) {}
```

---

## 5. Subpaquete `registration`

```java
package com.tournament.app.application.dto.registration;

import com.tournament.app.application.dto.deck.DeckSnapshotDto;
import com.tournament.app.domain.model.RegistrationStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record TournamentRegistrationRequest(
    @NotNull(message = "El identificador del torneo es obligatorio")
    UUID tournamentId,

    @NotNull(message = "El identificador del mazo es obligatorio")
    UUID deckId
) {}

public record TournamentRegistrationResponse(
    UUID id,
    UUID tournamentId,
    UUID userId,
    String username,
    RegistrationStatus status,
    Instant registeredAt,
    DeckSnapshotDto deckSnapshot
) {}
```

---

## 6. Subpaquete `round`

```java
package com.tournament.app.application.dto.round;

import com.tournament.app.application.dto.auth.UserSummaryResponse;
import com.tournament.app.domain.model.MatchStatus;
import com.tournament.app.domain.model.RoundStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MatchPairingResponse(
    UUID matchId,
    int tableNumber,
    UserSummaryResponse player1,
    UserSummaryResponse player2,
    boolean isBye,
    MatchStatus status,
    Integer player1Wins,
    Integer player2Wins,
    Integer draws
) {}

public record RoundDetailResponse(
    UUID roundId,
    UUID tournamentId,
    int roundNumber,
    RoundStatus status,
    List<MatchPairingResponse> pairings
) {}

public record ReportMatchResultRequest(
    @NotNull(message = "El identificador de la partida es obligatorio")
    UUID matchId,

    @Min(value = 0, message = "Las victorias no pueden ser negativas")
    @Max(value = 2, message = "El máximo de partidas ganadas es 2")
    int player1Wins,

    @Min(value = 0, message = "Las victorias no pueden ser negativas")
    @Max(value = 2, message = "El máximo de partidas ganadas es 2")
    int player2Wins,

    @Min(value = 0, message = "Los empates no pueden ser negativos")
    @Max(value = 3, message = "El máximo de empates es 3")
    int draws
) {}

public record MatchResultResponse(
    UUID matchId,
    MatchStatus status,
    UUID reportedByUserId,
    int player1Wins,
    int player2Wins,
    int draws,
    boolean resolvedByAdmin
) {}

public record StandingEntryResponse(
    int rank,
    UUID userId,
    String username,
    int matchPoints,
    String matchRecord,
    BigDecimal omwPercentage,
    BigDecimal gwPercentage
) {}

public record TournamentStandingsResponse(
    UUID tournamentId,
    int roundNumber,
    List<StandingEntryResponse> standings
) {}
```

---

## 7. Subpaquetes `league`, `analytics` y `common`

```java
package com.tournament.app.application.dto.league;

import com.tournament.app.application.dto.tournament.TournamentSummaryResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LeagueStandingEntryResponse(
    int rank,
    UUID userId,
    String username,
    int tournamentsPlayed,
    int accumulatedPoints
) {}

public record LeagueDetailResponse(
    UUID id,
    UUID storeId,
    String name,
    Instant startDate,
    Instant endDate,
    List<TournamentSummaryResponse> tournaments,
    List<LeagueStandingEntryResponse> standings
) {}
```

```java
package com.tournament.app.application.dto.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ColorBreakdownDto(
    String colorCode,
    String colorName,
    BigDecimal percentage
) {}

public record CardUsageStatDto(
    UUID scryfallId,
    String cardName,
    String manaCost,
    String typeLine,
    int totalCopies,
    BigDecimal deckInclusionRate,
    BigDecimal averagePriceEur
) {}

public record MetagameAnalyticsResponse(
    String scope,
    UUID targetId,
    int totalDecksAnalyzed,
    BigDecimal averageDeckBudgetEur,
    List<ColorBreakdownDto> colorDistribution,
    List<CardUsageStatDto> topPlayedCards
) {}
```

```java
package com.tournament.app.application.dto.common;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public record FieldErrorDto(
    String field,
    Object rejectedValue,
    String message
) {}

public record ApiErrorResponse(
    URI type,
    String title,
    int status,
    String detail,
    URI instance,
    Instant timestamp,
    List<FieldErrorDto> invalidParams
) {}
```


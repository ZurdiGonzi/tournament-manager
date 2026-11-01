# Contratos REST y DTOs del Modelo de Datos (`001-store-tournament-data-model`)

Todos los endpoints siguen la segregación de seguridad definida en la Constitución (`/api/v1/public/**`, `/api/v1/player/**`, `/api/v1/admin/**`).

---

## 1. Endpoints Públicos (`/api/v1/public/**`) — `permitAll()`

### 1.1. Detalle de Tienda y Lista de Torneos
- **`GET /api/v1/public/stores/{storeId}`**
  - Respuesta `200 OK` (`StoreSummaryDto`):
    ```json
    {
      "id": "uuid",
      "name": "Magic Corner Madrid",
      "ownerUsername": "gonzalo_owner",
      "currency": "EUR",
      "memberCount": 42,
      "upcomingTournaments": [
        {
          "id": "uuid",
          "name": "viernes Commander Budget",
          "scheduledAt": "2026-11-06T18:00:00Z",
          "entryFee": 5.00,
          "prizeDescription": "Vales de tienda top 4 + sobre por participar",
          "restrictions": {
            "format": "COMMANDER",
            "maxDeckBudget": 100.00
          },
          "registeredPlayersCount": 16,
          "status": "REGISTRATION_OPEN"
        }
      ]
    }
    ```

### 1.2. Estadísticas de Cartas y Metajuego (Scryfall Analytics - `AnalyticsController`)
- **`GET /api/v1/public/analytics/stores/{storeId}/cards`** (opcional `?tournamentId={uuid}&format=COMMANDER`)
  - Respuesta `200 OK` (`TournamentAnalyticsDto`):
    ```json
    {
      "storeId": "uuid",
      "totalDecksAnalyzed": 24,
      "averageDeckPrice": 78.40,
      "colorDistribution": {
        "W": 18,
        "U": 25,
        "B": 20,
        "R": 15,
        "G": 22
      },
      "manaCurveAverage": {
        "1": 9.5,
        "2": 14.2,
        "3": 11.0,
        "4": 6.8,
        "5+": 4.5
      },
      "mostPlayedCards": [
        {
          "oracleId": "uuid",
          "name": "Sol Ring",
          "typeLine": "Artifact",
          "rarity": "uncommon",
          "totalCopies": 24,
          "inclusionRatePercent": 100.0,
          "currentPriceEur": 1.50
        }
      ]
    }
    ```

---

## 2. Endpoints de Jugador (`/api/v1/player/**`) — `hasAnyRole('PLAYER', 'ADMIN')`

### 2.1. Añadir Mazo a la Lista de Mazos del Usuario (Enlace a Moxfield)
- **`POST /api/v1/player/decks`**
  - Request (`CreateDeckRequest` record):
    ```json
    {
      "name": "Mi Mazo Commander Budget",
      "moxfieldUrl": "https://www.moxfield.com/decks/AbC123XyZ"
    }
    ```
  - Respuesta `201 Created` (`DeckDto`):
    ```json
    {
      "id": "uuid",
      "name": "Mi Mazo Commander Budget",
      "moxfieldUrl": "https://www.moxfield.com/decks/AbC123XyZ",
      "moxfieldDeckId": "AbC123XyZ",
      "createdAt": "2026-11-01T15:35:00Z"
    }
    ```

### 2.2. Apuntarse a un Torneo con Validación de Restricciones (`RegisterPlayerUseCase`)
- **`POST /api/v1/player/tournaments/{tournamentId}/registrations`**
  - Request (`RegisterTournamentRequest` record):
    ```json
    {
      "deckId": "uuid"
    }
    ```
  - Flujo de validación:
    1. Obtiene las cartas desde Moxfield (`MoxfieldClient`) usando `moxfield_url`.
    2. Sincroniza/consulta metadatos de cada carta en `scryfall_cards` (legalidad en `STANDARD` / `COMMANDER` y precio).
    3. Ejecuta `BudgetValidator` para verificar que el formato sea legal y `calculated_deck_price <= tournament.max_deck_budget`.
    4. Guarda `tournament_registrations` con el `deck_snapshot` en `JSONB` y desglosa en `registration_deck_cards`.
  - Respuesta `201 Created` (o `422 Unprocessable Entity` con `ProblemDetail` RFC 7807 si excede presupuesto o contiene cartas ilegales).

---

## 3. Endpoints de Dueño / Admin (`/api/v1/admin/**`) — `hasRole('ADMIN')`

### 3.1. Crear Torneo en la Tienda (`AdminTournamentController`)
- **`POST /api/v1/admin/stores/{storeId}/tournaments`**
  - Requiere que el usuario autenticado sea el `owner_id` de `storeId`.
  - Request (`CreateTournamentRequest` record):
    ```json
    {
      "name": "Torneo Mensual Standard Budget",
      "scheduledAt": "2026-11-15T17:00:00Z",
      "entryFee": 6.00,
      "prizeDescription": "1 Caja de sobres a repartir en Top 8",
      "format": "STANDARD",
      "maxDeckBudget": 80.00,
      "maxRounds": 4
    }
    ```

### 3.2. Gestionar Usuarios de la Tienda
- **`POST /api/v1/admin/stores/{storeId}/members`** / **`GET /api/v1/admin/stores/{storeId}/members`**
  - Permite al Dueño consultar y dar de alta usuarios en la lista de miembros de su tienda.


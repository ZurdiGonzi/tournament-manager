# REST API Contracts: Lógica de Negocio (Auth, Mazos, Torneos, Ligas, Fórmulas y Estadísticas)

**Feature**: `002-tournament-league-logic`  
**Security Policy**: Stateless JWT (`SessionCreationPolicy.STATELESS`)  
**Error Format**: RFC 7807 `ProblemDetail` (`application/problem+json`)

---

## 1. Endpoints de Autenticación (`/api/v1/auth/**` — `permitAll()`)

### 1.1. Registro de Usuario (`FR-001`)
`POST /api/v1/auth/register`

**Request (`RegisterUserRequest` record)**:
```json
{
  "username": "gonzalo_mtg",
  "password": "StrongPassword123!",
  "role": "PLAYER"
}
```
> Nota: El rol puede ser `"PLAYER"` (jugador) o `"ADMIN"` (dueño de tienda).

**Response (`201 Created` - `AuthTokenResponse` record)**:
```json
{
  "userId": "a1b2c3d4-0000-4000-8000-000000000001",
  "username": "gonzalo_mtg",
  "role": "PLAYER",
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresInSeconds": 3600
}
```

### 1.2. Inicio de Sesión / Login (`FR-001`)
`POST /api/v1/auth/login`

**Request (`LoginRequest` record)**:
```json
{
  "username": "gonzalo_mtg",
  "password": "StrongPassword123!"
}
```

**Response (`200 OK` - `AuthTokenResponse` record)**: Mismo contrato que en `/register`.

---

## 2. Endpoints de Jugador (`/api/v1/player/**` — `hasAnyRole('PLAYER', 'ADMIN')`)

### 2.1. Guardar Mazo en el Perfil conectado con Scryfall (`FR-002`, `FR-004`)
`POST /api/v1/player/decks`

Sincroniza las cartas desde Moxfield + Scryfall y guarda el mazo en la lista de mazos del perfil del usuario autenticado.

**Request (`SaveProfileDeckRequest` record)**:
```json
{
  "name": "Magda Dwarves & Treasures",
  "moxfieldUrl": "https://www.moxfield.com/decks/k9LmN2pQrSt",
  "format": "COMMANDER"
}
```

**Response (`201 Created` - `ProfileDeckResponse` record)**:
```json
{
  "deckId": "d0000001-0000-4000-8000-000000000001",
  "name": "Magda Dwarves & Treasures",
  "moxfieldUrl": "https://www.moxfield.com/decks/k9LmN2pQrSt",
  "format": "COMMANDER",
  "commanderName": "Magda, Brazen Outlaw",
  "commanderOracleId": "7f9e1234-5678-4abc-def0-1234567890ab",
  "colorIdentity": ["R"],
  "lastCalculatedPrice": 78.40,
  "currency": "EUR",
  "cardCount": 100
}
```

### 2.2. Listar Mazos Guardados en el Perfil (`FR-002`)
`GET /api/v1/player/decks` -> Devuelve `List<ProfileDeckResponse>`.

### 2.3. Apuntarse a un Torneo / Jornada con Mazo Guardado o Mazo Nuevo (`FR-003`, `FR-004`)
`POST /api/v1/player/tournaments/{tournamentId}/register`

Permite elegir un mazo ya guardado en el perfil (`deckId`) **o bien** añadir un mazo nuevo en el momento (`newDeck` con `saveToProfile: true/false`). Valida formato y presupuesto contra Scryfall y crea el `deck_snapshot` (`JSONB`).

**Request (`TournamentRegistrationRequest` record)**:
```json
{
  "savedDeckId": null,
  "newDeck": {
    "name": "Kefka Grixis Control",
    "moxfieldUrl": "https://www.moxfield.com/decks/xYz987AbCdE",
    "saveToProfile": true
  }
}
```
*(Si el jugador usa un mazo guardado de su perfil, envía `"savedDeckId": "d0000001-..."` y `"newDeck": null`).*

**Response (`201 Created` - `TournamentRegistrationResponse` record)**:
```json
{
  "registrationId": "f1000001-0000-4000-8000-000000000001",
  "tournamentId": "c2000001-0000-4000-8000-000000000001",
  "userId": "a1b2c3d4-0000-4000-8000-000000000001",
  "deckId": "d0000002-0000-4000-8000-000000000002",
  "commanderName": "Kefka, Court Mage",
  "colorIdentity": ["U", "B", "R"],
  "calculatedDeckPrice": 84.10,
  "status": "REGISTERED"
}
```

### 2.4. Estadísticas de *"Tu Mazo"* conectadas con Scryfall (`FR-012`)
`GET /api/v1/player/decks/{deckId}/analytics?tournamentId={optional}&leagueId={optional}`

Muestra al jugador las estadísticas específicas de su mazo tanto en una jornada concreta como en el acumulado de liga/histórico.

**Response (`200 OK` - `PersonalDeckAnalyticsResponse` record)**:
```json
{
  "deckId": "d0000002-0000-4000-8000-000000000002",
  "deckName": "Kefka Grixis Control",
  "commanderName": "Kefka, Court Mage",
  "colorIdentity": ["U", "B", "R"],
  "scope": "LEAGUE_ACCUMULATED",
  "matchesPlayed": 12,
  "matchesWon": 7,
  "winRatePercentage": 58.33,
  "averageMatchPoints": 3.25,
  "averageMatchdayLeaguePoints": 11.50,
  "rivalMatchups": [
    {
      "rivalCommanderName": "Magda, Brazen Outlaw",
      "matchesPlayed": 5,
      "winsAgainstRival": 3,
      "winRatePercentage": 60.0,
      "summary": "Kefka vs Magda: gana Kefka 60%"
    }
  ],
  "scryfallComposition": {
    "averageCmc": 2.45,
    "matchdayAverageCmcComparison": -0.35,
    "colorBreakdown": { "U": 38, "B": 34, "R": 28 },
    "typeBreakdown": { "Instant": 22, "Sorcery": 12, "Creature": 18, "Artifact": 12, "Land": 36 },
    "totalPrice": 84.10
  }
}
```

---

## 3. Endpoints de Dueño de Tienda / Organizador (`/api/v1/admin/**` — `hasRole('ADMIN')`)

### 3.1. Crear o Modificar Fórmulas Matemáticas Guardadas en Base de Datos (`FR-007`, `FR-008`, `FR-009`)
- `POST /api/v1/admin/stores/{storeId}/formulas`
- `PUT /api/v1/admin/stores/{storeId}/formulas/{formulaId}`

Permite al dueño de la tienda guardar y editar en la base de datos las funciones matemáticas para puntos de partida (`MATCH_POINTS`), puntos de jornada de liga (`LEAGUE_MATCHDAY_POINTS`) y cálculo de *pairings* (`PAIRING_WEIGHT`).

**Request (`UpsertScoringFormulaRequest` record)**:
```json
{
  "name": "Puntos Partida Commander Tienda",
  "formulaType": "MATCH_POINTS",
  "expression": "(wins * 3) + (draws * 1) + (kills * 0.5)",
  "description": "3 puntos por victoria, 1 por empate y 0.5 por eliminación en mesa"
}
```

### 3.2. Registrar una Liga (`FR-006`)
`POST /api/v1/admin/stores/{storeId}/leagues`

**Request (`CreateLeagueRequest` record)**:
```json
{
  "name": "Liga Commander Invierno 2026",
  "format": "COMMANDER",
  "maxDeckBudget": 100.00,
  "matchPointsFormulaId": "11111111-0000-4000-8000-000000000001",
  "matchdayPointsFormulaId": "11111111-0000-4000-8000-000000000002",
  "pairingFormulaId": "11111111-0000-4000-8000-000000000003"
}
```

### 3.3. Registrar un Torneo o Jornada de Liga indicando Número de Partidas (`FR-005`)
`POST /api/v1/admin/stores/{storeId}/tournaments`

**Request (`CreateTournamentRequest` record)**:
```json
{
  "name": "Jornada 1 - Liga Commander Invierno",
  "leagueId": "22222222-0000-4000-8000-000000000001",
  "matchdayNumber": 1,
  "scheduledAt": "2026-11-15T17:00:00Z",
  "entryFee": 5.00,
  "prizeDescription": "Sobres + Puntos de Liga",
  "format": "COMMANDER",
  "maxDeckBudget": 100.00,
  "maxRounds": 4,
  "matchPointsFormulaId": "11111111-0000-4000-8000-000000000001",
  "pairingFormulaId": "11111111-0000-4000-8000-000000000003"
}
```
> Nota: `"maxRounds": 4` establece cuántas partidas (rondas) hay en el torneo (`FR-005`).

### 3.4. Generar Emparejamientos (*Pairings*) de Ronda con la Función Matemática del Dueño (`FR-009`)
`POST /api/v1/admin/tournaments/{tournamentId}/rounds/generate`

Evalúa la fórmula `pairing_formula_id` para cada jugador inscrito y genera las mesas de la siguiente ronda (hasta alcanzar `maxRounds`).

### 3.5. Registrar Resultados de una Partida y Recalcular Puntos de Torneo y Jornada (`FR-007`, `FR-008`, `FR-010`)
`POST /api/v1/admin/matches/{matchId}/result`

Exclusivo para el organizador/dueño de la tienda (`Clarifications Session 2026-11-01`). Evalúa la fórmula de puntos por partida y actualiza automáticamente los puntos de la jornada y el acumulado en `league_standings`.

**Request (`RecordMatchResultRequest` record)**:
```json
{
  "participantResults": [
    {
      "userId": "a1b2c3d4-0000-4000-8000-000000000001",
      "placement": 1,
      "wins": 1,
      "losses": 0,
      "draws": 0,
      "kills": 2
    },
    {
      "userId": "a1b2c3d4-0000-4000-8000-000000000002",
      "placement": 2,
      "wins": 0,
      "losses": 1,
      "draws": 0,
      "kills": 1
    }
  ]
}
```

---

## 4. Endpoints Públicos de Estadísticas de Jornada, Liga y Enfrentamientos (`/api/v1/public/**` — `permitAll()`)

### 4.1. Estadísticas de Jornada o Liga (`FR-011`)
- `GET /api/v1/public/analytics/tournaments/{tournamentId}` (Estadísticas de una jornada concreta)
- `GET /api/v1/public/analytics/leagues/{leagueId}` (Estadísticas acumuladas de toda la liga)

**Response (`200 OK` - `MatchdayDeckAnalyticsResponse` record)**:
```json
{
  "scopeId": "c2000001-0000-4000-8000-000000000001",
  "scopeType": "MATCHDAY",
  "totalDecks": 16,
  "mostPlayedCards": [
    {
      "oracleId": "00010d56-2f5a-4698-9c53-0b7a5a7f82d2",
      "cardName": "Sol Ring",
      "typeLine": "Artifact",
      "decksCount": 15,
      "presencePercentage": 93.75,
      "totalCopies": 15
    }
  ],
  "mostPlayedColors": {
    "W": 25.0,
    "U": 56.25,
    "B": 50.0,
    "R": 68.75,
    "G": 31.25
  },
  "winningestCommanders": [
    {
      "commanderOracleId": "9a8b7c6d-5678-4abc-def0-1234567890ab",
      "commanderName": "Kefka, Court Mage",
      "colorIdentity": ["U", "B", "R"],
      "matchesPlayed": 8,
      "matchesWon": 5,
      "winRatePercentage": 62.5,
      "averagePointsEarned": 3.50
    }
  ],
  "commanderMatchups": [
    {
      "commanderA": "Magda, Brazen Outlaw",
      "commanderB": "Kefka, Court Mage",
      "matchesTogether": 5,
      "commanderAWins": 2,
      "commanderBWins": 3,
      "dominantCommander": "Kefka, Court Mage",
      "dominantWinPercentage": 60.0,
      "summaryLabel": "Magda vs Kefka: gana Kefka 60%"
    }
  ]
}
```

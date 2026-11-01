# Data Model: Contratos y DTOs del Backend

**Feature**: `002-backend-dtos` | **Fecha**: 2026-11-01

Este documento define la estructura detallada de todos los Data Transfer Objects (DTOs), tipos de datos, validaciones y modelos conceptuales para la capa de aplicación (`com.tournament.app.application.dto`).

---

## 1. Tipos Base y Enums

### 1.1 `UserRole`
Representa el rol y nivel de privilegio de un usuario.
- Valores:
  - `PLAYER`: Jugador estándar (puede inscribirse a torneos, gestionar sus mazos y reportar resultados).
  - `ADMIN`: Administrador / Propietario de tienda (puede crear torneos, configurar tiendas y resolver disputas).

### 1.2 `TournamentFormat`
Formato de Magic: The Gathering bajo el que se rige el torneo.
- Valores:
  - `STANDARD`: Formato rotativo de 60 cartas mínimo y máximo 4 copias por carta.
  - `COMMANDER`: Formato singleton de 100 cartas con 1 comandante legendario.
  - `MODERN`: Formato no rotativo desde Mirrodin/8ª edición.
  - `PAUPER`: Formato accesible compuesto exclusivamente por cartas comunes.

### 1.3 `TournamentStatus`
Estado del ciclo de vida del torneo.
- Transiciones:
  - `DRAFT` ➔ `REGISTRATION_OPEN` ➔ `IN_PROGRESS` ➔ `COMPLETED`
  - Cualquier estado previo a `COMPLETED` ➔ `CANCELLED`

### 1.4 `RegistrationStatus`
Estado de la inscripción de un jugador en un torneo.
- Valores:
  - `CONFIRMED`: Inscripción activa con mazo validado y snapshot capturado.
  - `CANCELLED`: Inscripción cancelada por el jugador antes de iniciar el torneo.
  - `DROPPED`: Jugador que abandona voluntariamente el torneo en curso.

### 1.5 `RoundStatus`
Estado de una ronda de torneo.
- Valores:
  - `ACTIVE`: Ronda en juego con emparejamientos activos.
  - `FINISHED`: Ronda finalizada con todas las partidas cerradas o resueltas.

### 1.6 `MatchStatus`
Estado de una partida individual en una ronda.
- Valores:
  - `PENDING`: Partida pendiente de disputa o reporte.
  - `SUBMITTED`: Resultado enviado por un jugador, pendiente de confirmación.
  - `CONFIRMED`: Resultado acordado por ambos jugadores o validado por el sistema.
  - `DISPUTED`: Resultados contradictorios entre jugadores; requiere intervención del admin.

---

## 2. Definición Estructurada de DTOs por Paquete

### 2.1 Paquete `auth` (`com.tournament.app.application.dto.auth`)

#### `RegisterRequest`
Payload para registro de un nuevo usuario.
- `username` (`String`): Obligatorio, `@NotBlank`, `@Size(min = 3, max = 30)`, `@Pattern(regexp = "^[a-zA-Z0-9._-]+$")`.
- `email` (`String`): Obligatorio, `@NotBlank`, `@Email`, `@Size(max = 255)`.
- `password` (`String`): Obligatorio, `@NotBlank`, `@Size(min = 8, max = 100)`.
- `role` (`UserRole`): Opcional (por defecto `PLAYER`).

#### `LoginRequest`
Payload para autenticación.
- `usernameOrEmail` (`String`): Obligatorio, `@NotBlank`.
- `password` (`String`): Obligatorio, `@NotBlank`.

#### `AuthResponse`
Respuesta de autenticación exitosa con token JWT.
- `accessToken` (`String`): Token JWT firmado.
- `tokenType` (`String`): `"Bearer"`.
- `expiresInSeconds` (`long`): Duración en segundos (ej. 86400).
- `user` (`UserSummaryResponse`): Resumen del usuario autenticado.

#### `UserSummaryResponse`
Resumen público/seguro de un usuario.
- `id` (`UUID`): Identificador único.
- `username` (`String`): Nombre de usuario.
- `email` (`String`): Correo electrónico.
- `roles` (`Set<UserRole>`): Conjunto de roles del usuario.
- `createdAt` (`Instant`): Marca de tiempo de registro.

#### `UserProfileResponse`
Detalle completo de perfil del jugador / administrador.
- `id` (`UUID`): Identificador único.
- `username` (`String`): Nombre de usuario.
- `email` (`String`): Correo electrónico.
- `roles` (`Set<UserRole>`): Roles del usuario.
- `totalDecks` (`int`): Cantidad de mazos registrados.
- `totalTournamentsPlayed` (`int`): Cantidad de torneos disputados.
- `createdAt` (`Instant`): Fecha de registro.

---

### 2.2 Paquete `store` (`com.tournament.app.application.dto.store`)

#### `CreateStoreRequest`
Solicitud de alta de tienda física o comunidad.
- `name` (`String`): Obligatorio, `@NotBlank`, `@Size(min = 2, max = 100)`.
- `description` (`String`): Opcional, `@Size(max = 500)`.
- `address` (`String`): Opcional, `@Size(max = 255)`.

#### `StoreSummaryResponse`
Resumen de tienda para listados.
- `id` (`UUID`): Identificador único.
- `name` (`String`): Nombre comercial.
- `ownerUsername` (`String`): Nombre del dueño.
- `totalTournaments` (`int`): Total de torneos organizados.

#### `StoreDetailResponse`
Detalle completo de tienda.
- `id` (`UUID`): Identificador único.
- `name` (`String`): Nombre comercial.
- `description` (`String`): Descripción.
- `address` (`String`): Dirección física.
- `owner` (`UserSummaryResponse`): Propietario de la tienda.
- `totalTournaments` (`int`): Conteo de torneos.
- `totalMembers` (`int`): Conteo de usuarios vinculados.
- `createdAt` (`Instant`): Fecha de alta.

---

### 2.3 Paquete `deck` (`com.tournament.app.application.dto.deck`)

#### `RegisterDeckRequest`
Solicitud para asociar un mazo de Moxfield al perfil.
- `name` (`String`): Obligatorio, `@NotBlank`, `@Size(min = 2, max = 100)`.
- `moxfieldUrl` (`String`): Obligatorio, `@NotBlank`, `@Pattern(regexp = "^https://(www\\.)?moxfield\\.com/decks/[a-zA-Z0-9_-]+$")`.

#### `DeckSummaryResponse`
Resumen de mazo en la biblioteca del usuario.
- `id` (`UUID`): Identificador interno del mazo.
- `name` (`String`): Nombre asignado por el usuario.
- `moxfieldUrl` (`String`): Enlace directo a Moxfield.
- `moxfieldId` (`String`): Identificador extraído de Moxfield.
- `createdAt` (`Instant`): Fecha de vinculación.

#### `DeckCardDto`
Representación de una carta individual dentro de un snapshot de mazo enriquecida con Scryfall.
- `scryfallId` (`UUID`): Identificador único de carta en Scryfall.
- `oracleId` (`UUID`): Identificador Oracle para agrupar impresiones.
- `name` (`String`): Nombre en inglés oficial de la carta.
- `quantity` (`int`): Cantidad de copias en el mazo (`@Min(1)`).
- `manaCost` (`String`): Coste de maná simbólico (ej. `"{1}{U}{B}"`).
- `manaValue` (`BigDecimal`): Coste de maná convertido (CMC).
- `typeLine` (`String`): Línea de tipo (ej. `"Creature — Human Wizard"`).
- `colors` (`List<String>`): Colores de la carta (`["U", "B"]`).
- `colorIdentity` (`List<String>`): Identidad de color para Commander.
- `unitPriceEur` (`BigDecimal`): Precio unitario capturado en euros.
- `isCommander` (`boolean`): Indica si es el comandante del mazo.

#### `DeckSnapshotDto`
Snapshot inmutable del mazo al momento de la inscripción.
- `deckId` (`UUID`): Identificador del mazo origen.
- `deckName` (`String`): Nombre del mazo.
- `moxfieldUrl` (`String`): URL de procedencia.
- `format` (`TournamentFormat`): Formato evaluado.
- `totalPriceEur` (`BigDecimal`): Suma acumulada del precio de las cartas.
- `capturedAt` (`Instant`): Marca de tiempo exacta de captura.
- `cards` (`List<DeckCardDto>`): Lista completa de cartas y cantidades.

#### `DeckValidationFailureResponse`
Respuesta detallada en caso de rechazo de un mazo para un torneo.
- `reason` (`String`): Causa principal (`BUDGET_EXCEEDED`, `ILLEGAL_CARDS`, `INVALID_FORMAT`, `UNREACHABLE_MOXFIELD`).
- `maxAllowedBudgetEur` (`BigDecimal`): Límite presupuestario configurado (nulo si no aplica).
- `evaluatedBudgetEur` (`BigDecimal`): Coste calculado del mazo presentado.
- `violatingCards` (`List<String>`): Nombres de las cartas no permitidas o causantes del exceso.

---

### 2.4 Paquete `tournament` (`com.tournament.app.application.dto.tournament`)

#### `TournamentRestrictionsDto`
Reglas competitivas aplicables al torneo.
- `format` (`TournamentFormat`): Formato obligatorio (`@NotNull`).
- `maxBudgetEur` (`BigDecimal`): Tope de presupuesto permitido (`@DecimalMin("0.00")`, opcional).

#### `CreateTournamentRequest`
Solicitud de creación de nuevo evento.
- `storeId` (`UUID`): Obligatorio, `@NotNull`.
- `name` (`String`): Obligatorio, `@NotBlank`, `@Size(min = 3, max = 120)`.
- `scheduledAt` (`Instant`): Obligatorio, `@NotNull`, `@Future`.
- `entryFeeEur` (`BigDecimal`): Obligatorio, `@NotNull`, `@DecimalMin("0.00")`.
- `prizeDescription` (`String`): Obligatorio, `@NotBlank`, `@Size(max = 500)`.
- `maxCapacity` (`Integer`): Obligatorio, `@NotNull`, `@Min(4)`, `@Max(256)`.
- `restrictions` (`TournamentRestrictionsDto`): Obligatorio, `@NotNull`, `@Valid`.

#### `TournamentSummaryResponse`
Resumen de torneo para carteleras públicas y calendarios.
- `id` (`UUID`): Identificador del torneo.
- `storeId` (`UUID`): Identificador de la tienda.
- `storeName` (`String`): Nombre de la tienda.
- `name` (`String`): Título del evento.
- `status` (`TournamentStatus`): Estado actual.
- `scheduledAt` (`Instant`): Fecha y hora prevista.
- `entryFeeEur` (`BigDecimal`): Coste de inscripción.
- `prizeDescription` (`String`): Dotación o premio.
- `registeredCount` (`int`): Plazas ocupadas.
- `maxCapacity` (`int`): Aforo máximo.
- `restrictions` (`TournamentRestrictionsDto`): Restricciones activas.

#### `TournamentDetailResponse`
Detalle completo de torneo, incluyendo participantes y rondas.
- `id` (`UUID`): Identificador del torneo.
- `store` (`StoreSummaryResponse`): Tienda organizadora.
- `name` (`String`): Nombre del torneo.
- `status` (`TournamentStatus`): Estado actual.
- `scheduledAt` (`Instant`): Fecha y hora.
- `entryFeeEur` (`BigDecimal`): Precio de entrada.
- `prizeDescription` (`String`): Premio.
- `maxCapacity` (`int`): Aforo máximo.
- `registeredCount` (`int`): Inscritos actuales.
- `restrictions` (`TournamentRestrictionsDto`): Restricciones.
- `currentRoundNumber` (`Integer`): Ronda actual (o nulo si no ha iniciado).
- `totalRounds` (`int`): Rondas totales planificadas.
- `createdAt` (`Instant`): Fecha de creación.

---

### 2.5 Paquete `registration` (`com.tournament.app.application.dto.registration`)

#### `TournamentRegistrationRequest`
Solicitud de inscripción a un torneo.
- `tournamentId` (`UUID`): Obligatorio, `@NotNull`.
- `deckId` (`UUID`): Obligatorio, `@NotNull`.

#### `TournamentRegistrationResponse`
Respuesta de inscripción confirmada.
- `id` (`UUID`): Identificador de la inscripción.
- `tournamentId` (`UUID`): Torneo inscrito.
- `userId` (`UUID`): Jugador inscrito.
- `username` (`String`): Nombre de usuario.
- `status` (`RegistrationStatus`): Estado de la plaza.
- `registeredAt` (`Instant`): Fecha de inscripción.
- `deckSnapshot` (`DeckSnapshotDto`): Copia inmutable congelada del mazo.

---

### 2.6 Paquete `round` (`com.tournament.app.application.dto.round`)

#### `MatchPairingResponse`
Emparejamiento de una mesa de juego.
- `matchId` (`UUID`): Identificador de la partida.
- `tableNumber` (`int`): Número de mesa asignada.
- `player1` (`UserSummaryResponse`): Primer jugador.
- `player2` (`UserSummaryResponse`): Segundo jugador (nulo si es BYE).
- `isBye` (`boolean`): Verdadero si el jugador 1 recibe descanso/BYE.
- `status` (`MatchStatus`): Estado actual de la partida.
- `player1Wins` (`Integer`): Partidas ganadas por jugador 1.
- `player2Wins` (`Integer`): Partidas ganadas por jugador 2.
- `draws` (`Integer`): Empates.

#### `RoundDetailResponse`
Consulta de una ronda activa o finalizada.
- `roundId` (`UUID`): Identificador de la ronda.
- `tournamentId` (`UUID`): Torneo al que pertenece.
- `roundNumber` (`int`): Número de ronda (1, 2, 3...).
- `status` (`RoundStatus`): Estado de la ronda.
- `pairings` (`List<MatchPairingResponse>`): Colección de partidas de la ronda.

#### `ReportMatchResultRequest`
Reporte de resultado emitido por un jugador o árbitro.
- `matchId` (`UUID`): Obligatorio, `@NotNull`.
- `player1Wins` (`int`): `@Min(0)`, `@Max(2)`.
- `player2Wins` (`int`): `@Min(0)`, `@Max(2)`.
- `draws` (`int`): `@Min(0)`, `@Max(3)`.

#### `MatchResultResponse`
Confirmación del estado tras el reporte.
- `matchId` (`UUID`): Identificador de la partida.
- `status` (`MatchStatus`): Nuevo estado (`SUBMITTED`, `CONFIRMED`, `DISPUTED`).
- `reportedByUserId` (`UUID`): Usuario que envió el reporte.
- `player1Wins` (`int`): Victorias de jugador 1.
- `player2Wins` (`int`): Victorias de jugador 2.
- `draws` (`int`): Empates.
- `resolvedByAdmin` (`boolean`): Si fue arbitrado manualmente.

#### `StandingEntryResponse`
Fila individual en la clasificación del torneo.
- `rank` (`int`): Posición en la tabla (1º, 2º...).
- `userId` (`UUID`): Jugador.
- `username` (`String`): Nombre del jugador.
- `matchPoints` (`int`): Puntos de torneo (3 por victoria, 1 por empate).
- `matchRecord` (`String`): Resumen (ej. `"3-1-0"`).
- `omwPercentage` (`BigDecimal`): Desempate Opponent Match Win % (ej. `62.50`).
- `gwPercentage` (`BigDecimal`): Desempate Game Win % (ej. `66.67`).

#### `TournamentStandingsResponse`
Tabla completa de clasificación.
- `tournamentId` (`UUID`): Torneo clasificado.
- `roundNumber` (`int`): Ronda hasta la que se computan los puntos.
- `standings` (`List<StandingEntryResponse>`): Lista ordenada de posiciones.

---

### 2.7 Paquete `league` (`com.tournament.app.application.dto.league`)

#### `LeagueStandingEntryResponse`
Posición de un jugador en la liga multijornada.
- `rank` (`int`): Posición general.
- `userId` (`UUID`): Identificador de jugador.
- `username` (`String`): Nombre.
- `tournamentsPlayed` (`int`): Torneos disputados dentro de la liga.
- `accumulatedPoints` (`int`): Puntos acumulados en la liga.

#### `LeagueDetailResponse`
Detalle de una liga de tienda.
- `id` (`UUID`): Identificador de la liga.
- `storeId` (`UUID`): Tienda organizadora.
- `name` (`String`): Nombre de la liga (ej. "Liga Commander Otoño 2026").
- `startDate` (`Instant`): Fecha de inicio.
- `endDate` (`Instant`): Fecha de clausura.
- `tournaments` (`List<TournamentSummaryResponse>`): Torneos puntuables vinculados.
- `standings` (`List<LeagueStandingEntryResponse>`): Clasificación acumulada.

---

### 2.8 Paquete `analytics` (`com.tournament.app.application.dto.analytics`)

#### `ColorBreakdownDto`
Distribución de presencia de colores de Magic.
- `colorCode` (`String`): `"W"`, `"U"`, `"B"`, `"R"`, `"G"`, `"C"`.
- `colorName` (`String`): `"White"`, `"Blue"`, `"Black"`, `"Red"`, `"Green"`, `"Colorless"`.
- `percentage` (`BigDecimal`): Porcentaje de presencia (0.00 - 100.00).

#### `CardUsageStatDto`
Estadística de uso de carta con datos Scryfall.
- `scryfallId` (`UUID`): Identificador Scryfall.
- `cardName` (`String`): Nombre de la carta.
- `manaCost` (`String`): Coste de maná.
- `typeLine` (`String`): Línea de tipo.
- `totalCopies` (`int`): Copias totales jugadas en el evento/tienda.
- `deckInclusionRate` (`BigDecimal`): Porcentaje de mazos que la incluyen (0.00 - 100.00).
- `averagePriceEur` (`BigDecimal`): Precio medio unitario.

#### `MetagameAnalyticsResponse`
Resumen analítico agregado.
- `scope` (`String`): `"TOURNAMENT"` o `"STORE"`.
- `targetId` (`UUID`): Identificador del torneo o tienda analizada.
- `totalDecksAnalyzed` (`int`): Muestra de mazos evaluada.
- `averageDeckBudgetEur` (`BigDecimal`): Coste medio de los mazos.
- `colorDistribution` (`List<ColorBreakdownDto>`): Reparto de colores.
- `topPlayedCards` (`List<CardUsageStatDto>`): Ranking de cartas más frecuentes.

---

### 2.9 Paquete `common` (`com.tournament.app.application.dto.common`)

#### `FieldErrorDto`
Detalle de fallo en un campo específico de una solicitud.
- `field` (`String`): Nombre de la propiedad (ej. `"restrictions.maxBudgetEur"`).
- `rejectedValue` (`Object`): Valor inválido suministrado.
- `message` (`String`): Motivo legible del fallo de validación.

#### `ApiErrorResponse`
Estructura RFC 7807 uniforme de error.
- `type` (`URI`): Tipo URI del error.
- `title` (`String`): Título corto del error.
- `status` (`int`): Código de estado HTTP.
- `detail` (`String`): Explicación detallada.
- `instance` (`URI`): Ruta del recurso solicitada.
- `timestamp` (`Instant`): Marca de tiempo UTC del evento.
- `invalidParams` (`List<FieldErrorDto>`): Lista de errores de campo (opcional).


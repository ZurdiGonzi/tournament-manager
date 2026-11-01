# Phase 0 Research: Lógica de Negocio de Usuarios, Mazos (Scryfall), Torneos, Ligas, Fórmulas en BD y Estadísticas

**Feature**: `002-tournament-league-logic`  
**Date**: 2026-11-01

---

## 1. Almacenamiento en Base de Datos y Evaluación Pura de Fórmulas Matemáticas del Dueño

- **Decision**: Persistir las fórmulas matemáticas como registros editables en PostgreSQL dentro de la tabla `scoring_formulas` (vinculadas a `stores`, `leagues` y `tournaments` según su tipo: `MATCH_POINTS`, `LEAGUE_MATCHDAY_POINTS`, `PAIRING_WEIGHT`) y evaluarlas en el núcleo de dominio (`domain/service/FormulaEvaluationEngine.java`) mediante un evaluador aritmético recursivo puro en Java 21 sin dependencias externas.
- **Rationale**:
  - Cumple la directriz explícita de guardar las fórmulas matemáticas en la base de datos para que el dueño de la tienda pueda consultarlas y modificarlas libremente sin redesplegar el backend.
  - Respeta el **Principio I de la Constitución** (`domain/` 100% libre de frameworks externos): un parser descendente recursivo (`Recursive Descent Parser`) en Java 21 puro soporta suma, resta, multiplicación, división segura (protección frente a división por cero devolviendo `0.0`), paréntesis, funciones `min`/`max` y variables inyectadas en un `Map<String, Double>` desde el resultado de la partida o clasificación, en menos de 150 líneas de código determinista y 100% testeable en memoria.
- **Alternatives considered**:
  - *Evaluación mediante motor JavaScript (`GraalVM` / `ScriptEngine`)*: Descartado por riesgo de seguridad (inyección de código arbitrario) y sobrecarga de memoria en capa gratuita.
  - *Fórmulas fijas codificadas en clases Java*: Descartado porque impide que el dueño de la tienda modifique sus propias funciones matemáticas en la base de datos.

---

## 2. Flujo Unificado de Mazos (Perfil + Inscripción al Torneo con Mazo Guardado o Nuevo + Scryfall)

- **Decision**: Soportar en `RegisterPlayerUseCase` una entrada polimórfica o dual (`deckId` opcional para mazo ya guardado en el perfil del jugador, o `moxfieldUrl` + `saveToProfile` booleano para un mazo nuevo introducido en el acto). En ambos flujos, `ScryfallDeckEnricher` sincroniza las cartas faltantes o desactualizadas contra Scryfall (`POST https://api.scryfall.com/cards/collection`), valida el formato (`STANDARD` o `COMMANDER`) y el presupuesto (`BudgetValidator`), y persiste tanto el `deck_snapshot` (`JSONB`) como las filas en `registration_deck_cards`.
- **Rationale**:
  - Permite al jugador guardar mazos en su perfil (`POST /api/v1/player/decks`) y reutilizarlos en un clic al apuntarse a cualquier jornada, o bien pegar un enlace nuevo en el momento de apuntarse decidiendo si desea conservarlo en su perfil.
  - Garantiza que el 100% de los mazos de perfil y de torneo estén enriquecidos con los metadatos de Scryfall (comandante, identidad de color, colores, CMC, línea de tipo, legalidades y precios) cumpliendo `FR-002`, `FR-003` y `FR-004`.
- **Alternatives considered**:
  - *Obligar al usuario a ir primero a su perfil antes de inscribirse*: Descartado porque incumple el requisito de poder acceder a un torneo añadiendo directamente un mazo nuevo o uno guardado.

---

## 3. Modelado de Ligas, Jornadas y Emparejamientos (1v1 y Mesas Multijugador Commander)

- **Decision**:
  - Modelar una **Liga (`leagues`)** como una competición de una tienda que agrupa **Jornadas** (`tournaments` con `league_id` y `matchday_number`) y mantiene una tabla `league_standings` actualizada progresivamente cada vez que el organizador introduce resultados de partidas (`FR-006`, `FR-010`).
  - Desacoplar los participantes de cada partida en una tabla `match_participants` (además de mantener compatibilidad 1v1 en `matches`) para soportar tanto duelos 1v1 (*Standard*, 2 jugadores por mesa) como mesas multijugador (*Commander*, pods de 4 jugadores o 3 en caso de resto impar), almacenando por cada jugador su comandante, resultado, posición en mesa y los puntos calculados con la fórmula de partida del dueño.
- **Rationale**:
  - El usuario especificó enfrentamientos de comandantes (*"Magda vs Kefka gana Kefka 60%"*) y ligas por jornadas. En *Commander*, las partidas pueden ser de 2 a 4 jugadores y cada participante compite con su comandante registrado en el *snapshot* de la jornada.
  - Al restringir el reporte de resultados exclusivamente al organizador/dueño de la tienda (`Clarifications Session 2026-11-01`), las partidas pasan directamente de `IN_PROGRESS` a `CONFIRMED` cuando el dueño registra el resultado de la mesa, disparando en el acto el recálculo de puntos de partida, de jornada y de liga.
- **Alternatives considered**:
  - *Limitar `matches` únicamente a `player1_id` y `player2_id`*: Insuficiente para mesas de 4 jugadores de Commander y para cruzar enfrentamientos entre múltiples comandantes en una misma partida.

---

## 4. Cálculo de Estadísticas de Jornada, Matriz de Enfrentamientos ("Magda vs Kefka") y "Tu Mazo"

- **Decision**:
  - Calcular las estadísticas combinando las tablas relacionales (`registration_deck_cards`, `scryfall_cards`, `match_participants`) y los *snapshots* `JSONB`, ofreciendo filtros por `tournamentId` (jornada individual) y por `leagueId` / histórico (`FR-011`, `FR-012`):
    1. **Cartas más jugadas**: Agregación sobre `registration_deck_cards` + `scryfall_cards` agrupada por `oracle_id`.
    2. **Colores e identidades de color más jugados**: Agregación de `color_identity` del mazo/comandante y distribución de `colors` de las cartas vía Scryfall.
    3. **Comandantes más ganadores**: Agrupación de `match_participants` por el comandante (`commander_oracle_id` / `commander_name` del mazo inscrito), calculando partidas jugadas, victorias, porcentaje de victoria (`win_rate`) y puntos medios.
    4. **Emparejamientos de rivales (*Matchups*, ej. "Magda vs Kefka")**: Cruce par-a-par (`self-join` sobre `match_participants` para una misma `match_id` con `p1.user_id != p2.user_id`) contabilizando las partidas en que el Comandante A y el Comandante B coincidieron en la misma mesa y cuántas veces el Comandante A quedó por encima / ganó frente al Comandante B.
    5. **Estadísticas de *"Tu Mazo"* (`/api/v1/player/decks/{deckId}/analytics`)**: Métricas filtradas para el mazo del jugador autenticado (win rate, puntos medios por partida y por jornada, balance contra cada comandante rival al que se ha enfrentado y curva de maná/colores/coste de Scryfall comparada con la media de la jornada).
- **Rationale**:
  - Permite responder en `< 200ms` sin invocar la API externa de Scryfall durante la visualización de estadísticas, tanto para una jornada concreta como para el acumulado de la liga o el histórico personal del jugador.
- **Alternatives considered**:
  - *Recalcular pidiendo datos a Moxfield/Scryfall en cada lectura de estadísticas*: Descartado por latencia, límites de *rate-limit* y riesgo de pérdida de inmutabilidad histórica.

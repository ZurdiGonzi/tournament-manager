# Quickstart & Validation Guide: Lógica de Negocio (`002-tournament-league-logic`)

Esta guía describe los pasos verificables de extremo a extremo para validar los casos de uso y contratos definidos en [data-model.md](./data-model.md) y [rest-api-contracts.md](./contracts/rest-api-contracts.md).

---

## 1. Prerrequisitos

- **Java 21** y **Spring Boot 3.x** configurados en el proyecto.
- **PostgreSQL 15+ (Supabase / Neon o instancia local)** con las migraciones Flyway `V1__init_store_tournament_schema.sql` y `V2__league_formulas_and_business_logic_schema.sql` aplicadas.
- Variables de entorno configuradas:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>?sslmode=require&prepareThreshold=0`
  - `JWT_SECRET=<clave-secreta-256-bits>`

---

## 2. Escenarios de Validación Paso a Paso

### Escenario 1: Registro de Usuario, Login y Guardado de Mazo en el Perfil con Scryfall (`FR-001`, `FR-002`, `FR-004`)
1. Invocar `POST /api/v1/auth/register` para crear un usuario jugador (`role = "PLAYER"`) y un usuario dueño de tienda (`role = "ADMIN"`).
2. Invocar `POST /api/v1/auth/login` y capturar el `accessToken` JWT de cada uno.
3. Con el token de jugador, invocar `POST /api/v1/player/decks` pasando un enlace de Moxfield de *Commander* (ej. comandante `"Magda, Brazen Outlaw"`).
4. **Resultado esperado**: El sistema consulta Scryfall, guarda el mazo en el perfil del jugador con `commanderName`, `colorIdentity` y `lastCalculatedPrice`, y devuelve `201 Created`.

### Escenario 2: Configuración de Fórmulas Matemáticas en BD, Liga y Torneo por el Dueño (`FR-005` a `FR-009`)
1. Con el token de Dueño (`ROLE_ADMIN`), invocar `POST /api/v1/admin/stores/{storeId}/formulas` para guardar en base de datos las tres fórmulas matemáticas editables:
   - `MATCH_POINTS` (puntos de cada partida del torneo)
   - `LEAGUE_MATCHDAY_POINTS` (puntos de cada jornada de liga)
   - `PAIRING_WEIGHT` (cálculo de *pairings* en cada jornada)
2. Invocar `POST /api/v1/admin/stores/{storeId}/leagues` vinculando las tres fórmulas.
3. Invocar `POST /api/v1/admin/stores/{storeId}/tournaments` indicando `leagueId`, `matchdayNumber = 1` y `maxRounds = 4` (cantidad de partidas por torneo).
4. **Resultado esperado**: Las fórmulas quedan persistidas en `scoring_formulas` (pudiendo actualizarse mediante `PUT`), la liga queda activa y la Jornada 1 queda configurada a 4 partidas.

### Escenario 3: Inscripción a Torneo con Mazo Guardado o Mazo Nuevo (`FR-003`)
1. Jugador 1 se apunta a la Jornada 1 invocando `POST /api/v1/player/tournaments/{id}/register` con `"savedDeckId"` (su mazo de *Magda* guardado en el perfil).
2. Jugador 2 se apunta a la misma Jornada 1 enviando `"newDeck"` con el enlace de Moxfield de *Kefka* y `"saveToProfile": true`.
3. **Resultado esperado**: Ambos jugadores quedan inscritos con su `deck_snapshot` (`JSONB`) inmutable validado contra Scryfall, y el mazo de *Kefka* queda además guardado en el perfil del Jugador 2.

### Escenario 4: Generación de *Pairings*, Registro de Resultados por el Dueño y Cálculo Progresivo de Puntos (`FR-007` a `FR-010`)
1. El dueño de la tienda invoca `POST /api/v1/admin/tournaments/{id}/rounds/generate`. El motor evalúa la fórmula `PAIRING_WEIGHT` guardada en BD y crea las mesas de la Ronda 1.
2. El dueño de la tienda registra el resultado de la partida invocando `POST /api/v1/admin/matches/{matchId}/result`.
3. **Resultado esperado**:
   - Se evalúa la fórmula `MATCH_POINTS` de la BD y se asignan `match_points_earned` en `match_participants`.
   - Se actualiza `tournament_points` en `tournament_registrations`.
   - Se evalúa la fórmula `LEAGUE_MATCHDAY_POINTS` de la BD y se actualiza el acumulado del jugador en `league_standings`.
   - Si un usuario con `ROLE_PLAYER` intenta llamar a `POST /api/v1/admin/matches/{matchId}/result`, recibe `403 Forbidden`.

### Escenario 5: Consulta de Estadísticas de Jornada ("Magda vs Kefka") y de "Tu Mazo" (`FR-011`, `FR-012`)
1. Invocar `GET /api/v1/public/analytics/tournaments/{tournamentId}` (y `/leagues/{leagueId}`).
2. **Resultado esperado**: Devuelve las cartas más jugadas, colores más jugados, comandantes más ganadores y la lista `commanderMatchups` mostrando `"Magda vs Kefka: gana Kefka 60%"`.
3. Invocar `GET /api/v1/player/decks/{deckId}/analytics` con el token del jugador.
4. **Resultado esperado**: Devuelve las estadísticas de *"Tu Mazo"* (porcentaje de victorias, puntos medios, cruces contra comandantes rivales y desglose de curva/colores/costes de Scryfall).

---

## 3. Ejecución de Tests Automatizados

```powershell
# Ejecutar tests unitarios del núcleo de dominio (FormulaEvaluationEngine, SwissPairingEngine, BudgetValidator)
./mvnw test -Dtest="*DomainTest,*EngineTest,*ValidatorTest"

# Ejecutar suite completa de backend y verificación de seguridad
./mvnw verify
```

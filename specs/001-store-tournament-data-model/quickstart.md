# Quickstart & Validation Guide: Modelo de Datos de Tienda, Torneos, Mazos y Scryfall (`001-store-tournament-data-model`)

Esta guía describe cómo validar de extremo a extremo el modelo de datos relacional y `JSONB` en PostgreSQL una vez ejecutada la fase de implementación (`/speckit-implement`).

## 1. Prerrequisitos

- **Java 21** instalado (`java -version`).
- **PostgreSQL 15+** accesible (instancia cloud en **Supabase** / **Neon** o contenedor local para tests).
- Variables de entorno configuradas para el backend:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>?sslmode=require&prepareThreshold=0`
  - `SPRING_DATASOURCE_USERNAME=<user>`
  - `SPRING_DATASOURCE_PASSWORD=<password>`
  - `JWT_SECRET=<clave-de-al-menos-32-bytes>`

## 2. Verificación de Migraciones Flyway y Esquema (`database-ddl-contract.sql`)

1. Inicia la aplicación o ejecuta los tests de repositorio:
   ```powershell
   ./mvnw test -Dtest=*Repository*Test
   ```
2. **Resultado esperado**:
   - Flyway aplica `V1__init_store_tournament_schema.sql` sin errores.
   - Hibernate 6 (`ddl-auto: validate`) confirma que las entidades JPA coinciden exactamente con las tablas `users`, `stores`, `store_members`, `decks`, `tournament_templates`, `tournaments`, `scryfall_cards`, `tournament_registrations`, `registration_deck_cards`, `rounds` y `matches`.

## 3. Escenarios de Validación Funcional

### Escenario A: Alta de Dueño, Tienda y Torneo con Restricciones (US1)
1. Autentícate como usuario con rol `ADMIN` (Dueño) en `/api/v1/auth/login`.
2. Crea una tienda o selecciona la tienda donde el usuario es `owner_id`.
3. Envía un `POST /api/v1/admin/stores/{storeId}/tournaments` con:
   - `format`: `"COMMANDER"`
   - `maxDeckBudget`: `100.00`
   - `entryFee`: `5.00`
   - `prizeDescription`: `"Vales de tienda para el Top 4"`
   - `scheduledAt`: `"2026-11-15T17:00:00Z"`
4. **Resultado esperado**: Respuesta `201 Created` y registro persistido en la tabla `tournaments` vinculado a `stores.id`.

### Escenario B: Registro de Mazo de Moxfield y Validación de Presupuesto/Formato con Scryfall (US2)
1. Autentícate como jugador (`ROLE_PLAYER`).
2. Registra un mazo enviando únicamente su enlace en `POST /api/v1/player/decks` (`moxfieldUrl`).
3. Solicita inscripción en `POST /api/v1/player/tournaments/{tournamentId}/registrations`.
4. **Resultado esperado**:
   - Si todas las cartas son legales en `COMMANDER` según `scryfall_cards.legalities` y la suma de precios de Scryfall es `<= 100.00`: devuelve `201 Created`, inserta fila en `tournament_registrations` con el `deck_snapshot` en `JSONB` y puebla `registration_deck_cards`.
   - Si el coste supera `100.00` o alguna carta no es legal en el formato: devuelve `422 Unprocessable Entity` indicando las cartas o el exceso de presupuesto (`BudgetValidator`).

### Escenario C: Consulta de Estadísticas Basadas en Scryfall (US3)
1. Realiza un `GET /api/v1/public/analytics/stores/{storeId}/cards`.
2. **Resultado esperado**: Devuelve en menos de 200 ms las cartas más jugadas, la curva de maná media, la distribución de colores y el precio medio de los mazos leyendo directamente de `scryfall_cards`, `registration_deck_cards` y `tournament_registrations` sin realizar peticiones HTTP externas a Scryfall ni Moxfield.


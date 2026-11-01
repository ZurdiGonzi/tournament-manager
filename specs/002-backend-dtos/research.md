# Research: Definición y Validación de DTOs en Java 21 y Spring Boot 3.x

**Feature**: `002-backend-dtos` | **Fecha**: 2026-11-01

Este documento consolida las decisiones técnicas de diseño para los Data Transfer Objects (DTOs) del backend de **Tournament Manager**, garantizando cumplimiento con el Principio I de la Constitución (Java 21 `record` inmutables, `application/dto`) y desacoplamiento limpio con el dominio.

---

## 1. Java 21 Records como DTOs y Validación con Jakarta Validation

### Decisión
Todos los DTOs de solicitud (`Request`) y respuesta (`Response`) se implementarán como **`record` de Java 21** anotados con Jakarta Validation (`jakarta.validation.constraints.*`) en el constructor canónico o en los componentes del record.

### Justificación
- **Inmutabilidad estricta**: Los records generan automáticamente campos `final`, constructor canónico, `equals()`, `hashCode()` y `toString()`, evitando efectos colaterales y mutaciones accidentales en la capa de aplicación o transporte.
- **Soporte nativo en Spring Boot 3 y Jackson 2.15+**: Jackson deserializa records nativamente sin necesidad de constructores vacíos ni setters mutables.
- **Validación declarativa**: Jakarta Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Pattern`) se aplica directamente a los componentes del record y es interceptada automáticamente por `@Valid` en los métodos de los controladores `@RestController`.

### Alternativas Consideradas
- **Clases POJO tradicionales con Lombok (`@Data`, `@Getter`/`@Setter`)**: Descartado. Introduce mutabilidad innecesaria, dependencias externas de procesamiento de anotaciones y contradice explícitamente el Principio I de la Constitución.
- **Clases inmutables manuales con constructores prolijos**: Descartado por boilerplate excesivo en comparación con `record` de Java 21.

---

## 2. Modelado de Estados Cerrados y Enums

### Decisión
Los estados del ciclo de vida y clasificaciones finitas se modelarán mediante `enum` estándar de Java serializados como Strings (JSON string representation en formato `SCREAMING_SNAKE_CASE`):
- `UserRole`: `PLAYER`, `ADMIN`.
- `TournamentFormat`: `STANDARD`, `COMMANDER`, `MODERN`, `PAUPER`.
- `TournamentStatus`: `DRAFT`, `REGISTRATION_OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.
- `RegistrationStatus`: `PENDING`, `CONFIRMED`, `CANCELLED`, `DROPPED`.
- `RoundStatus`: `ACTIVE`, `FINISHED`.
- `MatchStatus`: `PENDING`, `SUBMITTED`, `CONFIRMED`, `DISPUTED`.

Para los resultados de validación o respuestas polimórficas (como respuestas de éxito vs error de validación de mazo), se utilizarán interfaces selladas (`sealed interface`) si se requiere control exhaustivo de tipos en compilación.

### Justificación
- Los enums aseguran integridad semántica tanto en la deserialización JSON como en la lógica de negocio.
- Evita cadenas de texto arbitrarias ("magic strings") en el frontend y en la API REST.

### Alternativas Consideradas
- **Strings libres con validación `@Pattern`**: Propenso a errores tipográficos y dificulta el tipado estricto en el frontend TypeScript.

---

## 3. Manejo de Fechas, Tiempos y Zonas Horarias

### Decisión
Todos los campos temporales (`scheduledAt`, `createdAt`, `submittedAt`, `validUntil`) se tiparán como **`java.time.Instant`** o **`java.time.OffsetDateTime`**, serializándose en formato estándar **ISO-8601 UTC** (`YYYY-MM-DDTHH:mm:ssZ`).

### Justificación
- La manipulación de instantes UTC normalizados previene errores de desfase horario entre tiendas locales y jugadores en distintas regiones o husos horarios.
- Jackson en Spring Boot 3 (`com.fasterxml.jackson.datatype:jackson-datatype-jsr310`) serializa `Instant` de manera canónica y eficiente a cadenas ISO-8601.

### Alternativas Consideradas
- `java.util.Date` o `java.sql.Timestamp`: Tecnologías legadas prohibidas en código moderno de Java 21.
- `LocalDateTime`: Descartado para eventos sincronizados en vivo porque carece de información de zona horaria o desplazamiento UTC.

---

## 4. Tipado Económico y de Precios (Límites de Presupuesto y Cartas)

### Decisión
Todos los valores económicos y precios (precio de entrada, presupuesto máximo de torneo, precio de carta individual, coste acumulado de mazo) se representarán mediante **`java.math.BigDecimal`** con escala de 2 decimales y modo de redondeo explícito (`RoundingMode.HALF_UP`).

### Justificación
- Los tipos primitivos de coma flotante (`double`, `float`) presentan errores inherentes de precisión binaria inadmisibles al verificar si un mazo excede por 1 céntimo el presupuesto permitido (ej. 50.00 €).
- `BigDecimal` mapea con total fidelidad al tipo `numeric(10,2)` de PostgreSQL y a la especificación de datos de Scryfall.

### Alternativas Consideradas
- `Double`: Descartado por imprecisión en operaciones aritméticas de suma de listas de 60-100 cartas.
- `Integer` (céntimos): Válido pero introduce conversiones adicionales en la serialización JSON frente a la notación de precios de Scryfall.

---

## 5. Respuestas de Error Estandarizadas (RFC 7807 ProblemDetail)

### Decisión
Los errores de la API se gestionarán mediante el estándar **RFC 7807 (`ProblemDetail`)** provisto nativamente en Spring Framework 6 / Spring Boot 3, complementado con un record de detalle de violaciones de campo (`FieldErrorDto`).

Estructura de payload de error:
- `type`: URI con la categoría de error (ej. `urn:problem:validation-error`, `urn:problem:deck-budget-exceeded`).
- `title`: Resumen corto legible (ej. "Error de validación en la solicitud").
- `status`: Código HTTP numérico (400, 401, 403, 404, 409, 422).
- `detail`: Explicación detallada del fallo específico.
- `instance`: URI de la petición que generó el error.
- `timestamp`: Marca de tiempo UTC.
- `invalidParams`: Lista de `FieldErrorDto(field, rejectedValue, message)`.

### Justificación
- Estándar IETF adoptado por la industria que evita formatos ad-hoc de error.
- Permite al frontend React mapear errores directamente a los campos del formulario.

---

## 6. Organización de Paquetes dentro de `com.tournament.app.application.dto`

### Decisión
Los DTOs se estructurarán en subpaquetes cohesivos por subdominio funcional dentro de `com.tournament.app.application.dto`:
- `auth`: `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserProfileResponse`.
- `store`: `CreateStoreRequest`, `StoreDetailResponse`, `StoreSummaryResponse`.
- `deck`: `RegisterDeckRequest`, `DeckSummaryResponse`, `DeckSnapshotDto`, `DeckCardDto`, `DeckValidationFailureResponse`.
- `tournament`: `CreateTournamentRequest`, `TournamentDetailResponse`, `TournamentSummaryResponse`, `TournamentRestrictionsDto`.
- `registration`: `TournamentRegistrationRequest`, `TournamentRegistrationResponse`.
- `round`: `RoundDetailResponse`, `MatchPairingResponse`, `ReportMatchResultRequest`, `MatchResultResponse`, `TournamentStandingsResponse`, `StandingEntryResponse`.
- `league`: `LeagueDetailResponse`, `LeagueStandingEntryResponse`.
- `analytics`: `MetagameAnalyticsResponse`, `ColorBreakdownDto`, `CardUsageStatDto`.
- `common`: `ApiErrorResponse`, `FieldErrorDto`.

### Justificación
- Evita un paquete "cajón de sastre" con más de 20 records planos.
- Facilita la navegación del código, el mantenimiento por subagentes y la asignación de responsabilidades.


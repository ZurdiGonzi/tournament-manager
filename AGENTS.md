# Tournament Manager - Guía de Arquitectura y Reglas de Agentes (`AGENTS.md`)

Este repositorio contiene el sistema **Tournament Manager**. Todos los agentes y subagentes que trabajen en este proyecto deben cumplir estrictamente las siguientes directrices tecnológicas y de arquitectura.

## Stack Tecnológico Principal

| Capa | Tecnología | Propósito y Convenciones Clave |
| :--- | :--- | :--- |
| **Backend** | Java 21 + Spring Boot 3.x | Lógica de dominio compleja (reglas de torneos, emparejamientos, desempates, validación), `records` para DTOs/eventos, `sealed interfaces` para estados y `@Scheduled` para tareas recurrentes. |
| **Seguridad** | Spring Security 6 + JWT | Autenticación **stateless** (`SessionCreationPolicy.STATELESS`). Segregación estricta de rutas y métodos para endpoints públicos, de jugador (`ROLE_PLAYER`) y de administrador (`ROLE_ADMIN`). |
| **Base de Datos** | PostgreSQL (Supabase / Neon) | Modelo relacional íntegro para torneos, inscripciones, rondas y partidas. Uso de columnas **`JSONB`** (`@JdbcTypeCode(SqlTypes.JSON)`) para almacenar snapshots inmutables de mazos (decks) al momento del registro. |
| **Scheduler** | Spring Task Scheduling (`@Scheduled`) | Creación periódica e idempotente de torneos recurrentes y cierre automático de rondas expiradas sin dependencias pesadas externas. |
| **Frontend** | React + TypeScript | `strict: true` en `tsconfig.json`. Tipos discriminados (discriminated unions) para estados de partidas/rondas, paneles de administración protegidos por rol y visualización de rondas en tiempo real. |

## Reglas Generales de Código y Arquitectura Hexagonal / Clean

0. **Estructura de Paquetes Backend (`com.tournament.app`)**:
   - `domain/` (`model`, `service`, `repository`): **Núcleo puro sin dependencias de frameworks** (ni anotaciones de Spring ni de JPA/Hibernate). Contiene modelos de dominio (`Deck`, `Match`, `Tournament`, `User`), servicios puros (`SwissPairingEngine`, `BudgetValidator`) y puertos de repositorio (`TournamentRepository`, `RegistrationRepository`).
   - `application/` (`usecase`, `dto`): Casos de uso (`RegisterPlayerUseCase`, `GeneratePairingsUseCase`, `ReportMatchResultUseCase`, `ScheduleTournamentUseCase`) y DTOs de aplicación.
   - `infrastructure/` (`adapter/moxfield`, `adapter/persistence/jpa`, `adapter/persistence/repository`, `security`, `scheduler`): Adaptadores externos (`MoxfieldClient`), entidades JPA y adaptadores de persistencia, seguridad (`SecurityConfig`, `JwtTokenProvider`, `CustomUserDetailsService`) y tareas programadas (`TournamentAutoScheduler`).
   - `presentation/`: Controladores REST (`AuthController`, `PlayerTournamentController`, `AdminTournamentController`, `AnalyticsController`).

1. **Java 21 Moderno**:
   - Usa `record` para todos los DTOs de entrada/salida y objetos de valor inmutables (como snapshots de mazos y resultados de partida).
   - Usa Pattern Matching for `switch` y `sealed` interfaces para modelar transiciones de estado del torneo (`DRAFT`, `REGISTRATION_OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
   - Valida siempre entradas con Jakarta Validation (`@Valid`, `@NotNull`, etc.) y maneja errores con `@RestControllerAdvice` usando `ProblemDetail` (RFC 7807).

2. **Seguridad (Spring Security + JWT)**:
   - Nunca uses sesiones HTTP (`HttpSession`).
   - Protege los endpoints bajo prefijos claros:
     - `/api/v1/public/**`: Consulta de torneos públicos, brackets, clasificaciones (`permitAll()`).
     - `/api/v1/player/**`: Inscripción a torneos, envío de mazo (decklist), reporte/confirmación de resultados (`hasAnyRole('PLAYER', 'ADMIN')`).
     - `/api/v1/admin/**`: Creación manual/configuración de plantillas recurrentes, resolución de disputas, generación forzada de rondas (`hasRole('ADMIN')`).

3. **Persistencia PostgreSQL y JSONB**:
   - Gestiona todos los cambios de esquema mediante migraciones versionadas (Flyway en `src/main/resources/db/migration`).
   - Los mazos presentados en una inscripción deben guardarse como **snapshot inmutable en `JSONB`** dentro de la tabla de inscripción (`tournament_registrations.deck_snapshot`), evitando que ediciones posteriores al mazo del usuario alteren torneos en curso o históricos.
   - Usa conexiones compatibles con el pooler de Supabase / Neon (HikariCP configurado adecuadamente para PgBouncer / transaction mode cuando aplique).

4. **Scheduler (`@Scheduled`)**:
   - Toda tarea programada debe ser **idempotente**: antes de generar un torneo recurrente, verifica mediante clave única (ej. `template_id` + `scheduled_for`) que no haya sido creado previamente.
   - Externaliza siempre las expresiones cron en `application.yml` (ej. `${tournament.scheduler.recurring-cron:0 0 2 * * *}`).

5. **Frontend React + TypeScript**:
   - Prohibido el uso de `any`. Define contratos equivalentes a los DTOs del backend.
   - Modela los estados asíncronos y de partidas mediante uniones discriminadas para evitar estados imposibles en la interfaz.


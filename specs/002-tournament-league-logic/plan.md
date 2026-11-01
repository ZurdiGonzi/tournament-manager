# Implementation Plan: Lógica de Negocio de Usuarios, Mazos con Scryfall, Torneos, Ligas, Fórmulas Matemáticas en BD y Estadísticas

**Branch**: `002-tournament-league-logic` | **Date**: 2026-11-01 | **Spec**: [spec.md](file:///C:/Users/Gonzalo/.gemini/antigravity/worktrees/tournament-manager/implement_business_logic_core/specs/002-tournament-league-logic/spec.md)

**Input**: Feature specification from `/specs/002-tournament-league-logic/spec.md`

## Summary

Diseñar e implementar el núcleo completo de lógica de negocio sobre la arquitectura limpia/hexagonal (`com.tournament.app`) y PostgreSQL (`V2__league_formulas_and_business_logic_schema.sql`), abarcando:
1. **Registro de Usuarios y Autenticación Stateless (JWT)**: Registro e inicio de sesión (`/api/v1/auth/register`, `/api/v1/auth/login`) segregando permisos entre `ROLE_PLAYER` (Jugador) y `ROLE_ADMIN` (Dueño de tienda / Organizador).
2. **Mazos en el Perfil e Inscripción a Torneos Conectados con Scryfall**: Guardado de mazos en el perfil del jugador (`POST /api/v1/player/decks`) e inscripción a torneos/jornadas permitiendo elegir un **mazo guardado en el perfil** o **añadir un mazo nuevo en el acto** (con opción de guardarlo en el perfil), validando formato/presupuesto contra **Scryfall** y persistiendo el `deck_snapshot` (`JSONB`) inmutable.
3. **Torneos, Ligas, Jornadas y Fórmulas Matemáticas Persistidas en Base de Datos**: Almacenamiento en la tabla `scoring_formulas` de las funciones matemáticas configurables y editables por el dueño de la tienda para calcular:
   - Puntos por cada partida del torneo (`MATCH_POINTS`),
   - Puntos por cada jornada de liga (`LEAGUE_MATCHDAY_POINTS`),
   - Emparejamientos (*pairings*) en cada jornada (`PAIRING_WEIGHT`),
   junto con la configuración del número de partidas por torneo (`max_rounds`) y el cálculo progresivo de puntos en cada jornada cuando el organizador introduce los resultados de las mesas.
4. **Estadísticas de Jornada/Liga y Estadísticas de *"Tu Mazo"* con Scryfall**: Cálculo de cartas más jugadas, colores e identidades de color más jugados, comandantes más ganadores, porcentajes de enfrentamientos directos entre rivales/comandantes (ej. *"Magda vs Kefka: gana Kefka 60%"*) y panel personal *"Tu Mazo"* tanto por jornada como en el acumulado de liga/histórico.

## Technical Context

**Language/Version**: Java 21 (Backend) + TypeScript 5.x `strict: true` (Frontend)

**Primary Dependencies**: Spring Boot 3.x (Spring Data JPA / Hibernate 6, Spring Security 6 + JWT, Jakarta Validation, Spring Web `RestClient`), Flyway, PostgreSQL JDBC Driver

**Storage**: PostgreSQL 15+ (Supabase / Neon) con migraciones Flyway (`V1` + `V2`), `JSONB` (`@JdbcTypeCode(SqlTypes.JSON)`), arrays `TEXT[]` y tabla `scoring_formulas` para las funciones matemáticas del dueño

**Testing**: JUnit 5 + AssertJ (tests unitarios puros en `domain/` para evaluación de fórmulas matemáticas, *pairings*, validación de mazos y cálculo de puntos) + `@WebMvcTest` / Spring Security Test para autorización por rol

**Target Platform**: Servidor JVM 21 + PostgreSQL (Supabase / Neon) + SPA React/TypeScript

**Project Type**: Web application (Backend REST API con Arquitectura Hexagonal + Frontend React/TypeScript)

**Performance Goals**: Evaluación de fórmulas matemáticas, generación de *pairings* y recálculo de puntos de jornada/liga en `< 500ms`; consultas de estadísticas de jornada, *matchups* (*Magda vs Kefka*) y *"Tu Mazo"* en `< 200ms` usando datos locales de Scryfall

**Constraints**: `domain/` 100% libre de dependencias de Spring/JPA/Jackson; sesiones HTTP prohibidas (`SessionCreationPolicy.STATELESS`); reporte de resultados de partidas restringido exclusivamente al organizador/dueño de la tienda (`ROLE_ADMIN`); compatibilidad con PgBouncer (`prepareThreshold=0`)

**Scale/Scope**: Soporte multi-tienda con ligas de múltiples jornadas, decenas de jugadores por jornada, mesas 1v1 (*Standard*) y multijugador (*Commander*) y miles de cartas sincronizadas con Scryfall

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio de la Constitución (`v1.0.0`) | Evaluación Pre-Phase 0 | Evaluación Post-Phase 1 | Justificación / Evidencia en el Diseño |
| :--- | :---: | :---: | :--- |
| **I. Núcleo de Dominio Puro y Tipado en Java 21** | ✅ PASS | ✅ PASS | `FormulaEvaluationEngine`, `SwissPairingEngine`, `LeagueStandingCalculator`, `DeckAnalyticsCalculator` y `BudgetValidator` residen en `domain/service/` en Java 21 puro sin importar paquetes de Spring ni JPA. Todos los DTOs son `record` y los estados usan `enum`/`sealed interfaces`. |
| **II. Seguridad Stateless y Segregación por Roles con JWT** | ✅ PASS | ✅ PASS | `/api/v1/auth/**` y `/api/v1/public/analytics/**` son públicos; `/api/v1/player/**` exige jugador autenticado validando propiedad del mazo/inscripción; `/api/v1/admin/**` (fórmulas en BD, creación de torneos/ligas, *pairings* y registro de resultados de partidas) exige `ROLE_ADMIN` y propiedad de la tienda. |
| **III. Control Estricto de Tipos en Frontend (React + TS)** | ✅ PASS | ✅ PASS | Contratos tipados sin `any` para mazos de perfil, fórmulas matemáticas, rondas, estadísticas de jornada (*matchups*) y estadísticas de *"Tu Mazo"*. |
| **IV. Integridad Relacional en PostgreSQL y Snapshots en JSONB** | ✅ PASS | ✅ PASS | Migración Flyway `V2__league_formulas_and_business_logic_schema.sql` añade `scoring_formulas`, `leagues`, `league_standings` y `match_participants` manteniendo `deck_snapshot JSONB NOT NULL` inmutable por jornada. |
| **V. Planificación Nativa e Idempotente (`@Scheduled`)** | ✅ PASS | ✅ PASS | Compatible con `TournamentAutoScheduler` y la restricción `UNIQUE (template_id, scheduled_at)` definida en `V1`. |

## Project Structure

### Documentation (this feature)

```text
specs/002-tournament-league-logic/
├── plan.md                                             # Este archivo (salida de /speckit-plan)
├── research.md                                         # Decisiones técnicas y de dominio (Phase 0)
├── data-model.md                                       # Modelo ER y tablas V2 con fórmulas en BD y ligas (Phase 1)
├── quickstart.md                                       # Escenarios de validación extremo a extremo (Phase 1)
├── contracts/
│   ├── database-v2-formulas-and-leagues.sql            # Contrato SQL DDL (Flyway V2)
│   └── rest-api-contracts.md                           # Contratos REST (Auth, Player, Admin, Analytics)
└── tasks.md                                            # Se generará en Phase 2 mediante /speckit-tasks
```

### Source Code (repository root)

```text
src/main/java/com/tournament/app/
├── domain/                                             # Núcleo puro Java 21 (0 dependencias de frameworks)
│   ├── model/
│   │   ├── User.java
│   │   ├── Store.java
│   │   ├── Deck.java                                   # Mazo guardado en perfil + metadatos Scryfall (comandante, colores)
│   │   ├── ScryfallCard.java
│   │   ├── ScoringFormula.java                         # Fórmula matemática persistida editable por el dueño
│   │   ├── FormulaType.java                            # MATCH_POINTS | LEAGUE_MATCHDAY_POINTS | PAIRING_WEIGHT
│   │   ├── League.java                                 # Liga con referencias a sus fórmulas y jornadas
│   │   ├── LeagueStanding.java                         # Puntuación acumulada por jugador en la liga
│   │   ├── Tournament.java                             # Torneo / Jornada de liga con maxRounds y fórmulas
│   │   ├── TournamentRegistration.java                 # Inscripción con snapshot inmutable y puntos de jornada
│   │   ├── Round.java
│   │   ├── Match.java
│   │   └── MatchParticipant.java                       # Resultado por jugador/comandante en cada mesa
│   ├── service/
│   │   ├── FormulaEvaluationEngine.java                # Evaluador matemático puro para las fórmulas guardadas en BD
│   │   ├── SwissPairingEngine.java                     # Generador de pairings (1v1 y pods Commander) usando la fórmula
│   │   ├── LeagueScoringService.java                   # Cálculo progresivo de puntos de partida y de jornada de liga
│   │   ├── BudgetValidator.java                        # Validación de formato y presupuesto con datos de Scryfall
│   │   └── DeckAnalyticsService.java                   # Cálculo de estadísticas de jornada, matchups (Magda vs Kefka) y "Tu Mazo"
│   └── repository/
│       ├── UserRepository.java
│       ├── StoreRepository.java
│       ├── DeckRepository.java
│       ├── ScryfallCardRepository.java
│       ├── ScoringFormulaRepository.java
│       ├── LeagueRepository.java
│       ├── TournamentRepository.java
│       ├── RegistrationRepository.java
│       └── MatchRepository.java
├── application/                                        # Casos de uso y DTOs (records)
│   ├── usecase/
│   │   ├── RegisterUserUseCase.java
│   │   ├── AuthenticateUserUseCase.java
│   │   ├── SaveProfileDeckUseCase.java                 # Guarda mazo en perfil sincronizado con Scryfall
│   │   ├── RegisterPlayerUseCase.java                  # Apunta a torneo con mazo guardado o nuevo + snapshot JSONB
│   │   ├── ManageScoringFormulaUseCase.java            # Crea/edita fórmulas matemáticas del dueño en BD
│   │   ├── CreateLeagueUseCase.java
│   │   ├── CreateTournamentUseCase.java                # Define número de partidas (maxRounds) y fórmulas
│   │   ├── GeneratePairingsUseCase.java                # Calcula pairings de la jornada con la fórmula del dueño
│   │   ├── ReportMatchResultUseCase.java               # Exclusivo Dueño: registra resultado y recalcula puntos
│   │   └── GetDeckAnalyticsUseCase.java                # Estadísticas de jornada, liga y "Tu Mazo"
│   └── dto/
├── infrastructure/                                     # Adaptadores externos, persistencia JPA y seguridad JWT
│   ├── adapter/
│   │   ├── moxfield/
│   │   │   └── MoxfieldClient.java
│   │   ├── scryfall/
│   │   │   └── ScryfallClient.java
│   │   └── persistence/
│   │       ├── jpa/                                    # Entidades JPA (*Entity.java)
│   │       └── repository/                             # Implementaciones Spring Data JPA
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtTokenProvider.java
│   │   └── CustomUserDetailsService.java
│   └── scheduler/
│       └── TournamentAutoScheduler.java
└── presentation/                                       # Controladores REST y manejo de errores RFC 7807
    ├── AuthController.java                             # /api/v1/auth/** (register, login)
    ├── PlayerTournamentController.java                 # /api/v1/player/** (mazos en perfil, inscripción, "Tu Mazo")
    ├── AdminTournamentController.java                  # /api/v1/admin/** (fórmulas BD, ligas, torneos, pairings, resultados)
    ├── AnalyticsController.java                        # /api/v1/public/analytics/** (jornada, liga, Magda vs Kefka)
    └── GlobalExceptionHandler.java                     # @RestControllerAdvice con ProblemDetail

src/main/resources/
├── application.yml
└── db/migration/
    ├── V1__init_store_tournament_schema.sql
    └── V2__league_formulas_and_business_logic_schema.sql
```

**Structure Decision**: Se mantiene la estructura limpia de cuatro capas en `src/main/java/com/tournament/app/` establecida en la Constitución, añadiendo los modelos y servicios de dominio para fórmulas matemáticas persistidas en BD (`ScoringFormula`, `FormulaEvaluationEngine`), ligas (`League`, `LeagueScoringService`) y analítica avanzada de comandantes y mazos (`DeckAnalyticsService`), junto con la migración Flyway `V2`.

## Complexity Tracking

> **No existen violaciones a la Constitución (`0` excepciones requeridas).**

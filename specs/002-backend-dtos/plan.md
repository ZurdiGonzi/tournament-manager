# Implementation Plan: Contratos y DTOs del Backend

**Branch**: `002-backend-dtos` | **Date**: 2026-11-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/002-backend-dtos/spec.md`

## Summary

Definición exhaustiva y tipado estricto de los Data Transfer Objects (DTOs) del backend de **Tournament Manager** utilizando `record` inmutables de **Java 21** y **Jakarta Validation** dentro de `com.tournament.app.application.dto`. Los contratos cubren autenticación y perfil, administración de tiendas, mazos vinculados a Moxfield, configuración y registro de torneos con snapshot inmutable en JSONB, rondas y emparejamientos suizos, reporte de resultados y desempates, ligas multijornada, analíticas de metajuego con Scryfall y tratamiento uniforme de errores según RFC 7807 (`ProblemDetail`).

## Technical Context

**Language/Version**: Java 21 (LTS)

**Primary Dependencies**: Spring Boot 3.x (`spring-boot-starter-validation`, `spring-boot-starter-web`, `jackson-datatype-jsr310`), Jakarta Validation API

**Storage**: N/A para la definición de DTOs (capa de transporte/aplicación en memoria; persistencia de snapshots en PostgreSQL JSONB en features posteriores)

**Testing**: JUnit 5, AssertJ, Hibernate Validator (`Validation.buildDefaultValidatorFactory()`), Jackson `ObjectMapper`

**Target Platform**: Servidor backend en máquina virtual Java (JVM) / Contenedor Docker Linux

**Project Type**: Servicio web RESTful / Arquitectura Hexagonal (Capa de Aplicación `application/dto`)

**Performance Goals**: Deserialización y validación de payloads en < 20 ms sin sobrecarga de reflexión mutable

**Constraints**: Inmutabilidad estricta (`record`), ausencia total de dependencias de frameworks en `domain/`, serialización de fechas en ISO-8601 UTC (`Instant`), precisión monetaria mediante `BigDecimal` (2 decimales)

**Scale/Scope**: 28 records y 6 enums organizados en 9 subpaquetes modulares

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio Constitucional | Estado | Evidencia y Justificación |
| :--- | :---: | :--- |
| **I. Núcleo de Dominio Puro y Tipado en Java 21** | **PASS** | Todos los DTOs residen en `com.tournament.app.application.dto` y se declaran como `record` de Java 21 inmutables. El paquete `domain/` permanece limpio de anotaciones de Spring, Jackson o JPA. |
| **II. Seguridad Stateless y Segregación por Roles con JWT** | **PASS** | Los DTOs de autenticación (`LoginRequest`, `AuthResponse`) entregan tokens JWT sin estado, y se definen roles explícitos (`PLAYER`, `ADMIN`) alineados con la segregación de rutas `/api/v1/public/**`, `/api/v1/player/**` y `/api/v1/admin/**`. |
| **III. Control Estricto de Tipos en Frontend (React + TypeScript)** | **PASS** | Cada DTO y enum posee una correspondencia directa 1:1 para modelar contratos y tipos discriminados en el frontend sin recurrir a tipos abiertos o `any`. |
| **IV. Integridad Relacional y Snapshots Inmutables en JSONB** | **PASS** | Se define `DeckSnapshotDto` conteniendo la fotografía completa del mazo, cartas, cantidades y precios unitarios capturados de Scryfall al inscribirse. |
| **V. Planificación Nativa e Idempotente (@Scheduled)** | **PASS** | Los DTOs de torneos soportan parámetros reutilizables para instanciación programada e idempotente. |

## Project Structure

### Documentation (this feature)

```text
specs/002-backend-dtos/
├── plan.md              # Este plan de implementación
├── research.md          # Investigación de diseño, validación y serialización
├── data-model.md        # Definición estructurada de modelos y contratos DTO
├── quickstart.md        # Guía de validación y pruebas de DTOs
├── contracts/           # Contratos Java 21 y esquemas JSON
│   ├── dto-records-contract.md
│   └── rest-api-schemas.json
└── checklists/
    └── requirements.md  # Checklist de validación de calidad de la especificación
```

### Source Code (repository root)

```text
src/main/java/com/tournament/app/
├── domain/                               # Núcleo de negocio puro (sin frameworks)
│   ├── model/                            # Enums compartidos: UserRole, TournamentFormat, etc.
│   ├── repository/
│   └── service/
├── application/
│   ├── dto/                              # DTOs de Java 21 organizados por subdominio
│   │   ├── auth/                         # RegisterRequest, LoginRequest, AuthResponse, UserProfileResponse
│   │   ├── store/                        # CreateStoreRequest, StoreDetailResponse, StoreSummaryResponse
│   │   ├── deck/                         # RegisterDeckRequest, DeckSummaryResponse, DeckSnapshotDto, DeckCardDto
│   │   ├── tournament/                   # CreateTournamentRequest, TournamentDetailResponse, TournamentRestrictionsDto
│   │   ├── registration/                 # TournamentRegistrationRequest, TournamentRegistrationResponse
│   │   ├── round/                        # RoundDetailResponse, MatchPairingResponse, ReportMatchResultRequest, TournamentStandingsResponse
│   │   ├── league/                       # LeagueDetailResponse, LeagueStandingEntryResponse
│   │   ├── analytics/                    # MetagameAnalyticsResponse, ColorBreakdownDto, CardUsageStatDto
│   │   └── common/                       # ApiErrorResponse (RFC 7807), FieldErrorDto
│   └── usecase/                          # Orquestación de casos de uso
├── infrastructure/                       # Adaptadores JPA, clientes HTTP y seguridad
└── presentation/                         # Controladores REST con validación @Valid
```

**Structure Decision**: Se adopta la estructura modular limpia por subdominios funcionales dentro de `com.tournament.app.application.dto`, encapsulando los DTOs en 9 paquetes cohesivos para evitar acoplamientos y facilitar el mantenimiento.

## Complexity Tracking

> **Sin violaciones a la Constitución detectadas.**

---
name: spring-boot-tournament-backend
description: >-
  Use this skill when designing, implementing, or testing Backend logic in Java 21
  and Spring Boot 3.x, including tournament rules, Swiss/Elimination pairings,
  match result validation, and recurring tournaments with @Scheduled.
---

# Spring Boot 3.x & Java 21 Tournament Backend Skill

Utiliza esta guía paso a paso cuando trabajes en el backend de **Tournament Manager** con Java 21 y Spring Boot 3.x.

## 1. Estructura de Paquetes (Arquitectura Hexagonal / Clean en `com.tournament.app`)

Respeta estrictamente la separación de capas en `src/main/java/com/tournament/app/`:
- `domain/` (**Núcleo puro sin dependencias de frameworks**):
  - `domain.model`: `Deck`, `Match`, `Tournament`, `User`.
  - `domain.service`: `SwissPairingEngine` (algoritmo de emparejamiento suizo puro), `BudgetValidator` (comprobación de costes/presupuesto de mazos).
  - `domain.repository`: Puertos/interfaces de dominio (`TournamentRepository`, `RegistrationRepository`).
- `application/` (**Casos de uso**):
  - `application.usecase`: `RegisterPlayerUseCase`, `GeneratePairingsUseCase`, `ReportMatchResultUseCase`, `ScheduleTournamentUseCase`.
  - `application.dto`: Records de entrada/salida de la capa de aplicación.
- `infrastructure/` (**Adaptadores externos y frameworks**):
  - `infrastructure.adapter.moxfield`: `MoxfieldClient` (`RestClient` / `WebClient`), `MoxfieldDeckResponse`.
  - `infrastructure.adapter.persistence.jpa` y `infrastructure.adapter.persistence.repository`: Entidades JPA (con `@JdbcTypeCode(SqlTypes.JSON)` para snapshots `JSONB`) e implementaciones de los repositorios de dominio.
  - `infrastructure.security`: `SecurityConfig`, `JwtTokenProvider`, `CustomUserDetailsService`.
  - `infrastructure.scheduler`: `TournamentAutoScheduler` (cron jobs con `@Scheduled`).
- `presentation/` (**Controladores REST**):
  - `AuthController`, `PlayerTournamentController`, `AdminTournamentController`, `AnalyticsController`.

## 2. Patrones de Dominio en Java 21

### Máquina de Estados de Torneo y Partida
- Define estados claros en enums con métodos de transición validados:
  - Torneo: `DRAFT -> REGISTRATION_OPEN -> CHECK_IN -> IN_PROGRESS -> COMPLETED / CANCELLED`.
  - Partida (`Match`): `PENDING -> IN_PROGRESS -> AWAITING_CONFIRMATION -> REPORTED / DISPUTED -> CONFIRMED`.
- Lanza excepciones de dominio específicas (`InvalidTournamentStateException`, `IllegalPairingException`) cuando una operación viole las reglas del torneo.

### Algoritmo de Emparejamientos (Pairings)
1. **Primera ronda**: Emparejamiento aleatorio (con semilla reproducible para auditoría) o por *seeding* (ELO/ranking).
2. **Rondas siguientes (Suizo)**:
   - Agrupa jugadores por puntos actuales.
   - Evita revanchas (*rematches*) verificando el historial de enfrentamientos del torneo.
   - Si hay número impar de jugadores activos, asigna el *BYE* al jugador con menor puntuación que aún no haya recibido un *BYE*.

## 3. Tareas Programadas (`@Scheduled`)

Para la creación periódica de torneos recurrentes sin dependencias pesadas:
1. Habilita `@EnableScheduling` en una clase de configuración dedicada (`SchedulingConfig`), separada de la clase principal para facilitar tests integrados sin disparar crons.
2. Configura un pool de hilos dedicado en `application.yml`:
   ```yaml
   spring:
     task:
       scheduling:
         pool:
           size: 2
         thread-name-prefix: tournament-sched-
   ```
3. **Idempotencia**:
   - Cada `TournamentTemplate` activo debe tener su expresión cron o frecuencia y una marca `next_execution_at`.
   - Al generar la instancia de `Tournament`, registra `(template_id, scheduled_date)` con una restricción `UNIQUE` en PostgreSQL para evitar duplicados.

## 4. Validación del Paso
- Ejecuta `./mvnw test` (o `gradlew test`) asegurando pruebas unitarias para:
  - Emparejamientos sin enfrentamientos repetidos ni múltiples BYEs al mismo jugador.
  - Cálculo exacto de standings y tie-breakers.
  - Idempotencia del scheduler al ejecutarse dos veces seguidas en el mismo instante.


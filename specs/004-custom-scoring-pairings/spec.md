# Feature Specification: Configuración Personalizada de Pairings, Puntuación de Torneo y Puntuación de Ligas

**Feature Branch**: `004-custom-scoring-pairings`

**Created**: 2026-11-01

**Status**: Draft

**Input**: User description: "quiero que ahora agregues a la base de datos que el dueño de la tienda en sus torneos pueda incluir su propia formula de sistema de pairings, sistema de puntos por victoria y ssitema de puntos en liga"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Configuración Personalizada de Puntuación de Torneo (Victoria, Empate, Derrota, BYE) (Priority: P1)

Como **Dueño de tienda**, quiero definir las reglas de puntuación de mis torneos indicando cuántos puntos se otorgan por victoria (ej. 3 puntos tradicionales, o 2 puntos en eventos casuales), por empate (ej. 1 punto o 0), por derrota (habitualmente 0) y por descanso/BYE (ej. 3 puntos), para adaptar la puntuación competitiva al tipo de evento que organizo.

**Why this priority**: Es la base del cálculo de clasificaciones de cada torneo. Permite a las tiendas alejarse del estándar rígido de 3/1/0 si así lo desean para torneos temáticos, de novatos o de Commander multijugador.

**Independent Test**: Puede probarse creando un torneo con configuración personalizada (ej. victoria = 4 puntos, empate = 2 puntos, derrota = 0 puntos, BYE = 4 puntos), registrando resultados de partidas y comprobando que los puntos calculados en la tabla de clasificación reflejan la fórmula elegida en lugar de los valores fijos por defecto.

**Acceptance Scenarios**:

1. **Given** un dueño de tienda creando o editando un torneo, **When** especifica puntos personalizados por victoria (4), empate (2), derrota (0) y BYE (4), **Then** la base de datos almacena estas reglas asociadas al torneo.
2. **Given** un torneo creado sin especificar puntuación personalizada, **When** se persiste el torneo, **Then** el sistema aplica automáticamente los valores estándar internacionales (3 por victoria, 1 por empate, 0 por derrota, 3 por BYE).
3. **Given** un intento de configurar valores negativos de puntos para victoria o empate, **When** se intenta persistir, **Then** la base de datos rechaza la operación mediante restricciones de validación `CHECK`.

---

### User Story 2 - Elección y Parámetros del Sistema de Pairings (Priority: P1)

Como **Dueño de tienda**, quiero elegir el sistema de emparejamientos para cada torneo (Suizo tradicional, Eliminación directa, Todos contra todos / Round-Robin, o personalización algorítmica) y configurar sus directrices (política de BYE, prohibición estricta de revanchas o asignación por brackets), para que el gestor empareje a los jugadores según la dinámica deseada.

**Why this priority**: Diferentes tipos de eventos requieren mecánicas de pairing radicalmente distintas (un torneo clasificatorio requiere Suizo estricto, mientras que una final o un torneo relámpago requiere Eliminación directa).

**Independent Test**: Puede probarse creando un torneo con sistema `SINGLE_ELIMINATION` y otro con `SWISS` y parámetros específicos en formato estructurado (evitar revanchas activado y política de BYE al menor ranking), comprobando que la base de datos almacena y valida las opciones seleccionadas.

**Acceptance Scenarios**:

1. **Given** un dueño de tienda configurando un torneo, **When** selecciona el sistema de emparejamiento (ej. `SINGLE_ELIMINATION`, `ROUND_ROBIN` o `SWISS`), **Then** la base de datos valida que pertenezca a un conjunto permitido y almacena la opción junto con sus parámetros detallados de emparejamiento.
2. **Given** una configuración de emparejamiento personalizada (ej. parámetros para brackets o evitar revanchas), **When** se envía la configuración estructurada en formato JSONB, **Then** la base de datos almacena la configuración de emparejamiento de forma extensible y consultable.

---

### User Story 3 - Sistema Personalizado de Puntuación y Ranking para Ligas (Priority: P2)

Como **Dueño de tienda**, quiero definir la fórmula con la que los torneos computan para la clasificación general de mi Liga (ej. suma directa de puntos de partidas, puntos fijos por asistencia/participación a cada jornada, tabla de puntos por posición final en cada torneo como 10 pts al 1º / 6 pts al 2º, y descarte de las peores jornadas), para incentivar la asistencia continua y el rendimiento en la temporada.

**Why this priority**: Cada comunidad de tienda tiene su propio sistema de fidelización de liga; permitir reglas de asistencia y baremos por posición es clave para el éxito de ligas mensuales o de temporada.

**Independent Test**: Puede probarse configurando una liga con 2 puntos por asistencia a cada torneo y una escala de puntos por puesto (1º = 10, 2º = 6, 3º = 4), disputando torneos y comprobando que la clasificación general de la liga calcula la puntuación acumulada sumando asistencia, ranking de torneo y puntos de partida según las reglas de la liga.

**Acceptance Scenarios**:

1. **Given** una tienda organizando una liga, **When** el dueño configura el sistema de puntuación de la liga definiendo puntos por participación (ej. 2 puntos por torneo disputado) y una tabla de bonificación por posición en el torneo (ej. 1º=10, 2º=6, 3º=4, 4º=2), **Then** la base de datos almacena la regla de puntuación de la liga.
2. **Given** una liga con regla de considerar los mejores N torneos (descarte de jornadas), **When** se especifica `best_n_tournaments = 4`, **Then** la base de datos persiste este parámetro garantizando que sea un entero positivo.
3. **Given** la vista de clasificación de liga (`v_league_leaderboard`), **When** se consultan las posiciones de los jugadores, **Then** la vista desglosa y suma los puntos calculados de acuerdo con las directrices de la liga.

---

### Edge Cases

- **Valores negativos o no válidos**: Los puntos de victoria, empate, derrota, BYE o asistencia no pueden ser negativos (`CHECK >= 0`).
- **Valores por defecto ante configuraciones incompletas**: Si una tienda no especifica fórmula de liga o de torneo, el sistema debe aplicar por defecto el estándar oficial (Suizo clásico, 3/1/0/3 de puntos de torneo, y suma acumulativa directa en liga).
- **Descarte de torneos (`best_n_tournaments`) superior a los disputados**: Cuando un jugador ha disputado menos torneos que el límite N, computan todos sus torneos sin errores matemáticos.
- **Torneo con BYE en sistema personalizado**: El sistema debe otorgar los puntos definidos en `points_per_bye` (o en su defecto `points_per_win`) de forma determinista.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Puntuación Personalizada de Torneo)**: Las tablas `tournaments` y `tournament_templates` DEBEN incorporar columnas para la asignación de puntos de partida:
  - `points_per_win INT NOT NULL DEFAULT 3 CHECK (points_per_win >= 0)`
  - `points_per_draw INT NOT NULL DEFAULT 1 CHECK (points_per_draw >= 0)`
  - `points_per_loss INT NOT NULL DEFAULT 0 CHECK (points_per_loss >= 0)`
  - `points_per_bye INT NOT NULL DEFAULT 3 CHECK (points_per_bye >= 0)`
- **FR-002 (Configuración Avanzada de Puntuación de Torneo)**: Las tablas `tournaments` y `tournament_templates` DEBEN incluir una columna opcional `scoring_rules JSONB` para almacenar fórmulas o modificadores avanzados (ej. bonus por 2-0, puntos por diferencial de vidas o reglas de desempate personalizadas).
- **FR-003 (Sistema de Pairings del Torneo)**: Las tablas `tournaments` y `tournament_templates` DEBEN incorporar una columna `pairing_system VARCHAR(30) NOT NULL DEFAULT 'SWISS'` con restricción:
  `CHECK (pairing_system IN ('SWISS', 'SINGLE_ELIMINATION', 'ROUND_ROBIN', 'CUSTOM'))`.
- **FR-004 (Parámetros Extensibles de Pairings)**: Las tablas `tournaments` y `tournament_templates` DEBEN incluir una columna `pairing_config JSONB` para almacenar parámetros como:
  - `avoid_rematches` (`boolean`)
  - `bye_policy` (`'LOWEST_RANK'` o `'RANDOM'`)
  - `bracket_sort_criteria` (`text[]`)
- **FR-005 (Sistema de Puntuación de Liga)**: La tabla `leagues` DEBE incorporar una columna `scoring_system VARCHAR(30) NOT NULL DEFAULT 'CUMULATIVE_MATCH_POINTS'` con restricción:
  `CHECK (scoring_system IN ('CUMULATIVE_MATCH_POINTS', 'RANK_BASED', 'PARTICIPATION_PLUS_RECORD', 'CUSTOM'))`.
- **FR-006 (Puntos por Asistencia/Participación en Liga)**: La tabla `leagues` DEBE incorporar una columna `participation_points INT NOT NULL DEFAULT 0 CHECK (participation_points >= 0)` que otorgue puntos automáticos al jugador por el mero hecho de participar en cada torneo de la liga.
- **FR-007 (Regla de Descarte de Jornadas en Liga)**: La tabla `leagues` DEBE incorporar una columna `best_n_tournaments INT CHECK (best_n_tournaments IS NULL OR best_n_tournaments > 0)` para restringir el cómputo de la liga a las mejores N actuaciones de cada jugador.
- **FR-008 (Baremo por Posición en Liga)**: La tabla `leagues` DEBE incluir una columna `rank_points_distribution JSONB` que defina la escala de puntos otorgados según la posición final del jugador en cada torneo (ej. `{"1": 10, "2": 6, "3": 4, "4": 2, "top8": 1}`).
- **FR-009 (Adaptación de la Vista de Clasificación de Liga)**: La vista `v_league_leaderboard` DEBE actualizarse para incorporar el cómputo de puntos de participación y permitir visualizar la procedencia del total acumulado de cada jugador.
- **FR-010 (Plantillas Recurrentes Configurables)**: La tabla `tournament_templates` DEBE replicar exactamente todos los atributos de sistema de pairings y puntuación para que los torneos generados automáticamente por el `@Scheduled` hereden las fórmulas del organizador.

### Key Entities *(include if feature involves data)*

- **Reglas de Puntuación de Torneo**: Atributos en `tournaments` y `tournament_templates` que determinan el valor de victoria, empate, derrota y BYE.
- **Estrategia de Pairings**: Atributos en `tournaments` y `tournament_templates` que seleccionan el motor (`SWISS`, `SINGLE_ELIMINATION`, `ROUND_ROBIN`, `CUSTOM`) y sus parámetros en JSONB.
- **Estrategia de Puntuación de Liga**: Atributos en `leagues` que definen puntos por asistencia, escala por clasificación de torneo y reglas de descarte de jornadas.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001 (Flexibilidad de Puntuación de Torneo)**: El 100% de los torneos permiten a los administradores fijar valores arbitrarios no negativos para victorias, empates y BYEs sin estar atados rígidamente al esquema 3/1/0.
- **SC-002 (Integridad de Motores de Pairing)**: La base de datos restringe los sistemas de emparejamiento a los 4 tipos oficiales (`SWISS`, `SINGLE_ELIMINATION`, `ROUND_ROBIN`, `CUSTOM`) permitiendo parámetros extensibles en JSONB.
- **SC-003 (Soporte de Reglas de Liga Avanzadas)**: Las ligas admiten puntos por presencia física/participación, baremos por posición final en torneos y descarte de peores jornadas (`best_n_tournaments`).
- **SC-004 (Retrocompatibilidad e Idempotencia)**: Todos los torneos y ligas preexistentes o creados sin parámetros avanzados adoptan por defecto los valores estándar (Suizo clásico, 3/1/0/3 de torneo, sin coste de asistencia) sin provocar fallos de migración.

## Assumptions

- **Valores por defecto internacionales**: Si el dueño de la tienda no especifica valores, el sistema adopta automáticamente victoria = 3, empate = 1, derrota = 0, BYE = 3 y Suizo como algoritmo de emparejamiento.
- **Compatibilidad con Flyway**: Los cambios se implementarán actualizando la migración canónica `V1__init_schema.sql` (al estar en fase de diseño inicial) y los contratos de diseño del repositorio.


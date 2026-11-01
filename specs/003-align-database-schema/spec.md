# Feature Specification: Alineación del Esquema de Base de Datos y Migración Flyway

**Feature Branch**: `003-align-database-schema`

**Created**: 2026-11-01

**Status**: Draft

**Input**: User description: "1. El campo email en la tabla users (Crítico)... 2. Formatos de Torneo limitados en el CHECK... 3. Presupuesto máximo del mazo obligatorio en BD vs opcional en el negocio... 4. Coherencia en los estados de partida e inscripción... 5. Las migraciones Flyway en src/main/resources/db/migration/V1__init_schema.sql"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registro de Identidad con Correo Único y Torneos de Presupuesto Flexible (Priority: P1)

Como **Usuario y Administrador del Sistema**, quiero que la base de datos almacene obligatoriamente el correo electrónico de cada usuario garantizando su unicidad, y permita crear torneos tanto con presupuesto limitado (ej. torneos budget de 100 €) como sin límite de presupuesto (formatos abiertos o competitivos sin tope económico), para que el modelo de datos admita las reglas reales de la plataforma sin forzar valores artificiales.

**Why this priority**: Es un requisito bloqueante para la autenticación y la gestión de torneos; sin el campo de correo no se pueden procesar los DTOs de registro/login, y sin la flexibilidad de presupuesto se impide la creación de torneos abiertos estándar.

**Independent Test**: Puede probarse de forma independiente insertando dos usuarios con el mismo correo (comprobando que la base de datos rechaza el segundo por clave única) e insertando un torneo con presupuesto nulo y otro con presupuesto positivo de 75.00 €, verificando que ambos registros son aceptados.

**Acceptance Scenarios**:

1. **Given** un nuevo usuario con nombre de usuario, contraseña cifrada, rol y correo electrónico válido, **When** se persiste en la tabla `users`, **Then** queda registrado con su correo electrónico de forma única.
2. **Given** un correo electrónico ya registrado en el sistema, **When** se intenta insertar otro usuario con dicho correo, **Then** la base de datos rechaza la operación por violación de restricción de unicidad (`UNIQUE`).
3. **Given** un torneo oficial de *Standard* o *Commander* sin límite presupuestario, **When** el organizador crea el torneo con `max_deck_budget` nulo (`NULL`), **Then** la base de datos acepta el registro sin exigir un importe forzado.
4. **Given** un intento de crear un torneo con un valor de presupuesto negativo (ej. `-10.00 €`), **When** se intenta persistir, **Then** la base de datos rechaza la inserción mediante la restricción de validación `CHECK`.

---

### User Story 2 - Soporte de Formatos Ampliados y Normalización de Estados (Priority: P1)

Como **Jugador y Organizador de Torneos**, quiero que la base de datos reconozca los cuatro formatos competitivos contemplados (*Standard*, *Commander*, *Modern* y *Pauper*) y utilice los mismos estados de ciclo de vida definidos en la capa de aplicación para partidas (`PENDING`, `SUBMITTED`, `CONFIRMED`, `DISPUTED`) e inscripciones (`CONFIRMED`, `CANCELLED`, `DROPPED`), para evitar errores de mapeo o violaciones de restricciones al interactuar con el backend.

**Why this priority**: Evita inconsistencias de integridad semántica entre la capa de aplicación Java 21 y el motor PostgreSQL, garantizando transiciones de estado fluidas durante las partidas.

**Independent Test**: Puede probarse creando torneos en formatos *Modern* y *Pauper* en la base de datos, y actualizando el estado de partidas e inscripciones a través de los valores normalizados sin disparar violaciones de integridad relacional.

**Acceptance Scenarios**:

1. **Given** un torneo configurado en formato *Modern* o *Pauper*, **When** se persiste en las tablas `tournaments`, `leagues` o `tournament_templates`, **Then** la base de datos acepta el formato sin restricciones limitantes.
2. **Given** una partida disputada en una ronda, **When** los jugadores envían su resultado y se actualiza su estado a `SUBMITTED` o ante discrepancia a `DISPUTED`, **Then** la tabla `matches` valida el estado dentro del conjunto permitido.
3. **Given** un jugador que cancela su participación antes de iniciar el evento o abandona el torneo en curso, **When** se actualiza su inscripción a `CANCELLED` o `DROPPED`, **Then** la tabla `tournament_registrations` almacena el estado de manera conforme.

---

### User Story 3 - Inicialización Automatizada mediante Migración Flyway (Priority: P2)

Como **Ingeniero de Software y Administrador de Base de Datos**, quiero que todo el esquema relacional, tipos, vistas estadísticas y funciones analíticas residan en la carpeta oficial de migraciones del backend (`src/main/resources/db/migration/V1__init_schema.sql`), para que Spring Boot aplique automáticamente el esquema versionado al arrancar contra PostgreSQL en Supabase o Neon.

**Why this priority**: Asegura reproducibilidad en entornos de desarrollo local, pruebas de integración y despliegues productivos continuos sin intervención manual en la consola de la base de datos.

**Independent Test**: Puede probarse ejecutando la migración sobre una base de datos PostgreSQL limpia y verificando que se generan con éxito las 12 tablas, las 2 vistas de estadísticas analíticas y los 12 índices GIN/B-Tree.

**Acceptance Scenarios**:

1. **Given** un entorno Spring Boot configurado con Flyway y PostgreSQL, **When** arranca el contexto de la aplicación, **Then** Flyway localiza `V1__init_schema.sql` en `classpath:db/migration` y aplica el esquema sin errores de sintaxis.
2. **Given** la migración ejecutada, **When** se consultan las tablas del sistema y las vistas analíticas (`v_league_leaderboard`, `v_scryfall_card_stats`), **Then** todas las relaciones foráneas, restricciones de integridad y columnas JSONB están operativas.

---

### Edge Cases

- **Correos con diferencias de mayúsculas/minúsculas**: Se debe normalizar a minúsculas o utilizar unicidad insensible a mayúsculas para evitar cuentas duplicadas (ej. `user@mail.com` vs `USER@mail.com`).
- **Valores cero en el presupuesto**: Un presupuesto de `0.00 €` debe ser rechazado; si no hay límite debe ser `NULL`, y si hay límite debe ser estrictamente positivo (`> 0`).
- **Partida con descanso (BYE)**: La tabla `matches` debe admitir que `player2_id` sea nulo (`NULL`) cuando un jugador recibe descanso/BYE automático sin romper la integridad referencial.
- **Inmutabilidad del snapshot de mazo**: La columna `deck_snapshot JSONB` en `tournament_registrations` debe permanecer inmutable ante cambios posteriores en la tabla `decks`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Campo Email en Usuarios)**: La tabla `users` DEBE incluir una columna `email VARCHAR(255) NOT NULL UNIQUE` para dar soporte al registro y autenticación unificada.
- **FR-002 (Formatos Ampliados en Torneos, Ligas y Plantillas)**: Las tablas `tournaments`, `leagues` y `tournament_templates` DEBEN validar los formatos de juego mediante la restricción: `CHECK (format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER'))`.
- **FR-003 (Presupuesto Flexible y Opcional)**: Las tablas `tournaments` y `tournament_templates` DEBEN permitir que la columna `max_deck_budget NUMERIC(10, 2)` sea nula (`NULL`) para torneos sin tope de coste, validando que si contiene un valor sea estrictamente positivo: `CHECK (max_deck_budget IS NULL OR max_deck_budget > 0)`.
- **FR-004 (Normalización de Estados de Partida)**: La tabla `matches` DEBE admitir los estados alineados con el dominio: `CHECK (status IN ('PENDING', 'SUBMITTED', 'CONFIRMED', 'DISPUTED'))`.
- **FR-005 (Normalización de Estados de Inscripción)**: La tabla `tournament_registrations` DEBE admitir los estados alineados con el dominio: `CHECK (status IN ('CONFIRMED', 'CANCELLED', 'DROPPED'))`.
- **FR-006 (Ubicación Canónica de Flyway)**: El script SQL DDL definitivo DEBE ubicarse en `src/main/resources/db/migration/V1__init_schema.sql` y ser idéntico al contrato formal de arquitectura.
- **FR-007 (Vistas de Metajuego y Scryfall)**: El script de migración DEBE incluir las vistas SQL optimizadas: `v_league_leaderboard` (clasificación calculada por torneos) y `v_scryfall_card_stats` (frecuencia, colores y costes unitarios agregados).
- **FR-008 (Indexación GIN e Integridad Relacional)**: El script de migración DEBE incorporar índices B-Tree para claves foráneas y ordenaciones frecuentes, así como índices GIN para consultas sobre la columna `deck_snapshot JSONB` y atributos de Scryfall (`legalities`, `colors`).

### Key Entities *(include if feature involves data)*

- **Users**: Entidad de identidad con id único, username único, email único, password hash, rol (`PLAYER`, `ADMIN`) y marca temporal de alta.
- **Tournaments**: Evento competitivo con tienda organizadora, día/hora, aforo, premio, formato ampliado y presupuesto opcional.
- **Matches**: Emparejamiento de ronda con mesa, jugadores (con soporte de BYE), tanteador y estados normalizados (`PENDING`, `SUBMITTED`, `CONFIRMED`, `DISPUTED`).
- **Tournament Registrations**: Inscripción con snapshot JSONB inmutable del mazo, valoración económica y estados normalizados (`CONFIRMED`, `CANCELLED`, `DROPPED`).
- **Flyway Migration V1**: Archivo de migración versionado que define el estado inicial completo de la base de datos en PostgreSQL.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001 (Concordancia 100% de Estados y Enums)**: Cero divergencias entre los enums de Java 21 (`UserRole`, `TournamentFormat`, `TournamentStatus`, `RegistrationStatus`, `RoundStatus`, `MatchStatus`) y las restricciones `CHECK` en el esquema PostgreSQL.
- **SC-002 (Unicidad y Requerimiento de Correo)**: La base de datos rechaza de forma estricta registros de usuario sin correo o con correos duplicados en 100% de los casos.
- **SC-003 (Soporte Dual de Presupuesto)**: El sistema permite registrar y consultar eventos tanto con presupuesto acotado (> 0.00 €) como con presupuesto ilimitado (NULL) sin errores de inserción.
- **SC-004 (Migración Flyway Exitosa)**: El archivo `V1__init_schema.sql` crea exitosamente las 12 tablas relacionales, 2 vistas analíticas y 12 índices especializados en PostgreSQL sin fallos de ejecución.

## Assumptions

- **Compatibilidad con Supabase / Neon**: Se utiliza PostgreSQL 15+ con la extensión `pgcrypto` o la función nativa `gen_random_uuid()` disponible por defecto.
- **Evolución sin datos heredados en producción**: Al tratarse de la versión inicial (`V1`), el esquema se crea desde cero en una base de datos vacía sin necesidad de scripts de alteración incremental o scripts destructivos.
- **Convención de nomenclatura Flyway**: El archivo debe llamarse exactamente `V1__init_schema.sql` (con doble guión bajo tras la versión `V1`).


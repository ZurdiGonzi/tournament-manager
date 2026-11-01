# Research: Alineación del Esquema Relacional PostgreSQL y Migraciones Flyway

**Feature**: `003-align-database-schema` | **Fecha**: 2026-11-01

Este documento formaliza las decisiones de diseño para consolidar el esquema PostgreSQL (Supabase / Neon), asegurar concordancia 1:1 con los DTOs y modelos de Java 21, y habilitar la automatización de migraciones mediante Flyway en Spring Boot 3.x.

---

## 1. Integración de Flyway en Spring Boot 3.x

### Decisión
Integrar Flyway utilizando las dependencias oficiales de Spring Boot 3.3+:
- `org.flywaydb:flyway-core`
- `org.flywaydb:flyway-database-postgresql` (requerido a partir de Flyway 10+ para soporte dedicado de PostgreSQL).

La migración inicial se denominará **`V1__init_schema.sql`** y se ubicará en la ruta estándar de recursos: `src/main/resources/db/migration/`.

### Justificación
- Flyway gestiona el ciclo de vida del esquema de forma determinista y versionada, ejecutando la migración durante el arranque de Spring Boot antes de que Hibernate / Spring Data JPA inicialicen el contexto de persistencia.
- El Principio IV de la Constitución del proyecto establece expresamente: *"Gestiona todos los cambios de esquema mediante migraciones versionadas (Flyway en `src/main/resources/db/migration`)"*.

### Alternativas Consideradas
- **`ddl-auto: update` o `create` de Hibernate**: Descartado tajantemente. Genera esquemas no reproducibles, destruye índices GIN y no soporta vistas complejas ni columnas JSONB avanzadas.
- **Liquibase**: Descartado. Requiere archivos XML/YAML adicionales y añade complejidad innecesaria cuando SQL puro versionado es más directo para PostgreSQL.

---

## 2. Inclusión del Campo `email` en la Tabla `users`

### Decisión
Definir la columna `email VARCHAR(255) NOT NULL UNIQUE` en la tabla `users`, indexada por defecto mediante la restricción de clave única.

### Justificación
- Los DTOs de aplicación (`RegisterRequest`, `LoginRequest`, `UserSummaryResponse`, `UserProfileResponse`) exigen el correo electrónico para el flujo de autenticación JWT y comunicación.
- Una restricción `UNIQUE` en la base de datos previene colisiones por concurrencia durante registros simultáneos que la capa de aplicación por sí sola no puede garantizar de forma atómica.

---

## 3. Flexibilización del Presupuesto Máximo de Mazo (`max_deck_budget`)

### Decisión
Permitir valores nulos (`NULL`) en la columna `max_deck_budget NUMERIC(10, 2)` tanto en `tournaments` como en `tournament_templates`, aplicando la restricción:
```sql
CHECK (max_deck_budget IS NULL OR max_deck_budget > 0)
```

### Justificación
- En Magic: The Gathering, muchos torneos oficiales y torneos de tienda se juegan en formato abierto (sin límite de coste).
- Si la columna se define como `NOT NULL`, obligaría a registrar valores arbitrarios (ej. 99999 €), falseando las estadísticas y los contratos de la API REST donde `maxBudgetEur` es opcional.

---

## 4. Ampliación de Formatos Competitivos

### Decisión
Soportar los cuatro formatos estándar del dominio mediante:
```sql
CHECK (format IN ('STANDARD', 'COMMANDER', 'MODERN', 'PAUPER'))
```
en las tablas `leagues`, `tournaments` y `tournament_templates`.

### Justificación
- El enum de Java 21 `TournamentFormat` define estos cuatro formatos. Limitar la base de datos a sólo dos formatos provocaría errores inmediatos de violación de restricción relacional al programar eventos de *Modern* o *Pauper*.

---

## 5. Normalización de Estados de Partida e Inscripción

### Decisión
Alinear los valores de las restricciones `CHECK` con los enums exactos de Java 21:
- `matches.status`: `CHECK (status IN ('PENDING', 'SUBMITTED', 'CONFIRMED', 'DISPUTED'))`.
- `tournament_registrations.status`: `CHECK (status IN ('CONFIRMED', 'CANCELLED', 'DROPPED'))`.

### Justificación
- Previene discrepancias de persistencia en Hibernate (`@Enumerated(EnumType.STRING)`), donde un valor enviado por la capa de aplicación (como `SUBMITTED` o `CONFIRMED`) sería rechazado por un `CHECK` que espere nombres diferentes como `AWAITING_CONFIRMATION` o `REGISTERED`.


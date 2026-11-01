# Quickstart: Validación del Esquema PostgreSQL y Migraciones Flyway

**Feature**: `003-align-database-schema` | **Fecha**: 2026-11-01

Esta guía documenta los escenarios de validación para verificar la sintaxis del script DDL, la ejecución de la migración inicial de Flyway y la integridad de las restricciones relacionales.

---

## Prerrequisitos

- Base de datos PostgreSQL 15+ (local, Supabase o Neon).
- Script Flyway ubicado en `src/main/resources/db/migration/V1__init_schema.sql`.
- Propiedades de conexión configuradas en `application.yml`:
  ```yaml
  spring:
    datasource:
      url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/tournament_db}
      username: ${SPRING_DATASOURCE_USERNAME:postgres}
      password: ${SPRING_DATASOURCE_PASSWORD:postgres}
    flyway:
      enabled: true
      locations: classpath:db/migration
  ```

---

## Escenarios de Validación

### 1. Validación de Unicidad de Correo Electrónico (`email`)

- **Comando SQL de prueba**:
  ```sql
  -- Primer usuario (válido)
  INSERT INTO users (username, email, password_hash, role)
  VALUES ('player_uno', 'player@example.com', '$2a$12$hash1', 'PLAYER');

  -- Segundo usuario con el mismo email (debe fallar)
  INSERT INTO users (username, email, password_hash, role)
  VALUES ('player_dos', 'player@example.com', '$2a$12$hash2', 'PLAYER');
  ```
- **Resultado Esperado**: La segunda inserción arroja un error de violación de clave única:
  `ERROR: duplicate key value violates unique constraint "users_email_key"`.

---

### 2. Validación de Formatos Ampliados y Presupuesto Flexible

- **Comando SQL de prueba**:
  ```sql
  -- Torneo Modern con presupuesto libre (max_deck_budget = NULL)
  INSERT INTO tournaments (store_id, name, scheduled_at, entry_fee, prize_description, format, max_deck_budget, max_players)
  VALUES ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Modern Open Sin Presupuesto', now() + interval '5 days', 15.00, 'Caja de sobres', 'MODERN', NULL, 64);

  -- Torneo Pauper con presupuesto acotado (max_deck_budget = 50.00)
  INSERT INTO tournaments (store_id, name, scheduled_at, entry_fee, prize_description, format, max_deck_budget, max_players)
  VALUES ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Pauper 50€ Budget', now() + interval '6 days', 5.00, 'Trofeo', 'PAUPER', 50.00, 32);
  ```
- **Resultado Esperado**: Ambos torneos se insertan con éxito. Los formatos `MODERN` y `PAUPER` son reconocidos y el valor `NULL` es aceptado.

---

### 3. Validación de Estados de Partida e Inscripción Normalizados

- **Comando SQL de prueba**:
  ```sql
  -- Actualización de partida a SUBMITTED y DISPUTED
  UPDATE matches SET status = 'SUBMITTED' WHERE id = '...';
  UPDATE matches SET status = 'DISPUTED' WHERE id = '...';

  -- Actualización de inscripción a CANCELLED
  UPDATE tournament_registrations SET status = 'CANCELLED' WHERE id = '...';
  ```
- **Resultado Esperado**: Las actualizaciones se realizan sin infringir las restricciones `CHECK` de las tablas `matches` y `tournament_registrations`.


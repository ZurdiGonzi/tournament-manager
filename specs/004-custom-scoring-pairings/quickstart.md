# Quickstart: Validación de Pairings Personalizados y Puntuaciones

**Feature**: `004-custom-scoring-pairings` | **Fecha**: 2026-11-01

Esta guía documenta los escenarios de validación para verificar la inserción y consulta de torneos con configuraciones personalizadas de emparejamiento y ligas con reglas de fidelización/asistencia.

---

## Escenarios de Validación

### 1. Inserción de Torneo con Puntuación Personalizada (4 pts Victoria / 2 pts Empate)

```sql
INSERT INTO tournaments (
    store_id, name, scheduled_at, entry_fee, prize_description, format,
    pairing_system, points_per_win, points_per_draw, points_per_loss, points_per_bye
)
VALUES (
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    'Torneo Casual 4-2-0',
    now() + interval '3 days',
    5.00,
    'Sobres para todos',
    'COMMANDER',
    'SWISS',
    4, -- 4 puntos por victoria
    2, -- 2 puntos por empate
    0, -- 0 puntos por derrota
    4  -- 4 puntos por BYE
);
```
- **Resultado Esperado**: El torneo se registra con la regla de puntuación personalizada.

---

### 2. Inserción de Torneo de Eliminación Directa con Parámetros JSONB

```sql
INSERT INTO tournaments (
    store_id, name, scheduled_at, entry_fee, prize_description, format,
    pairing_system, pairing_config
)
VALUES (
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    'Top 8 Eliminación Directa',
    now() + interval '5 days',
    10.00,
    'Trofeo de Campeón',
    'MODERN',
    'SINGLE_ELIMINATION',
    '{"avoidRematches": false, "byePolicy": "HIGHEST_SEED"}'::jsonb
);
```
- **Resultado Esperado**: La base de datos valida `SINGLE_ELIMINATION` y almacena la configuración JSONB.

---

### 3. Inserción de Liga con Puntos por Asistencia y Baremo por Posición

```sql
INSERT INTO leagues (
    store_id, name, format, start_date,
    scoring_system, participation_points, best_n_tournaments, rank_points_distribution
)
VALUES (
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    'Liga de Otoño con Fidelización',
    'COMMANDER',
    now(),
    'PARTICIPATION_PLUS_RECORD',
    2, -- 2 puntos por presentarse a cada jornada
    6, -- Computan los mejores 6 torneos
    '{"1": 10, "2": 6, "3": 4, "4": 2}'::jsonb
);
```
- **Resultado Esperado**: La liga se crea con los 2 puntos por participación y el esquema de descarte.


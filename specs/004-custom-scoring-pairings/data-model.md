# Data Model: Esquema con Pairings Personalizados y Puntuaciones de Torneo y Liga

**Feature**: `004-custom-scoring-pairings` | **Fecha**: 2026-11-01

Este documento detalla las modificaciones en las entidades de base de datos (`tournaments`, `tournament_templates`, `leagues`) y la vista `v_league_leaderboard`.

---

## 1. Modificaciones en `tournaments` y `tournament_templates`

Se añaden las siguientes columnas a ambas tablas para controlar emparejamientos y puntuación:

| Columna | Tipo de Dato | Restricción / Default | Propósito |
| :--- | :--- | :--- | :--- |
| `pairing_system` | `VARCHAR(30)` | NOT NULL DEFAULT `'SWISS'` CHECK (`pairing_system IN ('SWISS', 'SINGLE_ELIMINATION', 'ROUND_ROBIN', 'CUSTOM')`) | Motor de emparejamientos utilizado. |
| `pairing_config` | `JSONB` | NULL | Parámetros específicos del emparejador (evitar revanchas, política de BYE, orden de desempates). |
| `points_per_win` | `INT` | NOT NULL DEFAULT 3 CHECK (`points_per_win >= 0`) | Puntos asignados por cada victoria de partida. |
| `points_per_draw` | `INT` | NOT NULL DEFAULT 1 CHECK (`points_per_draw >= 0`) | Puntos asignados por cada empate de partida. |
| `points_per_loss` | `INT` | NOT NULL DEFAULT 0 CHECK (`points_per_loss >= 0`) | Puntos asignados por cada derrota. |
| `points_per_bye` | `INT` | NOT NULL DEFAULT 3 CHECK (`points_per_bye >= 0`) | Puntos asignados cuando un jugador recibe descanso (BYE). |
| `scoring_rules` | `JSONB` | NULL | Modificadores adicionales de puntuación (ej. bonus por 2-0). |

---

## 2. Modificaciones en `leagues`

Se añaden las siguientes columnas a la tabla `leagues` para controlar el cómputo de la temporada:

| Columna | Tipo de Dato | Restricción / Default | Propósito |
| :--- | :--- | :--- | :--- |
| `scoring_system` | `VARCHAR(30)` | NOT NULL DEFAULT `'CUMULATIVE_MATCH_POINTS'` CHECK (`scoring_system IN ('CUMULATIVE_MATCH_POINTS', 'RANK_BASED', 'PARTICIPATION_PLUS_RECORD', 'CUSTOM')`) | Estrategia de cálculo de puntos en la liga. |
| `participation_points` | `INT` | NOT NULL DEFAULT 0 CHECK (`participation_points >= 0`) | Puntos fijos otorgados al jugador por cada torneo disputado. |
| `best_n_tournaments` | `INT` | CHECK (`best_n_tournaments IS NULL OR best_n_tournaments > 0`) | Máximo de mejores torneos a computar (descarte de peores). |
| `rank_points_distribution` | `JSONB` | NULL | Distribución de puntos otorgados por posición en cada torneo. |

---

## 3. Actualización de la Vista `v_league_leaderboard`

La vista SQL calcula la puntuación acumulada sumando los puntos de torneos más la bonificación de participación por torneo:

```sql
CREATE OR REPLACE VIEW v_league_leaderboard AS
SELECT
    l.id AS league_id,
    l.store_id,
    l.name AS league_name,
    u.id AS user_id,
    u.username,
    COALESCE(ls.total_points, 
        SUM(tr.match_points) + (COUNT(DISTINCT tr.tournament_id) * l.participation_points),
        0
    ) AS total_points,
    COUNT(DISTINCT tr.tournament_id) AS tournaments_played,
    COALESCE(SUM(tr.match_points), 0) AS match_points_sum,
    (COUNT(DISTINCT tr.tournament_id) * l.participation_points) AS participation_points_sum,
    RANK() OVER (
        PARTITION BY l.id
        ORDER BY COALESCE(ls.total_points, 
            SUM(tr.match_points) + (COUNT(DISTINCT tr.tournament_id) * l.participation_points),
            0
        ) DESC,
        COUNT(DISTINCT tr.tournament_id) DESC
    ) AS current_rank
FROM leagues l
JOIN league_standings ls ON ls.league_id = l.id
JOIN users u ON u.id = ls.user_id
LEFT JOIN tournaments t ON t.league_id = l.id
LEFT JOIN tournament_registrations tr ON tr.tournament_id = t.id AND tr.user_id = u.id
GROUP BY l.id, l.store_id, l.name, l.participation_points, u.id, u.username, ls.total_points;
```


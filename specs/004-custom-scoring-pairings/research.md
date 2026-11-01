# Research: Configuración de Pairings, Puntuación de Torneo y Ligas en PostgreSQL

**Feature**: `004-custom-scoring-pairings` | **Fecha**: 2026-11-01

Este documento evalúa las alternativas de diseño y persistencia para dotar a los dueños de tienda de control total sobre las mecánicas de emparejamiento, el sistema de puntuación de partidas y el cálculo de clasificaciones de ligas.

---

## 1. Modelado de Estrategias de Pairings

### Decisión
Combinar un tipo enumerado cerrado (`VARCHAR(30)` con `CHECK`) para el algoritmo de emparejamiento base y una columna **`JSONB`** (`pairing_config`) para almacenar parámetros extensibles específicos del algoritmo:
- Algoritmos soportados:
  - `SWISS`: Emparejamiento suizo clásico por tramos de puntos.
  - `SINGLE_ELIMINATION`: Cuadro de eliminación directa (bracket tradicional).
  - `ROUND_ROBIN`: Todos contra todos (ideal para grupos pequeños de 4-8 jugadores).
  - `CUSTOM`: Algoritmo propio o reglas específicas de la tienda.
- Estructura de `pairing_config JSONB`:
  ```json
  {
    "avoidRematches": true,
    "byePolicy": "LOWEST_RANK",
    "bracketSort": ["MATCH_POINTS", "OMW_PERCENTAGE", "GW_PERCENTAGE"]
  }
  ```

### Justificación
- Los motores estándar (`SWISS`, `SINGLE_ELIMINATION`, etc.) permiten que el backend aplique lógica de negocio compilada y optimizada en Java 21, mientras que la columna `JSONB` otorga flexibilidad ilimitada para ajustar parámetros sin requerir migraciones continuas de esquema.

---

## 2. Puntuación de Partidas en Torneo

### Decisión
Añadir columnas explícitas enteras en `tournaments` y `tournament_templates` con valores por defecto alineados con los estándares de Magic:
- `points_per_win INT NOT NULL DEFAULT 3 CHECK (points_per_win >= 0)`
- `points_per_draw INT NOT NULL DEFAULT 1 CHECK (points_per_draw >= 0)`
- `points_per_loss INT NOT NULL DEFAULT 0 CHECK (points_per_loss >= 0)`
- `points_per_bye INT NOT NULL DEFAULT 3 CHECK (points_per_bye >= 0)`
- `scoring_rules JSONB` (opcional, para modificadores adicionales como bonus por resultado 2-0 o desempates por vidas).

### Justificación
- Las columnas dedicadas permiten consultas SQL directas y eficientes para el cálculo de tablas de clasificación y vistas analíticas sin necesidad de parsear JSON en cada fila.
- Los valores por defecto garantizan que la creación básica de un torneo continúe comportándose con el sistema tradicional 3/1/0/3 si el organizador no introduce modificaciones.

---

## 3. Puntuación y Clasificación en Ligas Multijornada

### Decisión
Incorporar en la tabla `leagues`:
1. `scoring_system VARCHAR(30) NOT NULL DEFAULT 'CUMULATIVE_MATCH_POINTS'`
   - `CUMULATIVE_MATCH_POINTS`: Suma directa de puntos obtenidos en los torneos.
   - `RANK_BASED`: Puntos asignados según el puesto final en cada torneo (ej. 1º=10, 2º=6...).
   - `PARTICIPATION_PLUS_RECORD`: Puntos de torneo + puntos fijos por asistencia.
   - `CUSTOM`: Combinación libre según `rank_points_distribution`.
2. `participation_points INT NOT NULL DEFAULT 0 CHECK (participation_points >= 0)`: Puntos fijos otorgados por cada torneo jugado.
3. `best_n_tournaments INT CHECK (best_n_tournaments IS NULL OR best_n_tournaments > 0)`: Número máximo de torneos computables (descarte de las peores jornadas).
4. `rank_points_distribution JSONB`: Mapa de puntos por posición:
   ```json
   {
     "1": 10,
     "2": 6,
     "3": 4,
     "4": 2,
     "default": 0
   }
   ```

### Justificación
- Permite a las tiendas premiar la fidelidad y asistencia regular de los jugadores a la vez que incentiva el alto rendimiento en el podio.

---

## 4. Adaptación de la Vista SQL `v_league_leaderboard`

### Decisión
Actualizar `v_league_leaderboard` para que compute dinámicamente los puntos totales como:
```sql
COALESCE(ls.total_points, 
    SUM(tr.match_points) + (COUNT(DISTINCT tr.tournament_id) * l.participation_points),
    0
) AS total_points
```
y desglose las métricas de:
- `tournament_match_points`
- `participation_points_earned`
- `tournaments_played`

### Justificación
- Garantiza que cualquier cambio en las reglas de asistencia o puntos de torneo se refleje de manera transparente en la tabla general de la liga sin procesos nocturnos de recálculo batch.


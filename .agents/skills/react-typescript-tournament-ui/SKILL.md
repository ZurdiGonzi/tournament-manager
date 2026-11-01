---
name: react-typescript-tournament-ui
description: >-
  Use this skill when building or updating the React + TypeScript frontend,
  including strict types for match states, real-time round/bracket visualization,
  deck builder/submission forms, and role-protected admin dashboards.
---

# React + TypeScript Tournament Frontend Skill

Guía para desarrollar el frontend de **Tournament Manager** con control estricto de tipos.

## 1. Modelado Estricto de Estados (Discriminated Unions)

Activa `"strict": true` y `"noUncheckedIndexedAccess": true` en `tsconfig.json`.
Modela los estados de partidas y torneos mediante tipos discriminados que reflejen exactamente la máquina de estados del backend:

```typescript
export type MatchStatus =
  | { status: 'PENDING'; tableNumber: number }
  | { status: 'IN_PROGRESS'; tableNumber: number; startedAt: string; endsAt: string }
  | { status: 'AWAITING_CONFIRMATION'; reportedByPlayerId: string; p1Wins: number; p2Wins: number; draws: number }
  | { status: 'DISPUTED'; reason: string }
  | { status: 'CONFIRMED'; p1Wins: number; p2Wins: number; draws: number; winnerId: string | null };
```

## 2. Arquitectura de Rutas y Roles

Divide las vistas según los tres niveles de acceso del backend:
- **Rutas Públicas (`/tournaments`, `/tournaments/:id/standings`, `/tournaments/:id/pairings`)**:
  - Visualización de rondas en tiempo real, temporizador de ronda activa, tabla de posiciones (con puntos y porcentajes de desempate OMW%/GW%) y visor de mazos públicos (una vez finalizado el torneo si es *open decklist*).
- **Rutas de Jugador (`/my-matches`, `/tournaments/:id/register`)**:
  - Formulario tipado de envío de mazo (genera la estructura `DeckSnapshot` validada antes de enviar), botón de Check-in y panel de reporte/confirmación rápida de resultados de la ronda actual (`2-0`, `2-1`, `1-1`, etc.).
- **Rutas de Admin (`/admin/tournaments`, `/admin/templates`, `/admin/tournaments/:id/control`)**:
  - Guardián de ruta `<RequireRole role="ADMIN">` que verifica los claims del JWT.
  - Panel de control de ronda (generar siguiente ronda suiza, resolver partidas en estado `DISPUTED`, configurar plantillas cron para `@Scheduled`).

## 3. Sincronización de Rondas en Tiempo Real

- Utiliza **TanStack Query (React Query)** con `refetchInterval` adaptativo (o Server-Sent Events / WebSockets si está habilitado en el backend) en la vista de ronda activa para actualizar emparejamientos y resultados confirmados sin recargar la página.
- Al reportar un resultado o avanzar una ronda desde el panel de Admin, invalida inmediatamente las queries `['tournament', id, 'pairings']` y `['tournament', id, 'standings']`.

## 4. Validación del Paso
- Ejecuta `npm run typecheck` (o `tsc --noEmit`) y asegúrate de que haya **0 errores de tipos** antes de dar por completada cualquier tarea de interfaz.


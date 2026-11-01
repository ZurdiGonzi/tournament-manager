<!--
Sync Impact Report
- Version change: template -> 1.0.0
- Modified principles:
  - [PRINCIPLE_1_NAME] -> I. Núcleo de Dominio Puro y Tipado en Java 21 (Backend)
  - [PRINCIPLE_2_NAME] -> II. Seguridad Stateless y Segregación por Roles con JWT
  - [PRINCIPLE_3_NAME] -> III. Control Estricto de Tipos en Frontend (React + TypeScript)
  - [PRINCIPLE_4_NAME] -> IV. Integridad Relacional en PostgreSQL y Snapshots Inmutables en JSONB
  - [PRINCIPLE_5_NAME] -> V. Planificación Nativa e Idempotente de Torneos Recurrentes (@Scheduled)
- Added sections:
  - Stack Tecnológico y Arquitectura de Paquetes
  - Flujo de Desarrollo y Puertas de Calidad (Quality Gates)
- Removed sections: None
- Templates & guidance alignment:
  - ✅ .specify/templates/plan-template.md (aligned via Constitution Check gates)
  - ✅ .specify/templates/spec-template.md (aligned with user stories & requirements)
  - ✅ .specify/templates/tasks-template.md (aligned with Clean Architecture phase ordering)
  - ✅ AGENTS.md (synchronized runtime agent guidance)
- Follow-up TODOs: None
-->
# Tournament Manager Constitution

## Core Principles

### I. Núcleo de Dominio Puro y Tipado en Java 21 (Backend)
El backend DEBE implementarse en **Java 21** y **Spring Boot 3.x** siguiendo una separación
estricta de Arquitectura Limpia / Hexagonal bajo `com.tournament.app`.
- El paquete `domain/` (`model`, `service`, `repository`) DEBE permanecer 100% libre de
  dependencias de frameworks (PROHIBIDO usar anotaciones de Spring, JPA/Hibernate o Jackson en
  `domain/`).
- La lógica crítica del negocio —reglas de torneos, algoritmo de emparejamiento suizo
  (`SwissPairingEngine`), cálculo de desempates, validación de costes/presupuesto de mazos
  (`BudgetValidator`) y transiciones de estado de partidas— DEBE residir en el núcleo de dominio y
  ser verificable mediante pruebas unitarias puras sin levantar el contexto de Spring.
- Todos los DTOs de entrada/salida (`application/dto`) y objetos de valor inmutables DEBEN
  definirse como `record` de Java 21, y los estados cerrados DEBEN modelarse con `sealed interfaces`
  o `enum` con transiciones explícitas.

**Justificación**: Aísla las reglas complejas de torneos y emparejamientos frente a cambios de
infraestructura y garantiza máxima velocidad y determinismo en los tests de dominio.

### II. Seguridad Stateless y Segregación por Roles con JWT
La autenticación y autorización DEBEN implementarse con **Spring Security 6 + JWT** bajo una
política estrictamente sin estado (`SessionCreationPolicy.STATELESS`).
- PROHIBIDO el uso de sesiones HTTP (`HttpSession`) o estado de autenticación en memoria del
  servidor.
- Los endpoints REST DEBEN segregarse y protegerse por prefijo y rol:
  - `/api/v1/public/**`: Acceso público (`permitAll()`) para consulta de torneos, emparejamientos
    y clasificaciones.
  - `/api/v1/player/**`: Restringido a jugadores autenticados (`hasAnyRole('PLAYER', 'ADMIN')`)
    para inscripción, envío de mazos y reporte/confirmación de resultados propios.
  - `/api/v1/admin/**`: Restringido exclusivamente a administradores (`hasRole('ADMIN')`) para
    gestión de torneos, resolución de disputas, control de rondas y configuración de plantillas.
- Todo caso de uso de jugador DEBE validar la propiedad del recurso frente al usuario autenticado
  en el token JWT para impedir vulnerabilidades IDOR.

**Justificación**: Garantiza escalabilidad horizontal sin afinidad de sesión y protege la
integridad competitiva del torneo frente a manipulaciones no autorizadas.

### III. Control Estricto de Tipos en Frontend (React + TypeScript)
El frontend DEBE desarrollarse con **React + TypeScript** con el modo estricto habilitado
(`"strict": true` y `"noUncheckedIndexedAccess": true` en `tsconfig.json`).
- PROHIBIDO el uso del tipo `any` explícito o implícito.
- Los estados de torneos, rondas y partidas en tiempo real DEBEN modelarse mediante uniones
  discriminadas (*discriminated unions*) que reflejen fielmente los contratos del backend,
  impidiendo la representación de estados imposibles en la interfaz.
- Los paneles de administración y las vistas de jugador DEBEN validar el rol y estado del token JWT
  antes de renderizar acciones mutables, manteniendo sincronización reactiva de rondas y brackets.

**Justificación**: Elimina errores en tiempo de ejecución durante el seguimiento de rondas en vivo
y asegura consistencia total entre los contratos de la API REST y la interfaz de usuario.

### IV. Integridad Relacional en PostgreSQL y Snapshots Inmutables en JSONB
La capa de persistencia DEBE utilizar **PostgreSQL (Supabase / Neon)** combinando integridad
relacional para la estructura competitiva y almacenamiento documental **`JSONB`** para los mazos.
- Las entidades de torneos, inscripciones, rondas y emparejamientos DEBEN mantener claves foráneas,
  restricciones de unicidad (ej. un jugador no puede inscribirse dos veces al mismo torneo) y
  migraciones versionadas mediante Flyway.
- En el momento de la inscripción/cierre de registro, el mazo validado (incluyendo datos obtenidos
  vía adaptadores externos como `MoxfieldClient`) DEBE persistirse como un **snapshot inmutable**
  en una columna `JSONB` (`@JdbcTypeCode(SqlTypes.JSON)`).
- PROHIBIDO enlazar inscripciones de torneos en curso o finalizados a referencias mutables de mazos
  que puedan ser editadas posteriormente por el jugador.

**Justificación**: Aprovecha la capa gratuita y potencia relacional de PostgreSQL en Supabase/Neon
mientras preserva la auditoría histórica exacta del mazo con el que compitió cada jugador.

### V. Planificación Nativa e Idempotente de Torneos Recurrentes (@Scheduled)
La automatización de torneos periódicos DEBE ejecutarse de forma nativa mediante **Spring Task
Scheduling (`@Scheduled`)** dentro de `infrastructure/scheduler/TournamentAutoScheduler`, sin
introducir gestores de colas ni dependencias externas pesadas (como Quartz o brokers adicionales).
- Toda ejecución programada (`ScheduleTournamentUseCase`) DEBE ser estrictamente **idempotente**:
  antes de crear una instancia de torneo recurrente, el sistema DEBE verificar mediante clave única
  (plantilla + fecha/ventana programada) que el torneo no haya sido generado previamente.
- Las expresiones cron y parámetros de planificación DEBEN estar externalizados en la configuración
  (`application.yml` / variables de entorno) y ejecutarse sobre un pool de hilos controlado.

**Justificación**: Reduce la complejidad operativa y el consumo de recursos en entornos cloud de
capa gratuita manteniendo fiabilidad total en la creación automática de torneos recurrentes.

## Stack Tecnológico y Arquitectura de Paquetes

| Capa | Tecnología Obligatoria | Propósito Principal |
|------|------------------------|---------------------|
| **Backend** | Java 21 + Spring Boot 3.x | Lógica de dominio (torneos, suizo, validación de presupuesto), API REST y casos de uso. |
| **Seguridad** | Spring Security 6 + JWT | Autenticación stateless y control de acceso para rutas públicas, de jugador y de admin. |
| **Frontend** | React + TypeScript (`strict`) | Estados tipados de partidas, paneles de admin y visualización de rondas en tiempo real. |
| **Base de Datos** | PostgreSQL (Supabase / Neon) | Modelo relacional para torneos/emparejamientos y columnas `JSONB` para snapshots de mazos. |
| **Scheduler** | Spring `@Scheduled` | Creación periódica e idempotente de torneos recurrentes sin dependencias externas pesadas. |

La estructura de paquetes del backend en `src/main/java/com/tournament/app/` DEBE respetar las
siguientes cuatro capas:
1. `domain/` (`model`, `service`, `repository`): Modelos puros (`Deck`, `Match`, `Tournament`,
   `User`), motores de negocio (`SwissPairingEngine`, `BudgetValidator`) e interfaces de
   repositorio (`TournamentRepository`, `RegistrationRepository`).
2. `application/` (`usecase`, `dto`): Orquestación de casos de uso (`RegisterPlayerUseCase`,
   `GeneratePairingsUseCase`, `ReportMatchResultUseCase`, `ScheduleTournamentUseCase`) y DTOs.
3. `infrastructure/` (`adapter/moxfield`, `adapter/persistence/jpa`,
   `adapter/persistence/repository`, `security`, `scheduler`): Adaptadores HTTP/JPA, seguridad JWT
   y scheduler automático.
4. `presentation/`: Controladores REST (`AuthController`, `PlayerTournamentController`,
   `AdminTournamentController`, `AnalyticsController`).

## Flujo de Desarrollo y Puertas de Calidad (Quality Gates)

Todo desarrollo de funcionalidades mediante el flujo Spec Kit (`/speckit-specify` → `/speckit-plan`
→ `/speckit-tasks` → `/speckit-implement`) DEBE superar las siguientes puertas de calidad antes de
darse por completado:

1. **Pureza Arquitectónica**: Verificación de que ningún archivo dentro de `domain/` importe
   paquetes `org.springframework.*` ni `jakarta.persistence.*`.
2. **Pruebas del Motor de Dominio**: `SwissPairingEngine` y `BudgetValidator` DEBEN contar con
   tests unitarios que cubran casos borde (número impar de jugadores/BYE, prohibición de revanchas,
   desempates y límites de presupuesto del mazo).
3. **Verificación de Seguridad por Rol**: Cada nuevo endpoint en `presentation/` DEBE declarar y
   probar explícitamente su nivel de acceso (`PUBLIC`, `PLAYER` o `ADMIN`).
4. **Compilación Limpia y Tipado Estricto**: El backend DEBE compilar y pasar sus tests en Java 21,
   y el frontend DEBE ejecutar la comprobación de tipos de TypeScript (`tsc --noEmit`) con cero
   errores.

## Governance

Esta Constitución rige todas las decisiones técnicas, especificaciones (`spec.md`), planes de
implementación (`plan.md`) y listas de tareas (`tasks.md`) del proyecto **Tournament Manager**,
prevaleciendo sobre cualquier práctica ad-hoc.

- **Procedimiento de Enmienda**: Cualquier cambio al stack tecnológico, a los cinco principios
  fundamentales o a la estructura de capas requiere actualizar este documento mediante
  `/speckit-constitution`, documentar la justificación y sincronizar `AGENTS.md`.
- **Política de Versionado**: Se aplica versionado semántico (`MAJOR.MINOR.PATCH`):
  - `MAJOR`: Eliminación o redefinición incompatible de un principio arquitectónico o cambio de
    tecnología base.
  - `MINOR`: Incorporación de un nuevo principio, restricción arquitectónica o puerta de calidad.
  - `PATCH`: Aclaraciones de redacción, ejemplos o refinamientos sin cambios semánticos.
- **Revisión de Cumplimiento**: La sección *Constitution Check* de cada `plan.md` generado por
  `/speckit-plan` DEBE validar explícitamente los cinco principios de este documento. Para la guía
  operativa de los agentes durante el desarrollo diario, consúltese `AGENTS.md`.

**Version**: 1.0.0 | **Ratified**: 2026-11-01 | **Last Amended**: 2026-11-01

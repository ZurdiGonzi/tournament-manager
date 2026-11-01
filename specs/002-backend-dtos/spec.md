# Feature Specification: Contratos y DTOs del Backend para Torneos, Mazos, Usuarios y Analíticas

**Feature Branch**: `002-backend-dtos`

**Created**: 2026-11-01

**Status**: Draft

**Input**: User description: "Quiero que hagas el spec para declarar los dtos para el backend"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Autenticación, Gestión de Identidad y Tiendas (Priority: P1)

Como **Cliente de la API** (Frontend web o móvil), quiero intercambiar estructuras de datos predecibles y estrictamente validadas para el registro de usuarios, autenticación y administración de tiendas, de modo que el sistema procese credenciales de forma segura y devuelva perfiles diferenciando claramente entre jugadores y administradores/dueños.

**Why this priority**: Es la puerta de enlace inicial; sin contratos formales de autenticación, sesión y gestión de tiendas, ninguna otra operación protegida del backend puede ser consumida.

**Independent Test**: Puede probarse enviando un payload de registro de usuario con datos válidos, autenticándose posteriormente mediante el payload de login para recibir el token y perfil del usuario, y dando de alta una tienda con su propietario asignado.

**Acceptance Scenarios**:

1. **Given** un payload de registro de usuario con nombre de usuario, correo electrónico y contraseña que cumple las restricciones de complejidad, **When** el cliente envía la petición de registro, **Then** el sistema responde con el identificador del usuario creado, nombre, rol asignado y confirmación sin exponer contraseñas en texto claro.
2. **Given** credenciales de acceso válidas (nombre de usuario/correo y contraseña), **When** se envía la solicitud de autenticación, **Then** el sistema responde con una estructura que contiene el token de acceso JWT, tipo de token (Bearer), tiempo de expiración y el resumen del perfil del usuario (id, username, roles).
3. **Given** un usuario autenticado con rol de dueño, **When** envía la solicitud de creación de tienda con nombre y ubicación/contacto, **Then** el sistema valida los campos requeridos y responde con la estructura completa de la tienda creada, incluyendo identificador único, nombre y datos del propietario.
4. **Given** un payload de petición con campos obligatorios vacíos o formatos incorrectos (ej. correo inválido), **When** el backend procesa la solicitud, **Then** retorna una respuesta estándar de error estructurado especificando cada campo erróneo y la razón de rechazo.

---

### User Story 2 - Gestión de Mazos Moxfield, Creación de Torneos e Inscripciones Validadas (Priority: P1)

Como **Jugador o Administrador de Torneos**, quiero enviar y recibir contratos de datos consistentes para registrar mazos vinculados a Moxfield, configurar torneos con reglas de formato/presupuesto y solicitar inscripciones que almacenen una foto inmutable (snapshot) del mazo, para garantizar que las reglas se apliquen de manera uniforme y transparente.

**Why this priority**: Constituye el núcleo operativo del gestor de torneos; vincula las restricciones del organizador con las listas de cartas del jugador.

**Independent Test**: Puede probarse creando un torneo mediante su contrato de definición (con formato Standard y presupuesto máximo de 100 €), registrando un mazo con URL de Moxfield y enviando la solicitud de inscripción, recibiendo el detalle de inscripción con el snapshot de cartas confirmado o el rechazo detallado por presupuesto excedido.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado como dueño de tienda, **When** envía la solicitud de creación de torneo con nombre, fecha/hora programada, precio de entrada, premio, aforo, formato de juego (*Standard* o *Commander*) y tope de presupuesto opcional, **Then** el sistema valida y responde con el detalle completo del torneo, su estado inicial (Abierto para registro) y sus restricciones normalizadas.
2. **Given** un jugador registrado, **When** envía la solicitud de guardado de mazo con nombre de mazo y URL válida de Moxfield, **Then** el sistema responde con el mazo registrado, su identificador y la información preliminar obtenida (formato declarado y número de cartas).
3. **Given** un torneo abierto con límite presupuestario de 150 €, **When** un jugador envía la solicitud de inscripción seleccionando dicho torneo y uno de sus mazos registrados cuyo coste evaluado es de 130 €, **Then** el sistema responde con la confirmación de inscripción, estado `CONFIRMED` y la estructura inmutable del snapshot de cartas (nombre de carta, cantidad, coste de maná y precio unitario capturado).
4. **Given** un torneo con restricción presupuestaria de 50 €, **When** un jugador intenta inscribirse con un mazo evaluado en 65 €, **Then** el sistema responde con un error de validación de reglas de negocio detallando el importe total, el límite permitido y la lista de cartas que provocan el exceso de presupuesto.

---

### User Story 3 - Visualización de Rondas, Reporte de Partidas y Clasificación (Priority: P2)

Como **Jugador o Administrador de Torneos**, quiero consultar los emparejamientos de cada ronda, reportar resultados de partidas y visualizar la tabla de clasificación actualizada en tiempo real mediante contratos de datos claros, para seguir el progreso del torneo sin ambigüedades.

**Why this priority**: Facilita el desarrollo competitivo de las rondas del torneo una vez cerrado el registro de participantes.

**Independent Test**: Puede probarse consultando la estructura de una ronda generada (emparejamientos de mesa, jugadores y condición de BYE), enviando el reporte de resultado de una partida (victorias, derrotas, empates) y consultando la tabla de posiciones calculada.

**Acceptance Scenarios**:

1. **Given** una ronda activa en un torneo, **When** un cliente solicita la vista de la ronda, **Then** el sistema devuelve una colección de emparejamientos con número de mesa, datos de los dos jugadores enfrentados (o indicación de descanso/BYE) y estado de la partida (`PENDING`, `SUBMITTED`, `CONFIRMED`, `DISPUTED`).
2. **Given** un jugador participante en una partida pendiente, **When** envía el reporte de resultado indicando partidas ganadas, perdidas y empatadas (ej. 2 victorias, 1 derrota), **Then** el sistema responde con el estado de la partida actualizado a `SUBMITTED` a la espera de confirmación del oponente.
3. **Given** que ambos jugadores reportan resultados incompatibles entre sí, **When** se detecta la discrepancia, **Then** la partida responde con estado `DISPUTED` y queda disponible en la vista de administración para su resolución manual por el dueño.
4. **Given** partidas completadas en una ronda, **When** se solicita la clasificación del torneo, **Then** el sistema responde con la lista ordenada de jugadores, puntos de torneo acumulados y coeficientes de desempate oficiales (Buchholz / OMW%).

---

### User Story 4 - Ligas y Métricas Estadísticas Enriquecidas con Scryfall (Priority: P3)

Como **Usuario de la Tienda**, quiero consultar la clasificación general de ligas y contratos analíticos de cartas basados en Scryfall (cartas más jugadas, distribución de colores, curvas de coste y coste medio de mazos), para analizar las tendencias del metajuego en los eventos organizados.

**Why this priority**: Aporta valor añadido analítico y de fidelización sobre los torneos ya disputados sin bloquear la operativa transaccional diaria.

**Independent Test**: Puede probarse consultando los DTOs de resumen de una liga con sus torneos puntuables y solicitando las métricas agregadas de un torneo o tienda, recibiendo los resúmenes estadísticos normalizados.

**Acceptance Scenarios**:

1. **Given** una liga activa con varios torneos disputados, **When** se solicita el detalle de la liga, **Then** el sistema devuelve los metadatos de la liga, la lista de torneos que computan y la tabla clasificada de participantes con puntos acumulados.
2. **Given** torneos completados con snapshots de mazos, **When** se consulta la analítica de la tienda o de un torneo específico, **Then** el sistema devuelve el desglose estructurado: porcentaje de distribución por colores e identidad, coste medio en euros por mazo, y el ranking de las cartas más utilizadas con su información de Scryfall asociada (nombre, coste de maná, tipo y precio).

---

### Edge Cases

- **Payload con campos desconocidos o nulos**: El sistema debe rechazar o ignorar propiedades redundantes sin alterar los atributos declarados ni provocar fallos internos, devolviendo detalles de validación claros.
- **Formato de URL de Moxfield erróneo**: Cuando el enlace proporcionado no cumpla el patrón estándar (`https://www.moxfield.com/decks/...`), el contrato de validación debe responder indicando que la URL no pertenece a un dominio o formato de mazo soportado.
- **Emparejamiento impar (BYE)**: El contrato de emparejamiento debe representar de forma explícita cuándo un jugador recibe un descanso/BYE (oponente nulo o ausente y victoria automática computada), impidiendo estados inconsistentes en la interfaz cliente.
- **Representación unificada de errores**: Todo error de validación, conflicto de reglas o falta de autorización debe emitirse bajo el estándar RFC 7807 (`ProblemDetail`) con tipo de error, título, código HTTP, detalle descriptivo y lista de violaciones de validación por campo.
- **Normalización de fechas y zonas horarias**: Todos los campos temporales (`createdAt`, `scheduledAt`, `closedAt`) deben transmitirse bajo el estándar ISO-8601 con zona horaria UTC (`YYYY-MM-DD'T'HH:mm:ss'Z'`).

## Requirements *(mandatory)*

### Functional Requirements

#### 1. Contratos de Autenticación y Usuarios
- **FR-001 (Registro de Usuario)**: El sistema DEBE exponer un contrato de solicitud (`RegisterRequest`) que requiera nombre de usuario (3-30 caracteres, alfanumérico), correo electrónico válido y contraseña (mínimo 8 caracteres), y un contrato de respuesta (`UserSummaryResponse`) con id, username, email, roles y fecha de creación.
- **FR-002 (Autenticación e Identidad)**: El sistema DEBE exponer un contrato de solicitud (`LoginRequest`) con identificador de acceso (username o email) y contraseña, y un contrato de respuesta (`AuthResponse`) que incluya el token JWT de acceso, tipo de token, tiempo de expiración en segundos y el perfil básico del usuario autenticado.
- **FR-003 (Perfil de Usuario)**: El sistema DEBE exponer un contrato (`UserProfileResponse`) que incluya identificador, nombre de usuario, rol, tiendas a las que pertenece, recuento de mazos registrados y resumen de ligas en las que participa.

#### 2. Contratos de Tiendas y Ligas
- **FR-004 (Gestión de Tienda)**: El sistema DEBE definir contratos para creación de tienda (`CreateStoreRequest`: nombre, dirección/descripción) y consulta de tienda (`StoreDetailResponse`: id, nombre, propietario, fecha de creación, total de torneos y total de miembros).
- **FR-005 (Gestión de Liga)**: El sistema DEBE definir contratos para consulta de liga (`LeagueDetailResponse`: id, tienda, nombre, fecha de inicio/fin, estado) y su clasificación (`LeagueStandingEntryResponse`: posición, jugador, torneos disputados, puntos totales).

#### 3. Contratos de Mazos e Integración con Moxfield
- **FR-006 (Alta y Consulta de Mazo)**: El sistema DEBE definir un contrato de solicitud (`RegisterDeckRequest`) que contenga el nombre del mazo y la URL obligatoria a Moxfield, y un contrato de respuesta (`DeckSummaryResponse`) con id, nombre, url original, identificador público de Moxfield y fecha de registro.
- **FR-007 (Snapshot Inmutable de Mazo)**: El sistema DEBE definir el contrato de instantánea de mazo (`DeckSnapshotDto`) que contenga el identificador de mazo, formato validado, coste económico total calculado, fecha del snapshot y una lista de cartas (`DeckCardDto`: identificador Scryfall/Oracle, nombre de la carta, cantidad, tipo de carta, coste de maná, colores y precio individual en el momento del registro).

#### 4. Contratos de Torneos e Inscripción
- **FR-008 (Definición de Torneo)**: El sistema DEBE exponer un contrato (`CreateTournamentRequest`) que valide: nombre del torneo, tienda organizadora, fecha y hora de inicio (ISO-8601 a futuro), precio de inscripción (mayor o igual a cero), premio (texto descriptivo o dotación), capacidad máxima de jugadores y restricciones (`TournamentRestrictionsDto`: formato de juego obligatorio [*Standard*, *Commander*, etc.] y límite de presupuesto máximo opcional).
- **FR-009 (Detalle y Listado de Torneos)**: El sistema DEBE exponer contratos de respuesta (`TournamentSummaryResponse` y `TournamentDetailResponse`) que incluyan identificador, nombre, estado del torneo (`REGISTRATION_OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), fecha/hora, precio, premio, aforo máximo, total de inscritos y restricciones configuradas.
- **FR-010 (Inscripción a Torneo)**: El sistema DEBE exponer un contrato de solicitud (`TournamentRegistrationRequest`) que incluya el identificador del torneo y el identificador del mazo del jugador, y un contrato de respuesta (`TournamentRegistrationResponse`) con id de inscripción, jugador, torneo, estado (`CONFIRMED`), fecha de inscripción y el `DeckSnapshotDto` inmutable generado.
- **FR-011 (Respuesta de Error de Validación de Mazo)**: Cuando un mazo incumpla formato o presupuesto, el sistema DEBE retornar una estructura específica (`DeckValidationFailureResponse`) que señale: motivo del rechazo, límite permitido, valor evaluado del mazo y listado de cartas causantes o no legales.

#### 5. Contratos de Rondas, Partidas y Clasificación
- **FR-012 (Ronda y Emparejamientos)**: El sistema DEBE exponer un contrato de consulta de ronda (`RoundDetailResponse`) que contenga número de ronda, estado de la ronda (`ACTIVE`, `FINISHED`) y la lista de emparejamientos (`MatchPairingResponse`: id de partida, número de mesa, jugador 1, jugador 2 [o indicador de BYE], estado de la partida y resultado registrado si existe).
- **FR-013 (Reporte y Confirmación de Resultado)**: El sistema DEBE definir contratos para reporte de resultado (`ReportMatchResultRequest`: id de partida, partidas ganadas por jugador 1, partidas ganadas por jugador 2, empates) y respuesta de estado de partida (`MatchResultResponse`: id, estado [`PENDING`, `SUBMITTED`, `CONFIRMED`, `DISPUTED`], reportado por, resultado validado).
- **FR-014 (Clasificación de Torneo)**: El sistema DEBE exponer un contrato de tabla de posiciones (`TournamentStandingsResponse`) compuesto por una lista ordenada de participantes (`StandingEntryResponse`: posición, identificador y nombre del jugador, puntos de torneo acumulados, partidas ganadas/perdidas/empatadas y coeficientes de desempate OMW% / GW%).

#### 6. Contratos de Analíticas y Respuestas de Error
- **FR-015 (Estadísticas de Metajuego Scryfall)**: El sistema DEBE definir contratos para analíticas de tienda y torneo (`MetagameAnalyticsResponse`) que incluyan: coste medio por mazo, distribución porcentual por color e identidad (`ColorBreakdownDto`), y el top de cartas más jugadas (`CardUsageStatDto`: nombre, id Scryfall, coste de maná, frecuencia de uso y porcentaje de presencia en mazos).
- **FR-016 (Formato Estándar de Errores RFC 7807)**: El sistema DEBE formalizar el contrato de respuesta de errores (`ApiErrorResponse` / `ProblemDetail`) con: tipo URI, título, código HTTP, detalle del error, marca de tiempo y lista opcional de errores de validación de campo (`FieldErrorDto`: campo, valor rechazado, mensaje de validación).

### Key Entities *(include if feature involves data)*

- **Auth & Identity DTOs**: Estructuras de intercambio para registro, autenticación, revocación y entrega de tokens JWT y roles.
- **Store & League DTOs**: Contratos de datos para administración de tiendas físicas/comunidades y ligas multijornada.
- **Deck & Card Snapshot DTOs**: Representación estructurada del mazo guardado por URL y del conjunto de cartas con metadatos de Scryfall congelados en el tiempo.
- **Tournament & Restrictions DTOs**: Contratos de configuración de torneos, parámetros de aforo, precios, premios y restricciones competitivas (formato y presupuesto).
- **Match & Round DTOs**: Contratos de emparejamientos por ronda (sistema suizo), reporte bilateral de partidas y resolución de conflictos.
- **Standings & Analytics DTOs**: Contratos para tablas de clasificación (torneo y liga) y tableros analíticos de cartas y colores.
- **Problem Detail DTO**: Estructura canónica para el tratamiento y reporte uniforme de errores y fallos de validación en la API.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001 (Completitud de Contratos)**: El 100% de los casos de uso del backend (autenticación, gestión de tienda, mazos/moxfield, torneos, inscripciones con snapshot, rondas/partidas, ligas y analíticas) cuentan con sus respectivos DTOs de entrada y salida especificados.
- **SC-002 (Inmutabilidad de Snapshots)**: Toda respuesta de inscripción a torneo garantiza la presencia de la estructura completa del snapshot del mazo (cartas, cantidades, precios unitarios de Scryfall y coste total), permitiendo reconstruir el mazo histórico de forma independiente sin consultar fuentes externas.
- **SC-003 (Validación Temprana y Claridad de Errores)**: El 100% de las peticiones que contengan datos no válidos (presupuesto excedido, formatos de URL incorrectos, resultados incoherentes) reciben una respuesta estructurada RFC 7807 con indicación explícita del campo y motivo en menos de 1 ciclo de ida y vuelta.
- **SC-004 (Independencia del Cliente)**: Los contratos DTO están especificados de forma agnóstica para permitir su consumo idéntico tanto desde la interfaz web (React) como desde cualquier otro cliente API o herramienta de integración.
- **SC-005 (Consistencia de Estados)**: Todos los estados de ciclo de vida (torneo, ronda, partida, inscripción) están delimitados en conjuntos finitos y coherentes en los DTOs, imposibilitando la emisión de estados inválidos hacia los clientes.

## Assumptions

- **Alineación con la Constitución**: En la fase de implementación, todos los DTOs se materializarán en Java 21 como `record` inmutables dentro del paquete `application/dto` de acuerdo con el Principio I de la Constitución del proyecto.
- **Moxfield como fuente de mazos**: Se asume que el jugador proporciona una URL pública de Moxfield, y que el backend procesa e importa su lista de cartas para validarla contra los datos de Scryfall.
- **Moneda de presupuesto**: Los precios de cartas y restricciones presupuestarias se expresan en euros (€) con dos decimales mediante valores numéricos decimales.
- **Puntuación MTG tradicional**: La victoria otorga 3 puntos de torneo, el empate 1 punto y la derrota 0 puntos, sobre los cuales se calculan las clasificaciones y coeficientes de desempate.
- **Protocolo de reporte de partidas**: Se contempla el modelo habitual de reporte donde un jugador envía el resultado y el contrincante o el administrador confirman; en caso de desacuerdo, la partida pasa a estado disputado.


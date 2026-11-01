# Feature Specification: Acceso a Datos de Torneos, Ligas y Estadísticas de Tienda e Integración con Scryfall

**Feature Branch**: `002-store-tournament-backend-scryfall`

**Created**: 2026-11-01

**Status**: Draft

**Input**: User description: "teniendo en cuenta esta base de datos final (users, stores, store_members, decks, leagues, league_standings, tournament_templates, tournaments, scryfall_cards, tournament_registrations, registration_deck_cards, rounds, matches y las vistas v_league_leaderboard y v_scryfall_card_stats) quiero que hagas el backend necesario para extraer de la base de datos cada elemento. Tambien quiero que hagas lo necesario para comunicarte con scryfall"

## Clarifications

### Session 2026-11-01

- Q: ¿Cómo debe estructurar la API las respuestas de lectura para alimentar las pantallas de React: mediante recursos individuales separados o incluyendo también vistas agregadas por pantalla? → A: Enfoque híbrido: consultas para cada entidad individual + consultas agregadas listas para alimentar las vistas de un futuro cliente React.
- Q: ¿Cuál es el límite de alcance respecto al frontend en esta funcionalidad? → A: Alcance exclusivamente Backend: no se implementa código de frontend; se construyen únicamente la capa de extracción de base de datos y la comunicación con Scryfall.
- Q: ¿Cómo debe activarse en el backend la comunicación con Scryfall para poblar y actualizar la tabla `scryfall_cards`? → A: Internamente en el backend en el momento de registrar/inscribir un mazo, sincronizando sus cartas contra Scryfall y persistiendo los datos en `scryfall_cards`.
- Q: ¿Deben implementarse los DTOs (`application/dto`) en esta rama? → A: No; los DTOs se están desarrollando en la rama `002-backend-dtos`, por lo que quedan fuera del alcance de esta rama para evitar conflictos de integración.
- Q: ¿Cómo se alinea esta rama con la especificación `002-backend-dtos`? → A: Esta rama implementa el dominio (`domain/model`, `domain/repository`, `domain/service`), la persistencia JPA de las 12 tablas y 2 vistas (`infrastructure/adapter/persistence/`) y el cliente de Scryfall (`infrastructure/adapter/scryfall/`), exponiendo todos los datos y cálculos agregados que requieren los contratos definidos en `002-backend-dtos` sin crear las clases de `application/dto`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consulta Integral de Tiendas, Usuarios, Miembros, Mazos y Plantillas (Priority: P1)

Como **Jugador o Dueño de tienda**, quiero poder recuperar la información de usuarios, tiendas, miembros de cada tienda, mazos registrados por cada jugador y plantillas de torneos periódicos (incluyendo su vinculación opcional a una liga y su cupo máximo de jugadores), para visualizar el estado de la comunidad, los mazos disponibles de cada participante y la programación base de eventos del establecimiento.

**Why this priority**: Proporciona el acceso fundamental a las entidades maestras del sistema (usuarios, tiendas, membresías, mazos y plantillas), sin las cuales no es posible navegar por el catálogo de la tienda ni gestionar inscripciones o ligas.

**Independent Test**: Puede probarse de forma independiente poblando registros de usuarios, tiendas, miembros, mazos y plantillas de torneos, y verificando que cada elemento puede consultarse individualmente por su identificador y mediante listados filtrados por tienda, por liga o por usuario.

**Acceptance Scenarios**:

1. **Given** una tienda registrada con un propietario y varios usuarios miembros, **When** se solicita la información de la tienda y su comunidad, **Then** el sistema devuelve los datos de la tienda (nombre, moneda, propietario, fecha de alta) junto con la lista completa de usuarios miembros y su fecha de incorporación.
2. **Given** un usuario con varios mazos vinculados a Moxfield, **When** se consulta el perfil del usuario o su colección de mazos, **Then** el sistema devuelve los datos del usuario (nombre de usuario, rol, fecha de creación) y el listado de todos sus mazos con su nombre, enlace e identificador de Moxfield.
3. **Given** una tienda con plantillas de torneos configuradas, **When** el dueño consulta las plantillas de su tienda, **Then** el sistema devuelve todas las plantillas (activas e inactivas) con su liga asociada (si aplica), periodicidad, coste de entrada, premio, formato, presupuesto máximo, cupo máximo de jugadores, número máximo de rondas y fecha de última generación.

---

### User Story 2 - Extracción de Ligas, Clasificaciones de Liga, Torneos, Inscripciones, Rondas y Partidas (Priority: P1)

Como **Jugador, Dueño de tienda o Espectador**, quiero extraer toda la información competitiva de las ligas y los torneos (ligas de una tienda, tabla de posiciones y estadísticas de liga por jugador, detalles del torneo con su cupo y liga asociada, jugadores inscritos con sus puntos, porcentaje de victorias de oponentes, posición final, instantánea de su mazo y desglose de cartas por sección, así como las rondas y partidas con sus resultados), para seguir en todo momento la clasificación de las ligas, el desarrollo de cada torneo y auditar los mazos participantes.

**Why this priority**: Es el núcleo competitivo de la plataforma; permite mostrar ligas por temporada, clasificaciones consolidadas, calendarios de torneos, listas de inscritos ordenadas por puntuación, mazos inmutables presentados al registrarse, emparejamientos por mesa y resultados de cada ronda.

**Independent Test**: Puede probarse de forma independiente sobre una liga con varios torneos asociados, jugadores apuntados en la liga, inscripciones en torneos con puntuación y cartas asociadas, rondas y emparejamientos cargados, comprobando que se extraen íntegramente tanto las entidades individuales como la clasificación consolidada de la liga.

**Acceptance Scenarios**:

1. **Given** una tienda con una o varias ligas creadas en distintos estados (*Abierta*, *En Curso*, *Completada*, *Cancelada*), **When** se consultan las ligas de la tienda y su clasificación, **Then** el sistema devuelve los datos de cada liga (nombre, formato *Standard* o *Commander*, descripción de premios, fechas de inicio y fin, estado), el registro de participación de cada jugador en la liga (puntos totales, torneos jugados, partidas ganadas, empatadas y perdidas) y la tabla de clasificación ordenada por posición (`current_rank`), puntos totales y torneos disputados.
2. **Given** una tienda o liga con múltiples torneos en distintos estados (*Borrador*, *Inscripción Abierta*, *En Curso*, *Completado*, *Cancelado*), **When** se consultan los torneos filtrados por tienda o por liga, **Then** el sistema devuelve cada torneo con su liga y plantilla de origen (si existen), fecha programada, precio, premio, formato, presupuesto máximo, cupo máximo de jugadores, ronda actual y rondas máximas.
3. **Given** un torneo con jugadores apuntados, **When** se consultan las inscripciones o la clasificación del torneo, **Then** el sistema devuelve cada inscripción con el usuario, el mazo de referencia, el estado de participación (*Registrado*, *Confirmado*, *Retirado*, *Descalificado*), el precio calculado del mazo, los puntos de partida acumulados (`match_points`), el porcentaje de victorias de los oponentes (`omw_percentage`), el puesto final (`final_rank`), la instantánea completa del mazo y el listado detallado de sus cartas indicando sección (*Comandante*, *Mazo Principal*, *Banquillo*), cantidad y precio unitario fijado al inscribirse.
4. **Given** un torneo en curso o finalizado con rondas generadas, **When** se consultan las rondas y partidas del torneo, **Then** el sistema devuelve cada ronda en orden secuencial con su estado y marcas de tiempo, junto con todas las partidas de cada ronda indicando número de mesa, jugadores enfrentados (o asignación de pase libre/BYE cuando no hay segundo jugador), victorias de cada jugador, empates, estado de la partida y ganador.

---

### User Story 3 - Comunicación con Scryfall, Catálogo Local de Cartas y Estadísticas de Metajuego (Priority: P2)

Como **Sistema de Validación, Dueño de tienda o Jugador**, quiero que el sistema se comunique con el catálogo oficial de Scryfall para obtener y actualizar la información de las cartas de Magic (atributos de juego, legalidades por formato y precios en EUR y USD), almacenarla localmente y extraer tanto fichas de cartas individuales como estadísticas agregadas de uso y precio de cartas por tienda, liga y formato, para validar mazos y analizar el metajuego sin depender de llamadas externas en cada lectura.

**Why this priority**: Garantiza que el sistema pueda poblar y mantener al día el catálogo de cartas de Scryfall y ofrecer métricas analíticas inmediatas sobre las cartas más jugadas en cada tienda, liga y formato.

**Independent Test**: Puede probarse solicitando al sistema la sincronización de un conjunto de cartas frente a Scryfall, verificando su persistencia en el catálogo local y extrayendo posteriormente tanto las cartas individuales como las estadísticas agregadas de cartas cruzadas con las inscripciones de los torneos.

**Acceptance Scenarios**:

1. **Given** un mazo que se registra o inscribe cuyos identificadores o nombres de cartas aún no existen en el catálogo local (o cuyos datos han superado el tiempo de vigencia), **When** el backend procesa el registro del mazo, **Then** se comunica automáticamente con Scryfall para consultar las cartas del mazo, normaliza sus precios en EUR y USD y sus legalidades, y las guarda o actualiza en `scryfall_cards` con la fecha de última sincronización.
2. **Given** cartas ya almacenadas en el catálogo local de Scryfall, **When** se solicita extraer información de cartas por identificador de carta, identificador de oráculo, nombre, colores o legalidad de formato, **Then** el sistema recupera los registros directamente desde el catálogo local devolviendo todos sus atributos técnicos, precios e imagen.
3. **Given** torneos con inscripciones y cartas desglosadas registradas en una tienda o liga, **When** se consultan las estadísticas de cartas filtradas por tienda, liga o formato (*Standard* o *Commander*), **Then** el sistema devuelve para cada carta su información de Scryfall junto con el número de mazos distintos que la incluyen (`decks_including_card`), el total de copias jugadas (`total_copies_played`) y el precio unitario medio registrado (`avg_unit_price`).

---

### Edge Cases

- **Límites de tasa (Rate Limiting) en la comunicación con Scryfall**: Cuando se necesita obtener información de un mazo completo (hasta 100 cartas en *Commander*), el sistema agrupa las consultas o respeta el intervalo mínimo entre peticiones exigido por el proveedor externo para evitar bloqueos de servicio, priorizando siempre las cartas ya sincronizadas recientemente en el almacenamiento local.
- **Cartas de doble cara, aventuras o sin coste de maná convencional**: Al recibir desde Scryfall cartas con múltiples caras o sin coste de maná o precio en una divisa concreta, el sistema consolida el nombre, colores, imagen principal y valores numéricos por defecto (valor de maná cero o precio nulo/alternativo) sin producir fallos de lectura ni escritura.
- **Torneos independientes vs. torneos asociados a una Liga**: Un torneo o plantilla puede existir de forma independiente (`league_id` vacío) o pertenecer a una liga; las consultas de extracción soportan ambos casos sin requerir que todo torneo pertenezca obligatoriamente a una liga.
- **Desempate y cálculo en la clasificación de Liga**: Cuando un jugador está apuntado a una liga pero aún no ha disputado torneos o tiene puntuación cero, la consulta de clasificación de liga lo incluye válidamente con cero puntos y cero torneos jugados, calculando su posición (`current_rank`) en función de los puntos totales y el número de torneos jugados.
- **Partidas con número impar de jugadores (BYE)**: Al extraer partidas de una ronda donde un jugador no tiene oponente asignado (`player2` vacío), el sistema devuelve la partida válidamente representando el pase libre (BYE) sin errores por ausencia del segundo participante.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Extracción de Usuarios y Resumen de Perfil)**: El sistema DEBE permitir extraer usuarios de forma individual (por identificador único o por nombre de usuario) y en listados, exponiendo su identificador, nombre de usuario, rol (*PLAYER* o *ADMIN*), fecha de creación y los datos agregados necesarios para `UserProfileResponse` (tiendas a las que pertenece, recuento de mazos registrados y resumen de ligas en las que participa), sin exponer nunca el hash de contraseña en las respuestas de consulta.
- **FR-002 (Extracción de Tiendas, Miembros y Contadores)**: El sistema DEBE permitir extraer todas las tiendas registradas o una tienda específica (por identificador o nombre), incluyendo su propietario, moneda configurada (*EUR* o *USD*), fecha de creación, la lista de usuarios miembros vinculados con su fecha de unión y los totales agregados requeridos por `StoreDetailResponse` (total de torneos y total de miembros).
- **FR-003 (Extracción de Mazos de Usuario)**: El sistema DEBE permitir extraer los mazos registrados por un usuario o un mazo específico por su identificador, devolviendo su nombre, URL de Moxfield, identificador público de mazo en Moxfield y fechas de creación y actualización para alimentar `DeckSummaryResponse`.
- **FR-004 (Extracción de Ligas y Estadísticas de Participación en Liga)**: El sistema DEBE permitir extraer las ligas de una tienda (filtrables por estado *OPEN*, *IN_PROGRESS*, *COMPLETED*, *CANCELLED* o por formato *STANDARD*, *COMMANDER*), su lista de torneos puntuables asociados y los registros de rendimiento de los jugadores apuntados en cada liga (`league_standings`: puntos totales, torneos jugados, partidas ganadas, empatadas y perdidas, y fecha de actualización) para alimentar `LeagueDetailResponse`.
- **FR-005 (Extracción de Clasificación de Liga - Leaderboard)**: El sistema DEBE permitir extraer la clasificación consolidada de cualquier liga (`v_league_leaderboard`), devolviendo para cada jugador participante su posición actual en el ranking (`current_rank`), sus puntos totales acumulados y la cantidad de torneos disputados dentro de la liga para alimentar `LeagueStandingEntryResponse`.
- **FR-006 (Extracción de Plantillas de Torneos)**: El sistema DEBE permitir extraer las plantillas de torneos de una tienda o de una liga (todas o filtradas por estado activo), devolviendo su liga vinculada (si existe), expresión de programación periódica, precio de entrada, descripción de premios, formato (*STANDARD* o *COMMANDER*), presupuesto máximo de mazo, cupo máximo de jugadores (`max_players`), número máximo de rondas, estado activo y fecha de última generación.
- **FR-007 (Extracción de Torneos)**: El sistema DEBE permitir extraer torneos de forma individual por identificador y en listados filtrables por tienda, liga, estado (*DRAFT*, *REGISTRATION_OPEN*, *IN_PROGRESS*, *COMPLETED*, *CANCELLED*) y rango de fechas, devolviendo todos sus atributos de configuración, cupo máximo de jugadores (`max_players`), total de jugadores inscritos y progreso de rondas para alimentar `TournamentSummaryResponse` y `TournamentDetailResponse`.
- **FR-008 (Extracción de Inscripciones, Clasificación de Torneo y Cartas Desglosadas)**: El sistema DEBE permitir extraer las inscripciones y la tabla de posiciones de un torneo (`TournamentStandingsResponse` / `StandingEntryResponse`, ordenadas por puntos de partida y porcentaje de desempate OMW, incluyendo victorias/derrotas/empates) o las inscripciones de un jugador, incluyendo el estado de la inscripción, el precio total calculado del mazo, los puntos de partida (`match_points`), el porcentaje de victorias de oponentes (`omw_percentage`), la posición final (`final_rank`), la instantánea inmutable del mazo (`DeckSnapshot` con identificador de mazo, formato, coste total, fecha y cartas con id Scryfall/Oracle, nombre, cantidad, tipo, coste de maná, colores y precio unitario) y la colección relacional de cartas asociadas (`registration_deck_cards`).
- **FR-009 (Extracción de Rondas y Partidas)**: El sistema DEBE permitir extraer todas las rondas de un torneo ordenadas por número de ronda y todas las partidas pertenecientes a cada ronda ordenadas por número de mesa (`RoundDetailResponse` y `MatchPairingResponse`), incluyendo jugadores asignados (o indicador de BYE), marcador de victorias y empates, estado de la partida (*PENDING*, *IN_PROGRESS*, *AWAITING_CONFIRMATION*, *DISPUTED*, *CONFIRMED*) y ganador.
- **FR-010 (Extracción del Catálogo Local de Cartas Scryfall y Analíticas de Metajuego)**: El sistema DEBE permitir extraer cartas almacenadas localmente (`scryfall_cards`) por identificador de Scryfall, identificador de oráculo, coincidencia de nombre, colores o legalidades en un formato dado, así como extraer las estadísticas agregadas de metajuego (`v_scryfall_card_stats` y cálculos para `MetagameAnalyticsResponse`) filtrables por tienda, liga, torneo y formato, exponiendo el coste medio por mazo, la distribución porcentual por color e identidad (`ColorBreakdown`), y el ranking de cartas más jugadas (`CardUsageStat`: nombre, id Scryfall/Oracle, coste de maná, frecuencia de uso `total_copies_played`, porcentaje de presencia en mazos `decks_including_card` y precio unitario medio `avg_unit_price`).
- **FR-011 (Comunicación con Scryfall al Registrar el Mazo y Sincronización Persistente)**: En el momento en que se registra o inscribe un mazo, el backend DEBE comunicarse internamente con el servicio externo de Scryfall para recuperar los datos del lote de cartas del mazo, respetando las políticas de tráfico del proveedor, y almacenar o actualizar los registros obtenidos en `scryfall_cards` con su marca de tiempo de sincronización para reutilizarlos en las lecturas de base de datos posteriores y en la construcción del snapshot inmutable del mazo.
- **FR-012 (Alineación con `002-backend-dtos` sin Creación de Clases DTO en esta Rama)**: El backend en esta rama DEBE implementar la migración de base de datos, los modelos y puertos de dominio (`domain/model`, `domain/repository`, `domain/service`), las entidades y adaptadores de persistencia JPA (`infrastructure/adapter/persistence/`) y el cliente de Scryfall (`infrastructure/adapter/scryfall/`), proporcionando todos los métodos de extracción y agregación que requieren los contratos definidos en `002-backend-dtos`, pero absteniéndose de crear las clases del paquete `application/dto` para evitar conflictos al fusionar ambas ramas.

### Key Entities *(include if feature involves data)*

- **Usuario (`users`)**: Actor del sistema identificado por UUID y nombre único, con rol de jugador o administrador/dueño y fecha de alta.
- **Tienda (`stores`) y Miembro de Tienda (`store_members`)**: Establecimiento organizador con nombre único, propietario, divisa base (*EUR* o *USD*) y relación de usuarios miembros con fecha de incorporación.
- **Mazo (`decks`)**: Referencia a un mazo de un usuario en Moxfield (nombre, URL e identificador externo único por usuario).
- **Liga (`leagues`) y Clasificación de Liga (`league_standings` / `v_league_leaderboard`)**: Competición de varios torneos perteneciente a una tienda con formato, premios, fechas de inicio/fin y estado, junto con el registro de jugadores apuntados, su desglose de victorias/empates/derrotas y su posición en el ranking general de la liga.
- **Plantilla de Torneo (`tournament_templates`)**: Configuración reutilizable asociada a una tienda (y opcionalmente a una liga) para generar torneos periódicos con reglas de formato, presupuesto, cupo máximo de jugadores, rondas, coste y premio.
- **Torneo (`tournaments`)**: Evento competitivo de una tienda (opcionalmente vinculado a una liga y/o plantilla) con fecha programada, estado del ciclo de vida, formato, presupuesto máximo, cupo máximo de jugadores y control de rondas.
- **Carta Scryfall (`scryfall_cards`) y Estadísticas de Cartas (`v_scryfall_card_stats`)**: Registro local sincronizado desde Scryfall con identificadores oficiales (carta y oráculo), características de juego, legalidades, precios (*EUR* y *USD*), imagen y fecha de sincronización, junto con su vista analítica de presencia en mazos, copias totales jugadas y precio medio por tienda, liga y formato.
- **Inscripción a Torneo (`tournament_registrations`) y Carta de Inscripción (`registration_deck_cards`)**: Participación de un usuario en un torneo con un mazo, que conserva el precio calculado, la puntuación competitiva (`match_points`, `omw_percentage`, `final_rank`), la instantánea completa del mazo y el desglose relacional de cada carta por sección, cantidad y precio unitario.
- **Ronda (`rounds`) y Partida (`matches`)**: Estructura competitiva de un torneo dividida en rondas numeradas y enfrentamientos por mesa con resultados, estado de confirmación y ganador.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las 12 tablas y las 2 vistas analíticas definidas en el esquema de base de datos (`users`, `stores`, `store_members`, `decks`, `leagues`, `league_standings`, `tournament_templates`, `tournaments`, `scryfall_cards`, `tournament_registrations`, `registration_deck_cards`, `rounds`, `matches`, `v_league_leaderboard` y `v_scryfall_card_stats`) pueden extraerse mediante operaciones de lectura individuales y de listado sin pérdida de atributos ni relaciones.
- **SC-002**: Las consultas de extracción de elementos almacenados en la base de datos (incluyendo el detalle completo de un torneo con su clasificación, rondas y partidas, así como el leaderboard de una liga y las estadísticas de cartas) devuelven respuesta en menos de 500 milisegundos para torneos de hasta 128 jugadores y ligas de hasta 50 torneos.
- **SC-003**: La comunicación con Scryfall permite resolver y sincronizar la totalidad de las cartas de un mazo de hasta 100 cartas en menos de 5 segundos cuando se consultan por primera vez, y en menos de 200 milisegundos cuando las cartas ya se encuentran vigentes en el catálogo local.
- **SC-004**: El 100% de las consultas de extracción de usuarios omiten credenciales sensibles (hashes de contraseña), garantizando la privacidad de las cuentas en todas las vistas y respuestas.
- **SC-005**: Ante una caída temporal del servicio externo de Scryfall, el 100% de las consultas sobre cartas previamente sincronizadas y las estadísticas de cartas (`v_scryfall_card_stats`) continúan funcionando con normalidad a partir de los datos almacenados localmente.

## Assumptions

- **Alcance exclusivo de Backend y exclusión de DTOs en esta rama**: Esta funcionalidad abarca exclusivamente el desarrollo del backend de extracción de base de datos y la integración con Scryfall; no se incluye código de frontend ni la creación de DTOs (`application/dto`), ya que los DTOs se están construyendo en otra rama independiente.
- **Vigencia de la caché local de Scryfall**: Los datos y precios de una carta sincronizada desde Scryfall se consideran vigentes durante un periodo configurable (por defecto 24 horas) antes de requerir una nueva consulta al servicio externo, salvo que se solicite una actualización explícita.
- **Cumplimiento de límites de Scryfall**: La comunicación con Scryfall respeta un ritmo máximo de peticiones compatible con las directrices públicas de Scryfall (máximo de 10 peticiones por segundo y uso de consultas por lotes para colecciones de cartas) e incluye cabeceras identificativas de la aplicación.
- **Seguridad y visibilidad de lecturas**: La información pública de tiendas, ligas, tablas de clasificación de ligas, torneos, clasificaciones de torneos, rondas, partidas, catálogo de cartas y estadísticas de cartas es consultable abiertamente, mientras que la gestión administrativa y acciones de jugador siguen las reglas de permisos por rol establecidas en el proyecto.

# Feature Specification: Lógica de Negocio de Usuarios, Mazos con Scryfall, Torneos, Ligas, Fórmulas Matemáticas y Estadísticas de Jornada

**Feature Branch**: `002-tournament-league-logic`

**Created**: 2026-11-01

**Status**: Draft

**Input**: User description: "Ahora vamos a la logica de negocio. Quiero que ell sistema sea capaz de registrar un usuario, un torneo, una liga. Quiero que se pueda hacer login. QUiero que se pueda acceder a un torneo añadiendo un mazo (nuevo o uno guardado). Quiero que se pueda guardar un mazo en el perfil. Quiero que este todo bien conectado con scryfall. Quiero que en una jornada se calculen estadstias relaccionadas con los mazos: cartas mas jugadas, colores mas jugados, comandantes mas ganadores, que se muestren emparejamientos de rivales (magda vs kefka gana kefka 6% por ejemplo). Y quiero que muestren estadiscticas relaccionadas con tu mazo. Quiero que a traves de una funcion matematica que establezca el dueño de la tienda se calculen los puntos en cada partida del torneo. Quiero que el organizador diga cuantas partidas hay por torneo. Quiero que a traves de una funcion matematica que establezca el dueño de la tienda se calculen los puntos en cada jornada de liga. Quiero que a traves de una funcion matematica que establezca el dueño de la tienda se calculen los parings en cada jornada. Quiero que en cada jornada se vaya calculando los puntos"

## Clarifications

### Session 2026-11-01

- Q: Gestión y definición de las fórmulas matemáticas (puntos de partida, puntos de jornada y *pairings*) → A: Se dejan de lado fórmulas cerradas predefinidas en la especificación; la base de datos almacenará las fórmulas matemáticas como datos persistidos que el dueño de la tienda puede consultar y modificar libremente, y el sistema las leerá para calcular puntos y emparejamientos.
- Q: ¿Quién registra y valida el resultado de cada partida en una jornada para disparar el cálculo de puntos? → A: Solo el organizador/dueño de la tienda introduce los resultados de todas las mesas.
- Q: ¿Cómo funciona la inscripción de jugadores y la elección de mazo en las jornadas de una liga? → A: Inscripción por jornada con mazo elegible en cada jornada (el jugador se apunta a cada jornada que juega con un mazo guardado o nuevo y la liga acumula sus puntos).
- Q: ¿Qué alcance temporal deben abarcar las estadísticas de enfrentamientos (*matchups* como "Magda vs Kefka") y las estadísticas de "Tu Mazo"? → A: Por jornada y acumulado (se calculan las estadísticas de cada jornada individual y además pueden consultarse agregadas a nivel de Liga o histórico del mazo).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registro de Usuarios, Inicio de Sesión y Gestión de Mazos en Perfil con Scryfall (Priority: P1)

Como **Jugador u Organizador (Dueño de tienda)**, quiero poder registrarme en la plataforma, iniciar sesión en mi cuenta y gestionar mis mazos guardados en mi perfil —sincronizados y validados automáticamente con los datos oficiales de **Scryfall**—, así como apuntarme a un torneo seleccionando uno de mis mazos guardados o añadiendo un mazo nuevo en ese mismo instante, para participar en eventos competitivos de forma ágil y sin duplicar esfuerzos.

**Why this priority**: Es la puerta de entrada obligatoria a toda la lógica de negocio; sin identidad autenticada ni gestión e inscripción de mazos conectados con Scryfall, no pueden celebrarse torneos ni calcularse estadísticas de cartas o comandantes.

**Independent Test**: Puede probarse de forma independiente registrando un usuario nuevo, iniciando sesión, guardando un mazo en su perfil (verificando que se obtienen de Scryfall sus cartas, comandante, colores, legalidad y precios), e inscribiéndose en dos torneos: en el primero eligiendo el mazo guardado del perfil y en el segundo introduciendo un mazo nuevo directamente durante la inscripción.

**Acceptance Scenarios**:

1. **Given** un visitante no registrado, **When** completa el formulario de registro con credenciales válidas y posteriormente inicia sesión (login), **Then** el sistema autentica al usuario y habilita las acciones correspondientes a su perfil (Jugador o Dueño de tienda).
2. **Given** un jugador con sesión iniciada en su perfil, **When** añade y guarda un mazo en su colección personal, **Then** el sistema sincroniza las cartas del mazo con **Scryfall** (identificando comandante(s), colores/identidad de color, tipos de carta, legalidad y coste total) y lo deja disponible para futuras inscripciones.
3. **Given** un jugador autenticado que accede a un torneo con inscripción abierta, **When** elige apuntarse seleccionando un mazo previamente guardado en su perfil o introduciendo un mazo nuevo en el momento (con opción de guardarlo también en su perfil), **Then** el sistema valida el mazo frente a las restricciones del torneo usando los datos de Scryfall y registra al jugador con una foto fija (*snapshot*) del mazo para ese torneo.

---

### User Story 2 - Creación de Torneos y Ligas con Configuración de Partidas y Fórmulas Matemáticas (Priority: P1)

Como **Dueño de la tienda (Organizador)**, quiero registrar torneos individuales y ligas compuestas por múltiples jornadas, definir cuántas partidas (rondas) tiene cada torneo y establecer las funciones matemáticas personalizadas con las que se calculan: (a) los puntos de cada partida del torneo, (b) los emparejamientos (*pairings*) en cada jornada/ronda y (c) los puntos otorgados en cada jornada de liga, para adaptar el sistema de competición exactamente a las reglas propias de mi tienda.

**Why this priority**: Constituye el núcleo diferencial de reglas de negocio solicitado por el organizador, permitiendo que el cálculo de emparejamientos y puntuaciones de partidas y jornadas responda a fórmulas matemáticas definidas por cada tienda.

**Independent Test**: Puede probarse creando una liga y un torneo/jornada asociado con un número específico de partidas (ej. 4 rondas), configurando las tres funciones matemáticas del dueño de la tienda (fórmula de puntos por partida, fórmula de emparejamiento y fórmula de puntos de jornada de liga) y verificando que al simular una jornada con resultados de partidas se generan los emparejamientos y se actualizan los puntos de partida, de jornada y el acumulado de la liga conforme a dichas fórmulas.

**Acceptance Scenarios**:

1. **Given** un dueño de tienda autenticado, **When** registra un nuevo torneo o jornada, **Then** puede especificar el número exacto de partidas (rondas) que compondrán el torneo y la función matemática que calculará los puntos obtenidos por cada jugador en cada partida.
2. **Given** un dueño de tienda que registra o configura una liga, **When** define la función matemática de puntuación por jornada y la función matemática de emparejamientos (*pairings*), **Then** el sistema valida que las expresiones matemáticas sean sintácticamente válidas y utilicen variables del dominio permitidas antes de activarlas en la liga.
3. **Given** una jornada en curso con su función matemática de *pairings* configurada, **When** el organizador solicita generar los emparejamientos de la siguiente ronda/jornada, **Then** el sistema evalúa la función matemática para los jugadores participantes, agrupa a los rivales evitando emparejamientos inválidos y publica las mesas de juego hasta completar el número de partidas definido para el torneo.
4. **Given** que se registran los resultados de cada partida dentro de una jornada, **When** finaliza una partida o la jornada completa, **Then** el sistema aplica primero la función matemática de partida para asignar los puntos del torneo y, al cierre o avance de la jornada, aplica la función matemática de jornada para recalcular y acumular automáticamente los puntos de cada jugador en la clasificación general de la liga.

---

### User Story 3 - Estadísticas de Jornada (Metajuego y Enfrentamientos) y Estadísticas de "Tu Mazo" con Scryfall (Priority: P2)

Como **Jugador u Organizador**, quiero consultar al término o durante el transcurso de cada jornada las estadísticas globales de los mazos participantes —cartas más jugadas, colores más jugados, comandantes más ganadores y porcentajes de victoria en enfrentamientos directos entre rivales/comandantes (ej. *"Magda vs Kefka: gana Kefka 60%"*)—, así como un panel específico con las estadísticas detalladas de **mi propio mazo**, todo enriquecido con los datos de **Scryfall**, para analizar el metajuego de la tienda y el rendimiento real de mi mazo.

**Why this priority**: Transforma los datos de partidas y de Scryfall en valor analítico directo tanto para la comunidad de la tienda como para cada jugador individual en cada jornada.

**Independent Test**: Puede probarse registrando una jornada con varios jugadores cuyos mazos están sincronizados con Scryfall, completando sus partidas con distintos resultados y consultando tanto el informe estadístico de la jornada como la vista personal "Estadísticas de tu mazo" de un jugador participante.

**Acceptance Scenarios**:

1. **Given** una jornada con mazos inscritos y partidas disputadas, **When** un usuario consulta las estadísticas de la jornada, **Then** el sistema muestra el ranking de cartas más jugadas, la distribución de colores/identidades de color más jugados (según Scryfall) y los comandantes más ganadores ordenados por tasa de victorias y puntos obtenidos.
2. **Given** el historial de partidas disputadas en la jornada o liga entre distintos comandantes/mazos, **When** se consulta la sección de emparejamientos de rivales (*matchups*), **Then** el sistema muestra las comparativas de enfrentamientos directos entre comandantes o rivales indicando el porcentaje de victoria o ventaja de cada uno (por ejemplo, *"Magda vs Kefka: gana Kefka 60%"*).
3. **Given** un jugador autenticado que ha participado con un mazo, **When** accede a las estadísticas relacionadas con su mazo (*"Tu Mazo"*), **Then** visualiza el rendimiento específico de ese mazo (porcentaje de victorias, puntos medios por partida y por jornada, historial de enfrentamientos contra comandantes rivales indicando contra cuáles gana o pierde más, y composición de colores/costes/tipos obtenida de Scryfall comparada con la media de la jornada).

---

### Edge Cases

- **Fórmula matemática inválida o división por cero**: Si el dueño de la tienda introduce una función matemática con errores de sintaxis, variables inexistentes o que produzca una indeterminación matemática (como división por cero cuando un jugador lleva 0 partidas jugadas), el sistema valida la fórmula al guardarla con casos de prueba y, en tiempo de ejecución, aplica un valor seguro (0 por defecto) notificando al organizador sin bloquear la jornada.
- **Número impar de jugadores o mesas incompletas al calcular *pairings***: Cuando el número de jugadores activos en una ronda no sea múltiplo exacto del tamaño de partida (2 en duelo o 4 en mesas de Commander), el motor de *pairings* asigna un *BYE* o ajusta el tamaño de la última mesa (mesas de 3 jugadores en Commander) aplicando la puntuación estipulada en la fórmula del organizador y evitando que un mismo jugador reciba múltiples *BYEs* en el mismo torneo.
- **Muestra reducida en enfrentamientos entre comandantes (*matchups*)**: Cuando dos comandantes se han enfrentado muy pocas veces o empatan en victorias, el sistema muestra junto al porcentaje de victoria el número total de partidas registradas entre ambos (ej. *"Gana Kefka 60% (3 de 5 partidas)"*) para evitar interpretaciones engañosas.
- **Fallo temporal o carta no encontrada en Scryfall durante la inscripción**: Si una carta del mazo no se encuentra en la caché local y el servicio externo de Scryfall no responde temporalmente, el sistema informa claramente qué carta no pudo verificarse y permite reintentar la validación sin perder los datos del formulario de inscripción ni duplicar el mazo en el perfil.
- **Modificación de un mazo guardado en el perfil tras inscribirse a una jornada**: Si un jugador actualiza un mazo guardado en su perfil después de haberlo usado en una jornada anterior o en curso, las estadísticas y validaciones de la jornada utilizan la instantánea (*snapshot*) tomada al inscribirse en dicho torneo, manteniendo intactas las estadísticas históricas de cada jornada.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Registro de Usuarios e Inicio de Sesión)**: El sistema DEBE permitir el registro de nuevos usuarios y el inicio de sesión (*login*) mediante credenciales verificadas, diferenciando permisos entre **Jugadores** y **Dueños de tienda / Organizadores**.
- **FR-002 (Guardado de Mazos en el Perfil del Usuario)**: El sistema DEBE permitir a cualquier jugador autenticado guardar, listar, actualizar y eliminar mazos dentro de su perfil personal para reutilizarlos en futuros torneos o jornadas de liga.
- **FR-003 (Inscripción a Torneo con Mazo Nuevo o Guardado)**: Al inscribirse en un torneo, el sistema DEBE permitir al jugador elegir entre:
  1. Seleccionar un mazo previamente guardado en su perfil.
  2. Añadir un mazo nuevo directamente en el flujo de inscripción (ofreciendo la opción de guardarlo simultáneamente en su perfil).
  En ambos casos, el sistema DEBE generar una copia inmutable (*snapshot*) del mazo vinculada a esa inscripción.
- **FR-004 (Integración Completa con Scryfall)**: El sistema DEBE sincronizar y vincular todas las cartas y comandantes de cada mazo con los datos oficiales de **Scryfall** (identificador único, nombre oficial, coste y valor de maná, colores, identidad de color, línea de tipo, indicación de elegibilidad como comandante, rareza, legalidades por formato e información de precios), utilizando estos datos tanto para validar la inscripción al torneo como para alimentar los cálculos estadísticos.
- **FR-005 (Registro de Torneos y Configuración de Número de Partidas)**: El sistema DEBE permitir al organizador crear un **Torneo** definiendo sus datos básicos, restricciones y el **número exacto de partidas (rondas)** que se disputarán en dicho torneo, impidiendo generar rondas ordinarias adicionales una vez alcanzado ese límite (salvo edición explícita del organizador).
- **FR-006 (Registro de Ligas y Gestión por Jornadas)**: El sistema DEBE permitir al organizador crear una **Liga** compuesta por una secuencia ordenada de **Jornadas** (donde cada jornada es un torneo puntuable dentro de la liga al que los jugadores se inscriben de forma independiente eligiendo el mazo con el que competirán en esa jornada), manteniendo una clasificación general acumulada de la liga que suma los puntos obtenidos por cada jugador en todas las jornadas disputadas.
- **FR-007 (Fórmula Matemática Persistida para Puntos por Partida)**: El sistema DEBE almacenar en la base de datos la fórmula matemática de puntos por partida definida por el dueño de la tienda, permitiendo que el dueño la modifique y utilizándola para calcular los puntos de cada partida del torneo.
- **FR-008 (Fórmula Matemática Persistida para Puntos por Jornada de Liga)**: El sistema DEBE almacenar en la base de datos la fórmula matemática de puntos por jornada definida por el dueño de la tienda, permitiendo su edición por el propietario y aplicándola en cada jornada para calcular y acumular los puntos de la liga.
- **FR-009 (Fórmula Matemática Persistida para Cálculo de Emparejamientos / *Pairings*)**: El sistema DEBE almacenar en la base de datos la fórmula matemática de *pairings* definida por el dueño de la tienda, permitiendo que el dueño la actualice y empleándola en cada jornada para generar los emparejamientos entre jugadores.
- **FR-010 (Registro de Resultados por el Organizador y Cálculo Progresivo de Puntos)**: Únicamente el organizador/dueño de la tienda DEBE poder introducir y modificar los resultados de las partidas de cada ronda; a medida que el organizador registra dichos resultados dentro de una jornada, el sistema DEBE recalcular de forma inmediata tanto la puntuación acumulada de los jugadores en la jornada actual como su puntuación acumulada en la clasificación general de la liga.
- **FR-011 (Estadísticas de Mazos por Jornada y Acumuladas en Liga/Histórico)**: En cada jornada, y también de forma agregada a nivel de Liga e histórico, el sistema DEBE calcular y mostrar estadísticas basadas en los mazos inscritos y los resultados de las partidas, incluyendo obligatoriamente:
  1. **Cartas más jugadas** (frecuencia absoluta y porcentaje de presencia entre los mazos de la jornada, con opción de filtrar tierras básicas).
  2. **Colores e identidades de color más jugados** (porcentaje y conteo de cada color e identidad de color según Scryfall).
  3. **Comandantes más ganadores** (ranking de comandantes según tasa de victorias, puntos medios obtenidos y partidas ganadas).
  4. **Emparejamientos directos entre rivales / comandantes (*Matchups*)** (estadística comparativa cara a cara entre comandantes o arquetipos enfrentados, tanto en la jornada como en el acumulado de liga/histórico, mostrando quién domina el enfrentamiento y en qué porcentaje, p. ej., *"Magda vs Kefka: gana Kefka 60%"*).
- **FR-012 (Estadísticas Individuales de "Tu Mazo")**: El sistema DEBE proporcionar a cada jugador una vista de estadísticas dedicada a su mazo (*"Tu Mazo"*), consultable tanto para una jornada concreta como en el acumulado de la liga e histórico general del mazo, mostrando: tasa de victorias del mazo, puntos medios obtenidos por partida y por jornada, desglose de enfrentamientos contra los comandantes/rivales a los que se ha medido (indicando porcentajes de victoria/derrota frente a cada uno) y métricas de composición del mazo obtenidas de Scryfall (curva de maná, reparto de colores, tipos de cartas y valor económico).

### Key Entities *(include if feature involves data)*

- **Usuario (User)**: Cuenta registrada con credenciales de acceso (*login*), rol (**Jugador** o **Dueño/Organizador**) y colección de **Mazos guardados en su perfil**.
- **Mazo de Perfil y Snapshot de Inscripción (Profile Deck & Registration Deck Snapshot)**: El mazo guardado en el perfil almacena la referencia del usuario y sus cartas enriquecidas con **Scryfall** (incluyendo comandante(s) e identidad de color); al inscribirse en un torneo o jornada, genera un *snapshot* inmutable con el que compite y se calculan las estadísticas de esa jornada.
- **Carta Scryfall (Scryfall Card)**: Catálogo sincronizado con Scryfall que aporta atributos de juego (nombre, colores, identidad de color, coste de maná, línea de tipo, elegibilidad como comandante, legalidad y precio) para validaciones y analítica.
- **Liga (League)**: Competición continuada organizada por una tienda que agrupa múltiples **Jornadas**, conserva las **Funciones Matemáticas** de puntuación de jornada y *pairings* establecidas por el dueño, y mantiene la **Clasificación General Acumulada** de los jugadores.
- **Torneo / Jornada (Tournament / Matchday)**: Evento competitivo individual o perteneciente a una **Liga**, configurado con un **número determinado de partidas (rondas)**, su **Función Matemática de Puntos por Partida**, su **Función Matemática de Emparejamientos (*Pairings*)**, la lista de jugadores inscritos con sus mazos y las rondas disputadas.
- **Partida y Resultado por Jugador (Match & Player Match Result)**: Enfrentamiento dentro de una ronda de un torneo (duelo 1v1 o mesa multijugador de Commander) que registra los jugadores emparejados, sus comandantes/mazos, las métricas de desempeño en la partida (victoria/empate/derrota, puesto, eliminaciones) y los puntos resultantes tras aplicar la función matemática de partida.
- **Configuración de Función Matemática (Scoring & Pairing Formula)**: Expresión matemática segura definida por el dueño de la tienda (para puntos de partida, puntos de jornada de liga o criterio de *pairings*) compuesta por variables del dominio y operadores matemáticos evaluables de forma determinista.
- **Informe Estadístico de Jornada y de Mazo (Matchday & Personal Deck Analytics)**: Conjunto de métricas calculadas por jornada y por mazo individual (cartas más jugadas, colores más jugados, comandantes más ganadores, matriz de enfrentamientos *Comandante A vs Comandante B* con porcentajes de victoria, y estadísticas propias de *"Tu Mazo"*).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario puede completar su registro, iniciar sesión, guardar un mazo en su perfil sincronizado con Scryfall e inscribirse en un torneo (eligiendo el mazo guardado o añadiendo uno nuevo) en menos de 2 minutos.
- **SC-002**: El dueño de la tienda puede crear un torneo o liga definiendo el número de partidas y configurando las funciones matemáticas de puntos de partida, puntos de jornada y *pairings* en menos de 3 minutos, con validación inmediata de las fórmulas antes de guardarlas.
- **SC-003**: El 100% de las puntuaciones de partidas, clasificaciones de jornadas de liga y generación de emparejamientos (*pairings*) producen resultados matemáticamente exactos y deterministas de acuerdo con las funciones configuradas por el dueño de la tienda en menos de 2 segundos por ronda.
- **SC-004**: Al finalizar cualquier partida o jornada, las estadísticas de la jornada (cartas más jugadas, colores más jugados, comandantes más ganadores y porcentajes de enfrentamientos entre comandantes rivales como *"Comandante A vs Comandante B"*) y las estadísticas individuales de *"Tu Mazo"* se actualizan y muestran al usuario en menos de 3 segundos.
- **SC-005**: El 100% de las cartas e identidades de comandantes utilizadas en torneos cuentan con metadatos completos de Scryfall para el cálculo de colores, tipos y legalidades sin requerir intervención manual del organizador.

## Assumptions

- **Almacenamiento de fórmulas matemáticas en base de datos**: Las fórmulas matemáticas para calcular puntos por partida, puntos por jornada de liga y emparejamientos (*pairings*) se guardan directamente en la base de datos asociadas a la tienda/torneo/liga para que el dueño de la tienda pueda crearlas y modificarlas cuando lo necesite, dejando la definición concreta de las expresiones a la configuración almacenada.
- **Formatos soportados en enfrentamientos y estadísticas de comandantes**: Las estadísticas de *"comandantes más ganadores"* y enfrentamientos del tipo *"Magda vs Kefka"* aplican de forma nativa a formatos con comandante (*Commander / EDH / Brawl*), mientras que en formatos sin comandante (como *Standard*) dicha métrica agrupa por el nombre o arquetipo del mazo inscrito.
- **Cálculo de porcentaje en enfrentamientos entre rivales (*Matchups*)**: En un enfrentamiento entre el Comandante/Mazo A y el Comandante/Mazo B, el porcentaje mostrado representa la tasa de victorias o superación directa de uno frente al otro sobre el total de partidas en las que ambos han coincidido en la misma mesa o duelo durante la jornada o liga.
- **Relación entre Torneo, Jornada y Liga**: Un torneo puede celebrarse como evento independiente o como una **Jornada** numerada dentro de una **Liga**; en ambos casos comparte el mismo motor de rondas, *pairings*, puntos por partida y estadísticas de mazos, añadiendo en el caso de la liga el cálculo y acumulación de puntos de jornada mediante la función matemática de liga.

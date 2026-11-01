# Feature Specification: Modelo de Datos de Tienda, Torneos, Mazos (Moxfield) y Estadísticas (Scryfall)

**Feature Branch**: `001-store-tournament-data-model`

**Created**: 2026-11-01

**Status**: Draft

**Input**: User description: "Quiero que crees el spec de la base de datos. Tiene que constar de: Usuario: nombre, contraseña y lista de mazos, habra un usuario que sera el dueño que podra hacer mas cosas. Torneo: Dia y hora, precio, lista de usuarios apuntads, restricciones (formato de torneo de magic standrat, commander y restriccion de precio) y premio. Mazo: sara simplemente un enlace a moxfield. Tambien tienes que tener la informacion que aporta scryfall sobre las cartas para hacer las estadisticas. Tienda: lista de torneos, propietario, usuarios"

## Clarifications

### Session 2026-11-01

- Contexto aportado por el usuario: Los jugadores mantienen una lista de mazos guardados en su perfil; en cada jornada de liga/torneo el jugador debe registrarse con su mazo quedando guardado históricamente para futuras estadísticas, y la liga debe mantener un registro explícito de los jugadores inscritos que van a disputar sus torneos.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gestión de Tienda y Creación de Torneos por el Dueño (Priority: P1)

Como usuario **Dueño (Propietario)** de una tienda, quiero gestionar mi tienda (su comunidad de usuarios y su calendario de torneos) y crear torneos indicando día y hora, precio de entrada, premio y restricciones competitivas (formato de Magic como *Standard* o *Commander*, y límite de presupuesto del mazo), para organizar eventos oficiales con reglas claras para los jugadores.

**Why this priority**: Es la base estructural del sistema; sin una tienda, un propietario con permisos elevados y torneos configurados con sus restricciones, los jugadores no pueden inscribirse ni competir.

**Independent Test**: Puede probarse de forma independiente registrando un usuario con rol de dueño, asociándolo como propietario de una tienda, dando de alta un torneo con fecha/hora, coste de inscripción, premio y restricciones (*Commander* con límite de presupuesto de 100 €) y comprobando que aparece en el catálogo de torneos de la tienda.

**Acceptance Scenarios**:

1. **Given** un usuario identificado como dueño de una tienda, **When** crea un torneo especificando día y hora, precio de inscripción, premio, formato (*Standard* o *Commander*) y precio máximo permitido por mazo, **Then** el torneo queda registrado dentro de la lista de torneos de la tienda con todas sus restricciones activas.
2. **Given** un usuario normal (jugador que no es dueño de la tienda), **When** intenta crear, modificar o eliminar un torneo de la tienda, **Then** el sistema rechaza la operación por falta de privilegios de propietario.
3. **Given** una tienda activa, **When** el dueño consulta la información de su tienda, **Then** visualiza el propietario asignado, el listado de usuarios vinculados a la tienda y el historial/calendario de torneos.

---

### User Story 2 - Gestión de Mazos mediante Enlace a Moxfield e Inscripción a Torneos (Priority: P1)

Como **Jugador** registrado en una tienda, quiero guardar mis mazos indicando únicamente su enlace a Moxfield y apuntarme a un torneo seleccionando uno de mis mazos, para que el sistema verifique automáticamente que mi mazo cumple el formato (*Standard* o *Commander*) y la restricción de precio antes de confirmar mi plaza.

**Why this priority**: Representa el flujo principal de valor para los jugadores y conecta las entidades Usuario, Mazo, Torneo y validación de cartas.

**Independent Test**: Puede probarse creando un usuario jugador con nombre y contraseña, añadiendo un mazo mediante una URL válida de Moxfield e inscribiéndolo en un torneo abierto cuyo límite de precio y formato sean compatibles con las cartas del mazo.

**Acceptance Scenarios**:

1. **Given** un usuario registrado con nombre y contraseña, **When** añade un nuevo mazo proporcionando un enlace válido de Moxfield, **Then** el mazo queda asociado a la lista de mazos del usuario.
2. **Given** un torneo con formato *Standard* y límite de presupuesto de mazo de 150 €, **When** un usuario de la tienda solicita apuntarse con un mazo de Moxfield legal en *Standard* cuyo valor total de cartas es de 120 €, **Then** el usuario queda añadido a la lista de apuntados del torneo y se conserva una copia fija (snapshot) de las cartas y precios de su mazo en ese instante.
3. **Given** un torneo con formato *Commander* y restricción de precio de 80 €, **When** un usuario intenta apuntarse con un mazo cuyo valor total según los datos de cartas supera los 80 € o incluye cartas no legales en *Commander*, **Then** el sistema impide la inscripción e informa el motivo exacto del incumplimiento (presupuesto excedido o carta ilegal).

---

### User Story 3 - Enriquecimiento de Cartas con Scryfall y Generación de Estadísticas (Priority: P2)

Como **Dueño de tienda o Jugador**, quiero que el sistema almacene la información detallada que aporta Scryfall sobre cada carta presente en los mazos de los torneos, para consultar estadísticas sobre las cartas más jugadas, distribución de colores, coste de maná y precios medios en los torneos de la tienda.

**Why this priority**: Permite tanto el cálculo preciso de restricciones de presupuesto/formato como la explotación analítica del metajuego de la tienda sin depender de consultas externas repetitivas cada vez que se visualizan estadísticas.

**Independent Test**: Puede probarse sincronizando los datos de Scryfall de las cartas contenidas en los mazos inscritos en un torneo y consultando el resumen estadístico (top cartas más usadas, colores predominantes y coste total por mazo).

**Acceptance Scenarios**:

1. **Given** un mazo importado desde un enlace de Moxfield al inscribirse en un torneo, **When** el sistema procesa su listado de cartas, **Then** cada carta queda vinculada a su información de Scryfall (identificador, nombre, coste de maná/valor de maná, colores e identidad de color, línea de tipo, rareza, legalidades por formato y precio unitario).
2. **Given** uno o varios torneos celebrados en una tienda con jugadores apuntados, **When** se consultan las estadísticas de la tienda o del torneo, **Then** el sistema muestra métricas agregadas basadas en la información de Scryfall (frecuencia de aparición de cartas, distribución por colores/tipos y coste medio de los mazos participantes).

---

### Edge Cases

- **Modificación posterior del mazo en Moxfield**: ¿Qué ocurre si un jugador edita su mazo en Moxfield después de haberse apuntado a un torneo? El sistema conserva la foto fija (snapshot) de las cartas y su coste en el momento de la inscripción para que el torneo y las estadísticas históricas no se vean alterados.
- **Inscripción duplicada**: ¿Qué ocurre si un mismo usuario intenta apuntarse dos veces al mismo torneo? El sistema rechaza el segundo registro garantizando una única participación por jugador y torneo.
- **Cartas sin precio disponible o ediciones múltiples en Scryfall**: Cuando una carta tenga múltiples impresiones o no disponga de precio en la edición seleccionada en Moxfield, el sistema toma por defecto el precio estándar de la edición indicada o, en su defecto, el precio más económico disponible de una impresión legal de esa misma carta para no penalizar injustamente el límite de presupuesto.
- **Enlace de Moxfield inválido o privado**: Si el enlace proporcionado no corresponde a un mazo público accesible de Moxfield, el sistema informa al usuario que no es posible leer la lista de cartas para validar las restricciones del torneo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (Gestión de Usuarios y Roles)**: El sistema DEBE almacenar para cada **Usuario** su nombre identificativo único, su contraseña almacenada de forma cifrada, su rol (distinguiendo entre usuario estándar/jugador y usuario **Dueño/Propietario** con permisos de administración) y su lista de mazos asociados.
- **FR-002 (Permisos del Dueño)**: El sistema DEBE permitir exclusivamente al usuario **Dueño** de una tienda crear, editar y cancelar torneos de su tienda, definir las restricciones y premios de cada torneo y gestionar el listado de usuarios vinculados a la tienda.
- **FR-003 (Estructura de Tienda)**: El sistema DEBE almacenar para cada **Tienda** su nombre/identificación, la referencia obligatoria a su usuario **Propietario (Dueño)**, la lista de **Usuarios** miembros de la tienda y la lista de **Torneos** organizados por ella.
- **FR-004 (Representación de Mazo del Usuario)**: El sistema DEBE permitir a cualquier usuario registrar un **Mazo** proporcionando únicamente el enlace (URL) a Moxfield, asociándolo a la lista de mazos de dicho usuario.
- **FR-005 (Estructura y Atributos del Torneo)**: El sistema DEBE almacenar para cada **Torneo**: la tienda organizadora, el día y la hora de celebración, el precio de entrada/inscripción, el premio (descripción o dotación), las restricciones configuradas y la lista de usuarios apuntados.
- **FR-006 (Restricciones del Torneo)**: El sistema DEBE soportar en cada **Torneo** la definición de restricciones compuestas por:
  1. **Formato de juego de Magic** (incluyendo obligatoriamente *Standard* y *Commander*, extensible a otros formatos).
  2. **Restricción de precio máximo del mazo** (presupuesto tope permitido para la suma de cartas del mazo inscrito).
- **FR-007 (Inscripción de Usuarios y Snapshot del Mazo)**: Al apuntarse un usuario a un torneo con uno de sus mazos de Moxfield, el sistema DEBE validar que el mazo cumple las restricciones de formato y presupuesto del torneo, registrar la inscripción en la lista de usuarios apuntados y guardar una instantánea inmutable de la composición y coste del mazo en ese momento.
- **FR-008 (Catálogo e Información de Cartas de Scryfall)**: El sistema DEBE almacenar la información aportada por **Scryfall** para las cartas utilizadas en los mazos, incluyendo como mínimo: identificador de Scryfall/Oracle, nombre de la carta, coste de maná y valor de maná (CMC), colores e identidad de color, línea de tipo (criatura, instantáneo, tierra, comandante, etc.), rareza, legalidades por formato (*Standard*, *Commander*, etc.) y precios de mercado.
- **FR-009 (Explotación Estadística con Datos de Scryfall)**: El sistema DEBE permitir agregar y consultar estadísticas por torneo, por liga y por tienda a partir de los datos de Scryfall y las inscripciones registradas (cartas más jugadas, distribución de colores, curva de maná promedio, tipos de carta predominantes y coste medio de los mazos).
- **FR-010 (Estructura y Puntuación de Liga)**: El sistema DEBE almacenar para cada **Liga**: la tienda organizadora, el nombre y periodo/estado de la liga, la **lista de torneos** que puntúan para dicha liga y la **lista de jugadores participantes con sus puntos acumulados** (donde los puntos de cada jugador en la liga vienen determinados por los puntos obtenidos en los torneos asociados a la liga).

### Key Entities *(include if feature involves data)*

- **Tienda (Store)**: Representa el establecimiento o comunidad organizadora. Se relaciona con un único **Propietario** (Usuario con rol de dueño), una colección de **Usuarios** miembros, una colección de **Ligas** y una colección de **Torneos**.
- **Liga (League)**: Competición continuada organizada por una **Tienda** compuesta por una **lista de Torneos** puntuables y una **lista de Jugadores con sus puntos acumulados**, calculados a partir de su rendimiento en los torneos de la liga.
- **Usuario (User)**: Representa a una persona registrada en la plataforma. Contiene nombre de usuario, credencial de contraseña protegida, nivel de privilegio (Jugador o Dueño), pertenencia a tiendas, participación/puntos en ligas y su lista de **Mazos**.
- **Mazo (Deck)**: Representa un mazo perteneciente a un usuario, definido por su enlace directo a **Moxfield** (URL e identificador del mazo) y metadatos básicos derivados al sincronizarse.
- **Torneo (Tournament)**: Evento competitivo perteneciente a una **Tienda** y opcionalmente vinculado a una **Liga**. Incluye día y hora programados, precio de inscripción, aforo máximo, premio, estado del torneo, **Restricciones** (formato como *Standard* o *Commander* y presupuesto máximo del mazo) y la lista de **Inscripciones** (usuarios apuntados con sus puntos del torneo).
- **Inscripción a Torneo (Tournament Registration)**: Vínculo único entre un **Usuario** y un **Torneo**, que referencia el **Mazo** utilizado, registra los puntos obtenidos por el jugador en el torneo (que alimentan la **Liga**) y preserva la instantánea inmutable (snapshot) de las cartas y su valoración económica en el momento de apuntarse.
- **Carta Scryfall (Scryfall Card Metadata)**: Entidad de referencia que conserva los atributos oficiales de Scryfall (nombre, coste/valor de maná, colores, identidad de color, tipos, rareza, legalidad en *Standard*/*Commander* y precio) necesarios para validar restricciones y calcular estadísticas.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario Dueño puede dar de alta un nuevo torneo completo (día y hora, precio, premio, formato *Standard* o *Commander* y límite de precio de mazo) en menos de 1 minuto.
- **SC-002**: Un jugador puede registrar un mazo mediante su enlace de Moxfield y completar su inscripción validada (formato y presupuesto comprobados frente a datos de Scryfall) en menos de 10 segundos.
- **SC-003**: El 100% de las inscripciones con mazos que superen el límite de precio del torneo o contengan cartas ilegales en el formato seleccionado (*Standard* o *Commander*) son bloqueadas automáticamente antes de confirmar la plaza.
- **SC-004**: El 100% de los torneos en curso o finalizados mantienen intactas sus listas de cartas y estadísticas históricas aunque los jugadores modifiquen o borren posteriormente sus mazos en Moxfield.
- **SC-005**: Las consultas de estadísticas de tienda y torneo (cartas más jugadas, distribución de colores, curva de maná y presupuesto medio) muestran resultados completos sin necesidad de volver a consultar servicios externos durante la visualización.

## Assumptions

- **Moneda de referencia**: Los precios de inscripción a torneos y los límites de presupuesto de mazos se expresan en una moneda uniforme por tienda (por defecto Euros `EUR` o Dólares `USD`, tomando el campo de precio correspondiente de Scryfall).
- **Visibilidad en Moxfield**: Los enlaces de Moxfield aportados por los usuarios corresponden a mazos configurados como públicos o no listados (accesibles mediante su enlace) para permitir la lectura de sus cartas.
- **Actualización de datos de Scryfall**: La información de Scryfall almacenada localmente se actualiza al validar nuevos mazos o mediante sincronizaciones periódicas para mantener vigentes los precios y legalidades de *Standard* y *Commander*.
- **Relación Tienda-Usuarios**: Un usuario Dueño administra su tienda; los jugadores pueden pertenecer a una o varias tiendas y apuntarse a cualquiera de los torneos publicados en ellas.


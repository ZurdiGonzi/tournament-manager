# Quickstart: Validación de Contratos y DTOs del Backend

**Feature**: `002-backend-dtos` | **Fecha**: 2026-11-01

Esta guía describe cómo verificar y validar los DTOs de Java 21, su inmutabilidad, las anotaciones de Jakarta Validation y la serialización/deserialización con Jackson.

---

## Prerrequisitos

- **Java JDK 21** o superior instalado y configurado en el `PATH` (`java -version`).
- Dependencias mínimas requeridas en el `pom.xml`:
  - `spring-boot-starter-validation` (Jakarta Validation / Hibernate Validator).
  - `spring-boot-starter-web` (Spring MVC, Jackson con módulo JSR-310 para `Instant`).
  - `spring-boot-starter-test` / `junit-jupiter` (para pruebas unitarias de contratos).

---

## Escenarios de Validación Rápida

### 1. Validación de Inmutabilidad y Constructor Canónico en Java 21

Verifica que los records no expongan mutabilidad y que los constructores compactos apliquen valores predeterminados seguros.

- **Prueba**:
  ```java
  @Test
  void shouldAssignDefaultPlayerRoleWhenRoleIsNull() {
      var request = new RegisterRequest("gonzalo_mtg", "gonzalo@example.com", "Password123!", null);
      assertEquals(UserRole.PLAYER, request.role());
  }
  ```
- **Resultado Esperado**: El rol se establece automáticamente en `UserRole.PLAYER` y ningún campo puede ser alterado tras la instanciación.

---

### 2. Validación de Restricciones Jakarta (`@Valid`)

Verifica que el validador rechace campos obligatorios en blanco, correos inválidos, formatos incorrectos de URLs de Moxfield o límites numéricos violados.

- **Prueba con Validator**:
  ```java
  var validator = Validation.buildDefaultValidatorFactory().getValidator();

  // Caso A: Registro con username inválido y password corta
  var invalidRegister = new RegisterRequest("a", "email-invalido", "123", null);
  var violations = validator.validate(invalidRegister);
  assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
  assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
  assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));

  // Caso B: URL de Moxfield no compatible
  var invalidDeck = new RegisterDeckRequest("Azorius Control", "https://google.com/deck/123");
  var deckViolations = validator.validate(invalidDeck);
  assertTrue(deckViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("moxfieldUrl")));
  ```
- **Resultado Esperado**: Las violaciones de validación identifican con precisión el campo infractor y el mensaje descriptivo en español configurado en la anotación.

---

### 3. Prueba de Serialización / Deserialización JSON con Jackson

Verifica la serialización bidireccional de tipos críticos (`Instant` a ISO-8601 UTC y `BigDecimal` para precios).

- **Prueba con ObjectMapper**:
  ```java
  var mapper = JsonMapper.builder()
      .findAndAddModules() // Incluye JavaTimeModule
      .build();

  var snapshot = new DeckSnapshotDto(
      UUID.randomUUID(),
      "Simic Ramp",
      "https://www.moxfield.com/decks/test1234",
      TournamentFormat.COMMANDER,
      new BigDecimal("98.50"),
      Instant.parse("2026-11-01T15:30:00Z"),
      List.of(
          new DeckCardDto(
              UUID.randomUUID(),
              UUID.randomUUID(),
              "Sol Ring",
              1,
              "{1}",
              BigDecimal.ONE,
              "Artifact",
              List.of(),
              List.of(),
              new BigDecimal("1.50"),
              false
          )
      )
  );

  String json = mapper.writeValueAsString(snapshot);
  DeckSnapshotDto deserialized = mapper.readValue(json, DeckSnapshotDto.class);

  assertEquals(snapshot.format(), deserialized.format());
  assertEquals(0, snapshot.totalPriceEur().compareTo(deserialized.totalPriceEur()));
  assertEquals(snapshot.capturedAt(), deserialized.capturedAt());
  assertEquals(1, deserialized.cards().size());
  ```
- **Resultado Esperado**: Jackson deserializa de forma transparente el record y reconstruye todas las colecciones inmutables sin pérdida de precisión numérica ni alteración temporal.

---

### 4. Mapeo a Formato de Error RFC 7807 (`ApiErrorResponse`)

Verifica que las excepciones de validación en controladores (`MethodArgumentNotValidException`) se transformen en la respuesta estándar `ApiErrorResponse`.

- **Payload simulado devuelto**:
  ```json
  {
    "type": "urn:problem:validation-error",
    "title": "Error de validación en la solicitud",
    "status": 400,
    "detail": "Uno o varios campos no superaron las reglas de validación",
    "instance": "/api/v1/admin/tournaments",
    "timestamp": "2026-11-01T16:00:00Z",
    "invalidParams": [
      {
        "field": "restrictions.maxBudgetEur",
        "rejectedValue": -10.0,
        "message": "El límite de presupuesto no puede ser negativo"
      }
    ]
  }
  ```
- **Resultado Esperado**: El cliente recibe un código HTTP 400 y una estructura predecible con el listado de parámetros rechazados.


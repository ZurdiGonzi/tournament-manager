---
name: spring-security-jwt-rbac
description: >-
  Use this skill when configuring or modifying authentication, JWT token generation
  and validation, stateless Spring Security 6 filter chains, or role-based access
  control (PUBLIC, PLAYER, ADMIN) in the backend.
---

# Spring Security 6 + Stateless JWT Authentication Skill

Guía para implementar y auditar la seguridad tipada y sin estado en **Tournament Manager**.

## 1. Arquitectura Stateless

1. **Política de Sesión**: Configura siempre `SessionCreationPolicy.STATELESS` y desactiva CSRF (`AbstractHttpConfigurer::disable`) para la API REST basada en cabeceras `Authorization: Bearer <token>`.
2. **CORS Tipado**: Define un bean `CorsConfigurationSource` explícito que permita únicamente los orígenes configurados en `application.yml` (`app.cors.allowed-origins`).

## 2. Segregación de Endpoints y Roles

Configura `authorizeHttpRequests` en `SecurityFilterChain` + `@EnableMethodSecurity`:

- **Endpoints Públicos (`/api/v1/public/**`, `/api/v1/auth/**`)**:
  - `permitAll()`
  - Consulta de torneos abiertos, rondas en vivo, brackets y standings públicos.
- **Endpoints de Jugador (`/api/v1/player/**`)**:
  - `hasAnyRole("PLAYER", "ADMIN")`
  - Inscripción/baja en torneos, registro y validación de mazo (decklist), check-in, envío de resultado de partida propia.
  - **Regla crítica**: Verifica siempre en la capa de servicio que el `userId` extraído del principal (`@AuthenticationPrincipal`) pertenezca realmente a la partida o inscripción que intenta modificar (prevención de IDOR).
- **Endpoints de Administrador (`/api/v1/admin/**`)**:
  - `hasRole("ADMIN")`
  - Creación de torneos y plantillas recurrentes, inicio/cierre manual de rondas, modificación de resultados por arbitraje (override de disputas), descalificación de jugadores (`DROP` / `DQ`).

## 3. Estructura del Token JWT

- **Claims mínimos**:
  - `sub`: UUID o identificador único del usuario.
  - `roles`: Lista de roles (`["ROLE_PLAYER"]` o `["ROLE_PLAYER", "ROLE_ADMIN"]`).
  - `iat` y `exp`: Tiempos de emisión y expiración cortos para el access token (ej. 15-60 min).
- **Clave de firma**:
  - Usa HMAC-SHA256 (`HS256` con mínimo 256 bits) o claves asimétricas RSA (`RS256`) inyectadas mediante variables de entorno (`JWT_SECRET`), nunca hardcodeadas en el repositorio.

## 4. Validación del Paso
- Crea tests con `@WebMvcTest` y `spring-security-test` (`@WithMockUser` o JWT mock) comprobando:
  - `401 Unauthorized` cuando un endpoint `/api/v1/player/**` se invoca sin token.
  - `403 Forbidden` cuando un usuario con `ROLE_PLAYER` invoca `/api/v1/admin/**`.
  - `200 OK` en `/api/v1/public/**` sin cabeceras de autenticación.


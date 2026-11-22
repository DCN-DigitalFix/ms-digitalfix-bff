# ms-digitalfix-bff

> **Backend for Frontend (BFF)** del sistema DigitalFix — gestión de órdenes de trabajo para empresas del rubro eléctrico.  
> Actúa como capa intermedia segura entre el frontend Angular y los microservicios de negocio, validando tokens JWT emitidos por **Microsoft Entra ID (Azure AD)**.

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Endpoints de la API](#endpoints-de-la-api)
- [Roles y Permisos](#roles-y-permisos)
- [Estados de una Orden de Trabajo](#estados-de-una-orden-de-trabajo)
- [Seguridad (OAuth 2.0 / JWT)](#seguridad-oauth-20--jwt)
- [Configuración](#configuración)
- [Requisitos Previos](#requisitos-previos)
- [Ejecutar el Proyecto](#ejecutar-el-proyecto)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Notas de Desarrollo](#notas-de-desarrollo)

---

## Descripción General

`ms-digitalfix-bff` es un **microservicio Spring Boot** que expone una API REST hacia el frontend Angular de DigitalFix. Sus responsabilidades principales son:

1. **Validar** los tokens Bearer emitidos por Microsoft Entra ID (firma, emisor, audiencia y expiración).
2. **Autorizar** cada endpoint según los *App Roles* del usuario y los *scopes* OAuth 2.0 consentidos.
3. **Filtrar** datos según el perfil del usuario (p.ej., un Cliente solo ve sus propias órdenes).
4. **Proporcionar** una interfaz estable al frontend; en el futuro delegará operaciones al microservicio `ms-digitalfix-workorders` mediante HTTP con propagación de token.

---

## Stack Tecnológico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 4.0.8 |
| Seguridad | Spring Security — OAuth 2.0 Resource Server | Incluido en Boot 4 |
| Validación | Spring Boot Validation (Jakarta Bean Validation) | Incluido en Boot 4 |
| Build | Apache Maven (wrapper incluido) | 3.x |
| Proveedor de identidad | Microsoft Entra ID (Azure AD) | v2.0 endpoints |

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    Cliente Angular (SPA)                    │
│              http://localhost:4200  |  prod: AWS            │
└────────────────────────┬────────────────────────────────────┘
                         │  Bearer <JWT>  (Entra ID v2.0)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   ms-digitalfix-bff                         │
│   :8080                                                     │
│                                                             │
│  ┌──────────────┐  ┌─────────────────┐  ┌───────────────┐  │
│  │ SecurityConfig│  │WorkOrderController│  │SessionCtrl   │  │
│  │ (JWT decode  │  │  /api/v1/workorders  │  │  /api/me     │  │
│  │  + CORS)     │  └────────┬──────────┘  └───────────────┘  │
│  └──────────────┘           │                                │
│                    ┌────────▼──────────┐                    │
│                    │  WorkOrderService  │                    │
│                    │ (in-memory / mock) │                    │
│                    └───────────────────┘                    │
└─────────────────────────────────────────────────────────────┘
                         │  (futuro: HTTP + Bearer forwarding)
                         ▼
              ms-digitalfix-workorders  (pendiente)
```

---

## Endpoints de la API

### Health

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| `GET` | `/api/health` | ❌ Pública | Sonda de vida del servicio |

### Sesión

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| `GET` | `/api/me` | ✅ JWT válido | Devuelve los claims del token: subject, nombre, roles, scopes, audiencia y expiración |

### Órdenes de Trabajo — `/api/v1/workorders`

| Método | Ruta | Rol requerido | Scope requerido | Descripción |
|--------|------|--------------|----------------|-------------|
| `GET` | `/api/v1/workorders` | Admin / Supervisor / Cliente / Auditor | `OT.Read` | Lista todas las OT (el Cliente solo ve las suyas) |
| `GET` | `/api/v1/workorders/{id}` | Admin / Supervisor / Cliente / Auditor | `OT.Read` | Obtiene una OT por ID |
| `POST` | `/api/v1/workorders` | Admin / Supervisor / Cliente | `OT.Create` | Crea una nueva OT |
| `PATCH` | `/api/v1/workorders/{id}/estado` | Admin / Supervisor | `OT.Create` | Cambia estado y/o técnico asignado |
| `DELETE` | `/api/v1/workorders/{id}` | Admin | `OT.Create` | Elimina una OT (operación irreversible) |

#### Ejemplo — Crear OT (`POST /api/v1/workorders`)

```json
{
  "cliente": "Condominio Los Pinos",
  "descripcion": "Falla en tablero general, sin luz en pisos 3-5",
  "direccion": "Av. Las Rosas 420, Pudahuel"
}
```

#### Ejemplo — Cambiar estado (`PATCH /api/v1/workorders/{id}/estado`)

```json
{
  "estado": "ASIGNADA",
  "tecnicoAsignado": "Cuadrilla Sur"
}
```

#### Ejemplo — Respuesta `WorkOrder`

```json
{
  "id": "OT-0001",
  "cliente": "Condominio Los Aromos",
  "descripcion": "Tablero general con corte intermitente en el bloque B",
  "direccion": "Av. Los Aromos 1420, Lampa",
  "estado": "ASIGNADA",
  "tecnicoAsignado": "Cuadrilla Norte",
  "creadaPor": "sistema",
  "creadaEn": "2026-09-14T10:00:00",
  "actualizadaEn": "2026-09-15T08:30:00"
}
```

---

## Roles y Permisos

Los roles son **App Roles** definidos en el registro de la aplicación en Entra ID y viajan en el claim `roles` del JWT.

| Rol | Listar OT | Ver OT | Crear OT | Cambiar Estado | Eliminar OT |
|-----|:---------:|:------:|:--------:|:--------------:|:-----------:|
| **Admin** | ✅ (todas) | ✅ | ✅ | ✅ | ✅ |
| **Supervisor** | ✅ (todas) | ✅ | ✅ | ✅ | ❌ |
| **Cliente** | ✅ (propias) | ✅ | ✅ | ❌ | ❌ |
| **Auditor** | ✅ (todas) | ✅ | ❌ | ❌ | ❌ |

> **Nota:** La autorización es conjuntiva: el usuario debe tener el **rol** correcto **y** el **scope** OAuth correspondiente en su token.

---

## Estados de una Orden de Trabajo

```
INGRESADA ──► ASIGNADA ──► EN_EJECUCION ──► CERRADA
                                │
                                └──► ANULADA
```

| Estado | Descripción |
|--------|-------------|
| `INGRESADA` | OT recién creada, sin técnico asignado |
| `ASIGNADA` | Técnico o cuadrilla designados |
| `EN_EJECUCION` | Trabajo en curso |
| `CERRADA` | Trabajo completado exitosamente |
| `ANULADA` | OT cancelada |

---

## Seguridad (OAuth 2.0 / JWT)

El BFF opera como **OAuth 2.0 Resource Server** y **no emite tokens**. La validación es completamente local contra las claves públicas (JWKS) de Entra ID:

| Validación | Detalle |
|-----------|---------|
| **Firma** | Descarga el JWKS desde el OIDC metadata del tenant |
| **Emisor (`iss`)** | `https://login.microsoftonline.com/<tenant-id>/v2.0` |
| **Audiencia (`aud`)** | Application ID GUID o `api://<guid>` (v1 y v2) |
| **Expiración (`exp`)** | Validada automáticamente por Spring |
| **Roles** | Claim `roles` → `ROLE_<nombre>` vía `JwtAuthoritiesConverter` |
| **Scopes** | Claim `scp` → `SCOPE_<nombre>` vía `JwtAuthoritiesConverter` |

**CORS** está configurado con orígenes explícitos (no wildcards):

- `http://localhost:4200` (desarrollo Angular)
- `https://44.206.195.117` (servidor de producción)

---

## Configuración

El archivo de configuración principal es [`src/main/resources/application.yaml`](src/main/resources/application.yaml):

```yaml
server:
  port: 8080

spring:
  application:
    name: ms-digitalfix-bff
  security:
    oauth2:
      resourceserver:
        jwt:
          # Tenant de la organización en Entra ID
          issuer-uri: https://login.microsoftonline.com/<tenant-id>/v2.0

digitalfix:
  security:
    audiences:
      - <application-id-guid>
      - api://<application-id-guid>
  cors:
    allowed-origins:
      - http://localhost:4200
      - https://<ip-o-dominio-produccion>
```

> ⚠️ **No versiones el `application.yaml` con valores reales** en ambientes compartidos. Usa variables de entorno o un config server.

---

## Requisitos Previos

| Requisito | Versión mínima |
|-----------|---------------|
| Java (JDK) | 17 |
| Maven | 3.8+ (o usa `./mvnw`) |
| Registro de app en Entra ID | Con App Roles y scopes delegados configurados |

---

## Ejecutar el Proyecto

### Usando el Maven Wrapper (recomendado)

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Compilar y ejecutar el JAR

```bash
# Compilar
./mvnw clean package -DskipTests

# Ejecutar
java -jar target/ms-digitalfix-bff-0.0.1-SNAPSHOT.jar
```

### Ejecutar pruebas

```bash
./mvnw test
```

El servidor arranca en **`http://localhost:8080`**.

---

## Estructura del Proyecto

```
ms-digitalfix-bff/
├── .github/
│   └── modernize/
│       └── java-upgrade/          # Hooks para actualización automatizada de Java
├── src/
│   ├── main/
│   │   ├── java/cl/duoc/ms_digitalfix_bff/
│   │   │   ├── MsDigitalfixBffApplication.java   # Entry point de Spring Boot
│   │   │   ├── config/
│   │   │   │   ├── AudienceValidator.java         # Validador del claim "aud"
│   │   │   │   ├── JwtAuthoritiesConverter.java   # Mapea roles/scopes a GrantedAuthority
│   │   │   │   ├── SecurityConfig.java            # Filtro de seguridad, CORS y JWT decoder
│   │   │   │   └── SecurityProperties.java        # Bindings de application.yaml
│   │   │   ├── controller/
│   │   │   │   ├── HealthController.java          # GET /api/health (público)
│   │   │   │   ├── SessionController.java         # GET /api/me (claims del token)
│   │   │   │   └── WorkOrderController.java       # CRUD /api/v1/workorders
│   │   │   ├── dto/
│   │   │   │   ├── CambiarEstadoRequest.java      # Body para PATCH /estado
│   │   │   │   ├── CrearWorkOrderRequest.java     # Body para POST /workorders
│   │   │   │   ├── EstadoOT.java                  # Enum: ciclo de vida de OT
│   │   │   │   └── WorkOrder.java                 # Record de respuesta (OT completa)
│   │   │   ├── exception/
│   │   │   │   ├── ApiError.java                  # Modelo de error uniforme
│   │   │   │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice centralizado
│   │   │   │   ├── JsonAccessDeniedHandler.java   # 403 en formato JSON
│   │   │   │   ├── JsonAuthenticationEntryPoint.java # 401 en formato JSON
│   │   │   │   └── NotFoundException.java         # Excepción de dominio (404)
│   │   │   └── service/
│   │   │       └── WorkOrderService.java          # Lógica de negocio (in-memory, provisional)
│   │   └── resources/
│   │       └── application.yaml                   # Configuración principal
│   └── test/
│       └── java/cl/duoc/ms_digitalfix_bff/
│           └── MsDigitalfixBffApplicationTests.java
├── .gitignore
├── mvnw / mvnw.cmd                                # Maven Wrapper
├── pom.xml                                        # Dependencias y build
└── README.md
```

---

## Notas de Desarrollo

### Almacenamiento en Memoria (Provisional)

`WorkOrderService` usa un `ConcurrentHashMap` como almacén en memoria. **Los datos se pierden al reiniciar el servidor.** Esto es intencional mientras el microservicio de negocio `ms-digitalfix-workorders` no esté disponible.

> Cuando ese servicio esté operativo, `WorkOrderService` se convierte en un cliente HTTP que reenvía las peticiones propagando el Bearer token. La interfaz pública del controlador **no cambia**.

### Manejo de Errores Uniforme

Todos los errores (401, 403, 404, 400, 500) devuelven el mismo formato JSON:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No existe la orden de trabajo OT-9999",
  "path": "/api/v1/workorders/OT-9999"
}
```

### Configuración de `requestedAccessTokenVersion`

Para que el claim `aud` llegue como GUID simple (en lugar de `api://<guid>`), el manifiesto del registro de la API en Entra ID debe tener `requestedAccessTokenVersion = 2`. El BFF acepta **ambas** formas por compatibilidad.

---

*Proyecto académico — Duoc UC · DigitalFix*

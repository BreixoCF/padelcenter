# ADR-002: API-first with OpenAPI as source of truth

## Status

Accepted | 2026-04-16

## Context

El enfoque inicial era **code-first**: los endpoints se definían directamente
en los controladores con anotaciones Spring MVC y SpringDoc generaba la
documentación OpenAPI a partir del código.

Esto producía problemas recurrentes:

- El contrato de la API quedaba implícito en el código Java. Cambiar un
  nombre de campo o un tipo requería buscar todos los usos en lugar de
  editar un único fichero de especificación.
- Los DTOs de request/response se escribían manualmente y podían
  desincronizarse con lo que realmente devolvía el endpoint.
- No había un artefacto versionable que representara el contrato acordado
  con los consumidores (frontend, clientes móviles, equipos externos).
- La proliferación de DTOs similares (CreateBookingResponse, BookingHistoryResponse,
  UpdateBookingResponse) reflejaba la falta de diseño previo del contrato.

## Decision

Se adopta **API-first con OpenAPI 3.0.3** como fuente de verdad del contrato HTTP.

### Flujo de trabajo

```
1. Definir / modificar el contrato en docs/openapi/index.yaml
2. Ejecutar: mvn generate-sources
3. Implementar el nuevo método en el @RestController correspondiente
```

Nunca al revés: ningún endpoint existe si no está en el YAML.

### Herramientas

| Herramienta | Rol |
|---|---|
| `docs/openapi/index.yaml` | Especificación principal (usa `$ref` a paths/ y components/) |
| `openapi-generator-maven-plugin 7.6.0` | Genera interfaces Java y DTOs en `target/generated-sources/` |
| Generador `spring` con `interfaceOnly=true` | Produce interfaces que los controladores implementan |
| `skipDefaultInterface=true` | Evita la dependencia de `ApiUtil` en los métodos default |
| `useEnumCaseInsensitive=true` | Permite deserializar enums en mayúsculas/minúsculas |
| `dateLibrary=java8` | Usa `OffsetDateTime` para fechas con zona horaria |

### Estructura del contrato

```
docs/openapi/
├── index.yaml              # Entry point (info, servers, security, $refs)
├── paths/
│   ├── users.yaml
│   ├── centers.yaml
│   ├── fields.yaml
│   └── bookings.yaml
└── components/
    ├── schemas/            # DTOs: requests, responses, shared models
    └── securitySchemes/    # bearerAuth (JWT)
```

### Convención de DTOs

- Los DTOs generados (en `com.padelcenter.infrastructure.web.generated.model`)
  son **exclusivos de la capa web**. Nunca se pasan a la capa de aplicación.
- Los `*ApiMapper` convierten entre DTOs generados y comandos/queries de dominio.
- Los modelos de dominio (`domain/model`) son records Java inmutables independientes
  de la representación HTTP.

## Consequences

### Positivas

- **Contrato versionado en el repositorio**: cualquier cambio en la API
  es un diff en el YAML, visible en la revisión de código.
- **DTOs siempre alineados**: se generan automáticamente; es imposible
  que el código y la documentación difieran.
- **Consistencia entre operaciones**: un mismo tipo de respuesta
  (`BookingResponse`) se reutiliza en create, update, cancel y get,
  eliminando la proliferación de DTOs redundantes.
- **Swagger UI integrado**: `springdoc-openapi` sirve la UI en
  `/swagger-ui.html` sin configuración adicional.
- **Independencia de cliente**: cualquier consumidor puede generar
  su cliente a partir del YAML con el generador de su lenguaje.

### Negativas

- **Ciclo generate-sources obligatorio**: tras modificar el YAML hay que
  regenerar antes de compilar. En IDEs sin build automático puede provocar
  confusión inicial.
- **Código generado no editable**: los DTOs en `target/generated-sources/`
  no se modifican a mano; toda customización va en el YAML o en mappers.
- **Curva inicial del YAML**: escribir OpenAPI complejo (oneOf, discriminadores,
  referencias anidadas) tiene una curva de aprendizaje mayor que anotar un
  método Java.

## Alternatives considered

### SpringDoc code-first (anotaciones en controladores)

Más rápido de arrancar y familiar para la mayoría de los equipos Spring.
Descartado porque el contrato queda disperso en anotaciones Java, los DTOs
se pueden desincronizar silenciosamente, y no hay un único fichero que
represente la API completa.

### Contratos manuales sin generación de código

Mantener el YAML como documentación y los DTOs escritos a mano por separado.
Descartado porque duplica el mantenimiento: cualquier cambio en el contrato
requiere actualizar el YAML y los DTOs de forma manual y sincronizada,
lo que recupera el problema original de desincronización.

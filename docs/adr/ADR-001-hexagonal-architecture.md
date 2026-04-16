# ADR-001: Hexagonal Architecture as primary structural pattern

## Status

Accepted | 2026-04-16

## Context

El punto de partida del proyecto era una arquitectura en capas clásica
(Controller → Service → Repository) donde la lógica de negocio quedaba
dispersa entre servicios anémicos, entidades JPA con comportamiento, y
controladores que mezclaban validación HTTP con reglas de dominio.

Los problemas concretos que esto generaba:

- Los tests de servicio requerían Spring context o mocks de JPA, lo que
  hacía el feedback lento y frágil.
- Cambiar de base de datos o de framework web habría requerido tocar la
  lógica de negocio.
- Los modelos JPA (`@Entity`) se exponían directamente en los endpoints,
  acoplando la representación HTTP al esquema de base de datos.
- No había una frontera clara entre "qué hace el sistema" y "cómo lo hace".

## Decision

Se adopta **Hexagonal Architecture** (Ports & Adapters) como patrón estructural
principal, organizada en tres capas:

```
domain/          # Modelo de negocio puro — sin dependencias de framework
application/     # Casos de uso — orquestan el dominio, dependen solo de ports
infrastructure/  # Adaptadores — JPA, REST, Security, configuración Spring
```

### Reglas de dependencia

- `domain` no importa nada de `infrastructure` ni de Spring.
- `application` depende solo de `domain` (interfaces de repositorio como ports).
- `infrastructure` implementa los ports y depende de `application` y `domain`.
- Los controladores invocan únicamente casos de uso; nunca acceden a repositorios.

### Elementos clave

| Elemento | Ubicación | Descripción |
|---|---|---|
| Entidades / Value Objects | `domain/model` | Records Java inmutables, sin Spring |
| Ports de entrada | `application/shared` | Interfaces `CommandUseCase`, `QueryUseCase` |
| Ports de salida | `domain/repository` | Interfaces de repositorio sin JPA |
| Use Cases | `application/{create,read,update,delete}` | Un caso de uso por clase |
| Adaptadores de entrada | `infrastructure/inbound/web` | Controladores REST |
| Adaptadores de salida | `infrastructure/outbound/db` | Repositorios JPA |
| Configuración | `infrastructure/config` | Beans de Spring (Security, MVC) |

### Cumplimiento automatizado

Las reglas de capas se verifican en cada build mediante **ArchUnit**
(`ArchitectureTest`), con cinco reglas activas:

1. `domain` no depende de `infrastructure` ni Spring.
2. `application` no depende de `infrastructure`.
3. Controladores no acceden a repositorios directamente.
4. La capa web no importa clases `@Entity`.
5. La capa de aplicación no usa `@Autowired` en campos.

## Consequences

### Positivas

- **Testabilidad**: los casos de uso se testean con Mockito puro, sin
  Spring context. El feedback es inmediato (<2 s por suite de unit tests).
- **Independencia de infraestructura**: cambiar de PostgreSQL a otro motor,
  o de REST a GraphQL, no afecta al dominio ni a los casos de uso.
- **Claridad de responsabilidades**: cada clase tiene una única razón para
  cambiar; la lógica de negocio no se filtra a controladores ni entidades JPA.
- **Contrato explícito**: los ports documentan lo que el dominio necesita
  de la infraestructura, no al revés.

### Negativas

- **Boilerplate de mappers**: cada capa tiene sus propios DTOs/modelos y
  requiere clases de mapeo (ApiMapper, PersistenceMapper). Para entidades
  simples, esto supone código repetitivo.
- **Curva de aprendizaje**: desarrolladores acostumbrados a Spring MVC
  clásico necesitan entender la separación de capas antes de contribuir.
- **Más ficheros**: un caso de uso = un fichero. Para proyectos pequeños,
  la estructura puede parecer sobredimensionada al inicio.

## Alternatives considered

### Layered Architecture (Controller → Service → Repository)

Más familiar y con menos boilerplate. Descartada porque acopla la lógica
de negocio a Spring y a JPA, haciendo los tests lentos y la migración de
infraestructura costosa.

### CQRS completo con event sourcing

Mayor separación lectura/escritura y trazabilidad total de cambios.
Descartado por complejidad operacional desproporcionada para el tamaño
actual del proyecto. La arquitectura hexagonal actual está preparada para
evolucionar hacia CQRS si el volumen lo justifica.

# Logging — configuración y ejemplos

## Por perfil

| Perfil | Appender | Salida |
|--------|----------|--------|
| `local` / `test` | `CONSOLE_COLOR` | Texto plano con colores ANSI |
| _(default, sin perfil `local`)_ | `CONSOLE_JSON` | JSON estructurado (NDJSON), vía `LogstashEncoder` |

```bash
# Local (colores)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# JSON (perfil por defecto)
java -jar target/padelcenter-*.jar
```

### Formato local — converters de Logback

`logback-spring.xml` registra dos conversion rules de Spring Boot usadas en el pattern del appender `CONSOLE_COLOR`:

```xml
<conversionRule conversionWord="clr" converterClass="org.springframework.boot.logging.logback.ColorConverter"/>
<conversionRule conversionWord="wEx" converterClass="org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter"/>
```

- **`%clr(texto){color}`** — añade color ANSI; detecta automáticamente si la salida no es una TTY y lo desactiva. Colores disponibles: `{faint}`, `{highlight}` (color según nivel), `{cyan}`, `{yellow}`, `{red}`, etc.
- **`%wEx`** — indenta los stack traces para que sean legibles en consola, en vez de una sola línea continua.

Si estos converters no están registrados, Logback falla al arrancar con
`Unknown word for conversion [clr]` / `[wEx]`.

```
11:25:00.125  INFO --- [http-nio-8080-exec-1]  c.b.p.a.c.CreateUserUseCase : user.created userId=... email=...
```

### Formato JSON (perfil por defecto)

```json
{"timestamp":"2026-04-16T11:25:00.125Z","level":"INFO","logger":"com.bookings.padelcenter.application.create.CreateUserUseCase","message":"user.created userId=550e8400-e29b-41d4-a716-446655440001 email=john@example.com","thread":"http-nio-8080-exec-1","traceId":"550e8400-e29b-41d4-a716-446655440000","requestId":"7c8d9e0f-1a2b-3c4d-5e6f-7a8b9c0d1e2f","httpMethod":"POST","httpUri":"/api/v1/users","app":"padelcenter"}
```

Campos: `timestamp` (ISO 8601), `level`, `logger`, `message`, `thread`, `traceId`,
`requestId`, `httpMethod`/`httpUri` (request en curso), `app`.

## Nivel INFO en producción — solo eventos de negocio

`application-local.yaml` sube a `WARN` el ruido de framework (`org.hibernate.SQL`,
`DispatcherServlet`, `Spring Security`, etc.), de forma que cada request exitoso
genera **una sola línea** de log con el evento de negocio relevante
(`user.created`, `booking.created`, `booking.cancel.failed`...), no las ~20 líneas
de DEBUG (queries SQL, bindings, filtros de seguridad) que generaría por defecto.

## Propagación de `traceId`

`MdcFilter` extrae el header `X-Trace-Id` de la request entrante (o genera un UUID
si no viene), lo añade al MDC para que aparezca en todos los logs de ese request,
y lo devuelve en la respuesta con el mismo header — permite correlacionar logs de
un mismo request a través de varios servicios.

```bash
curl -H "X-Trace-Id: 550e8400-e29b-41d4-a716-446655440000" \
     -H "Authorization: Bearer ..." \
     http://localhost:8080/api/v1/users
```

## Ingestión en ELK / Grafana Loki

**Promtail → Loki:**

```yaml
clients:
  - url: http://loki:3100/loki/api/v1/push
scrape_configs:
  - job_name: padelcenter
    static_configs:
      - targets: [localhost]
        labels: { app: padelcenter, job: spring-logs }
    pipeline_stages:
      - json:
          expressions: { timestamp: timestamp, level: level, logger: logger, message: message, traceId: traceId, app: app }
      - labels: { level:, traceId:, app: }
```

```logql
{app="padelcenter"} | json | level="INFO"
{app="padelcenter", traceId="550e8400-e29b-41d4-a716-446655440000"}
```

**Logstash → Elasticsearch:**

```conf
input { stdin { codec => "json" } }
filter { date { match => [ "timestamp", "ISO8601" ] } }
output { elasticsearch { hosts => ["elasticsearch:9200"] index => "padelcenter-%{+YYYY.MM.dd}" } }
```

## Seguridad

Datos sensibles que nunca se loguean:

- `Authorization: Bearer ...` — `CommonsRequestLoggingFilter` con `setIncludeHeaders(false)`
- Request/response body (contraseñas, etc.) — `setIncludePayload(false)`

Solo se loguean `email`, `userId`, `traceId` y parámetros funcionales no sensibles.

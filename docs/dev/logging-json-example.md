# Logging JSON Estructurado — Ejemplos

## Configuración Local

Cuando ejecutas:
```bash
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Los logs aparecen en **consola en formato JSON estructurado**, listo para ingestión en ELK Stack o Grafana Loki.

---

## Ejemplos de Logs

### 1. Request entrante (MDC + CommonsRequestLoggingFilter)

```json
{
  "timestamp": "2026-04-16T11:20:15.234Z",
  "level": "DEBUG",
  "logger": "org.springframework.web.filter.CommonsRequestLoggingFilter",
  "message": "HTTP REQUEST: method=POST uri=/api/v1/users params=[]",
  "thread": "http-nio-8080-exec-1",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "7c8d9e0f-1a2b-3c4d-5e6f-7a8b9c0d1e2f",
  "httpMethod": "POST",
  "httpUri": "/api/v1/users",
  "app": "padelcenter",
  "env": "local"
}
```

### 2. Use case: CreateUser (DEBUG entry)

```json
{
  "timestamp": "2026-04-16T11:20:15.245Z",
  "level": "DEBUG",
  "logger": "com.bookings.padelcenter.application.create.CreateUserUseCase",
  "message": "user.create.start email=john@example.com firstName=John lastName=Doe",
  "thread": "http-nio-8080-exec-1",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "7c8d9e0f-1a2b-3c4d-5e6f-7a8b9c0d1e2f",
  "httpMethod": "POST",
  "httpUri": "/api/v1/users",
  "app": "padelcenter",
  "env": "local"
}
```

### 3. Use case: CreateUser (INFO success)

```json
{
  "timestamp": "2026-04-16T11:20:15.265Z",
  "level": "INFO",
  "logger": "com.bookings.padelcenter.application.create.CreateUserUseCase",
  "message": "user.created userId=550e8400-e29b-41d4-a716-446655440001 email=john@example.com firstName=John lastName=Doe",
  "thread": "http-nio-8080-exec-1",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "7c8d9e0f-1a2b-3c4d-5e6f-7a8b9c0d1e2f",
  "httpMethod": "POST",
  "httpUri": "/api/v1/users",
  "app": "padelcenter",
  "env": "local"
}
```

### 4. Use case: CreateBooking (complete flow)

```json
{
  "timestamp": "2026-04-16T11:20:16.100Z",
  "level": "DEBUG",
  "logger": "com.bookings.padelcenter.application.create.CreateBookingUseCase",
  "message": "booking.create.start fieldId=f47ac10b-58cc-4372-a567-0e02b2c3d479 userId=550e8400-e29b-41d4-a716-446655440001 start=2026-04-18T10:00:00 end=2026-04-18T11:00:00",
  "thread": "http-nio-8080-exec-2",
  "traceId": "550e8400-e29b-41d4-a716-446655440002",
  "requestId": "8d9e0f1a-2b3c-4d5e-6f7a-8b9c0d1e2f3a",
  "httpMethod": "POST",
  "httpUri": "/api/v1/users/550e8400-e29b-41d4-a716-446655440001/bookings",
  "app": "padelcenter",
  "env": "local"
}
```

```json
{
  "timestamp": "2026-04-16T11:20:16.110Z",
  "level": "DEBUG",
  "logger": "com.bookings.padelcenter.application.create.CreateBookingUseCase",
  "message": "booking.create.field.found fieldId=f47ac10b-58cc-4372-a567-0e02b2c3d479 available=true",
  "thread": "http-nio-8080-exec-2",
  "traceId": "550e8400-e29b-41d4-a716-446655440002",
  "requestId": "8d9e0f1a-2b3c-4d5e-6f7a-8b9c0d1e2f3a",
  "httpMethod": "POST",
  "httpUri": "/api/v1/users/550e8400-e29b-41d4-a716-446655440001/bookings",
  "app": "padelcenter",
  "env": "local"
}
```

```json
{
  "timestamp": "2026-04-16T11:20:16.125Z",
  "level": "INFO",
  "logger": "com.bookings.padelcenter.application.create.CreateBookingUseCase",
  "message": "booking.created bookingId=12345 fieldId=f47ac10b-58cc-4372-a567-0e02b2c3d479 userId=550e8400-e29b-41d4-a716-446655440001 totalPrice=50.00",
  "thread": "http-nio-8080-exec-2",
  "traceId": "550e8400-e29b-41d4-a716-446655440002",
  "requestId": "8d9e0f1a-2b3c-4d5e-6f7a-8b9c0d1e2f3a",
  "httpMethod": "POST",
  "httpUri": "/api/v1/users/550e8400-e29b-41d4-a716-446655440001/bookings",
  "app": "padelcenter",
  "env": "local"
}
```

### 5. Use case: UpdatePassword (WARN on failure)

```json
{
  "timestamp": "2026-04-16T11:20:17.200Z",
  "level": "WARN",
  "logger": "com.bookings.padelcenter.application.update.UpdatePasswordUseCase",
  "message": "user.password.update.failed userId=550e8400-e29b-41d4-a716-446655440001 reason=invalid_current_password",
  "thread": "http-nio-8080-exec-3",
  "traceId": "550e8400-e29b-41d4-a716-446655440003",
  "requestId": "9e0f1a2b-3c4d-5e6f-7a8b-9c0d1e2f3a4b",
  "httpMethod": "PATCH",
  "httpUri": "/api/v1/users/550e8400-e29b-41d4-a716-446655440001/password",
  "app": "padelcenter",
  "env": "local"
}
```

### 6. Use case: CancelBooking (WARN on conflict)

```json
{
  "timestamp": "2026-04-16T11:20:18.300Z",
  "level": "WARN",
  "logger": "com.bookings.padelcenter.application.update.CancelBookingUseCase",
  "message": "booking.cancel.failed bookingId=12346 reason=already_cancelled",
  "thread": "http-nio-8080-exec-4",
  "traceId": "550e8400-e29b-41d4-a716-446655440004",
  "requestId": "0f1a2b3c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "httpMethod": "DELETE",
  "httpUri": "/api/v1/bookings/12346",
  "app": "padelcenter",
  "env": "local"
}
```

### 7. Query: GetAllUsers (pagination)

```json
{
  "timestamp": "2026-04-16T11:20:19.400Z",
  "level": "DEBUG",
  "logger": "com.bookings.padelcenter.application.read.GetAllUsersUseCase",
  "message": "users.list.start page=0 size=20",
  "thread": "http-nio-8080-exec-5",
  "traceId": "550e8400-e29b-41d4-a716-446655440005",
  "requestId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "httpMethod": "GET",
  "httpUri": "/api/v1/users",
  "app": "padelcenter",
  "env": "local"
}
```

```json
{
  "timestamp": "2026-04-16T11:20:19.420Z",
  "level": "DEBUG",
  "logger": "com.bookings.padelcenter.application.read.GetAllUsersUseCase",
  "message": "users.list.found count=15 totalPages=1",
  "thread": "http-nio-8080-exec-5",
  "traceId": "550e8400-e29b-41d4-a716-446655440005",
  "requestId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "httpMethod": "GET",
  "httpUri": "/api/v1/users",
  "app": "padelcenter",
  "env": "local"
}
```

### 8. SQL Query (Hibernate DEBUG)

```json
{
  "timestamp": "2026-04-16T11:20:19.435Z",
  "level": "DEBUG",
  "logger": "org.hibernate.SQL",
  "message": "select user0_.user_id, user0_.first_name, user0_.last_name, user0_.email, ... from users user0_ where user0_.deleted_at is null limit ? offset ?",
  "thread": "http-nio-8080-exec-5",
  "traceId": "550e8400-e29b-41d4-a716-446655440005",
  "requestId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "app": "padelcenter",
  "env": "local"
}
```

### 9. SQL Parameters (Hibernate TRACE)

```json
{
  "timestamp": "2026-04-16T11:20:19.440Z",
  "level": "TRACE",
  "logger": "org.hibernate.orm.jdbc.bind",
  "message": "binding parameter [1] as [INTEGER] - [20], binding parameter [2] as [INTEGER] - [0]",
  "thread": "http-nio-8080-exec-5",
  "traceId": "550e8400-e29b-41d4-a716-446655440005",
  "requestId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "app": "padelcenter",
  "env": "local"
}
```

### 10. Response (CommonsRequestLoggingFilter)

```json
{
  "timestamp": "2026-04-16T11:20:19.450Z",
  "level": "DEBUG",
  "logger": "org.springframework.web.filter.CommonsRequestLoggingFilter",
  "message": "HTTP RESPONSE: status=200, contentType=application/json",
  "thread": "http-nio-8080-exec-5",
  "traceId": "550e8400-e29b-41d4-a716-446655440005",
  "requestId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "httpMethod": "GET",
  "httpUri": "/api/v1/users",
  "app": "padelcenter",
  "env": "local"
}
```

---

## Propagación de traceId

En un entorno de microservicios con API Gateway:

```bash
curl -H "X-Trace-Id: 550e8400-e29b-41d4-a716-446655440000" \
     -H "Authorization: Bearer ..." \
     http://localhost:8080/api/v1/users
```

- El `X-Trace-Id` es **extraído por MdcFilter**
- Se añade a **todos los logs del request** (MDC)
- Se devuelve en la **respuesta** con el header `X-Trace-Id`
- Permite **correlacionar logs de múltiples servicios**

Si no viene `X-Trace-Id`, se **genera un UUID aleatorio**.

---

## Ingestión en ELK/Loki

### Logstash → Elasticsearch

```conf
input {
  stdin { codec => "json" }
}

filter {
  date {
    match => [ "timestamp", "ISO8601" ]
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "padelcenter-%{+YYYY.MM.dd}"
  }
}
```

### Promtail → Grafana Loki

```yaml
clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: padelcenter
    static_configs:
      - targets:
          - localhost
        labels:
          app: padelcenter
          job: spring-logs
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            logger: logger
            message: message
            traceId: traceId
            requestId: requestId
            app: app
      - labels:
          level:
          traceId:
          app:
```

Luego en Grafana:

```logql
{app="padelcenter"} | json | level="INFO"
{app="padelcenter", traceId="550e8400-e29b-41d4-a716-446655440000"}
{logger="com.bookings.padelcenter.application.create.CreateUserUseCase"}
```

---

## Niveles y Patrones

| Nivel | Caso de uso | Ejemplo |
|-------|-----------|---------|
| **TRACE** | Bindings SQL muy detallado | `org.hibernate.orm.jdbc.bind` |
| **DEBUG** | Flow de use cases, queries | `booking.create.start`, `users.list.found` |
| **INFO** | Eventos de negocio | `user.created`, `booking.cancelled`, `user.password.updated` |
| **WARN** | Errores recuperables | `user.password.update.failed`, `booking.cancel.failed` |
| **ERROR** | Excepciones no manejadas | `GlobalExceptionHandler` (no explícito aquí, pero capturado) |

---

## Seguridad

⚠️ **Datos sensibles nunca se loguean:**
- ❌ `Authorization: Bearer ...` — bloqueado (`setIncludeHeaders(false)`)
- ❌ Request body con contraseña — bloqueado (`setIncludePayload(false)`)
- ❌ Response body — bloqueado (solo headers)
- ✅ `email`, `userId`, `traceId`, métodos y parámetros funcionales

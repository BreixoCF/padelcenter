# Ejemplo de Logging Reducido — INFO Business Events Only

Con la nueva configuración (INFO level sin queries SQL ni framework debug):

## Flujo Completo: Crear Usuario

### 1. Request POST /api/v1/users

**Input:**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGc..." \
  -d '{
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "SecurePass123!"
  }'
```

### 2. Logs Generados (Consola)

```json
{
  "timestamp": "2026-04-16T11:25:00.125Z",
  "level": "INFO",
  "logger": "com.bookings.padelcenter.application.create.CreateUserUseCase",
  "message": "user.created userId=550e8400-e29b-41d4-a716-446655440001 email=john@example.com firstName=John lastName=Doe",
  "thread": "http-nio-8080-exec-1",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "requestId": "7c8d9e0f-1a2b-3c4d-5e6f-7a8b9c0d1e2f",
  "app": "padelcenter",
  "env": "local"
}
```

✅ **Solo 1 línea de log** — el evento de negocio importante
❌ No hay queries SQL
❌ No hay Spring Security debug
❌ No hay DispatcherServlet
❌ No hay caller data

---

## Flujo: Crear Reserva

### 1. Request POST /api/v1/users/{userId}/bookings

### 2. Logs Generados

```json
{
  "timestamp": "2026-04-16T11:25:05.200Z",
  "level": "INFO",
  "logger": "com.bookings.padelcenter.application.create.CreateBookingUseCase",
  "message": "booking.created bookingId=12345 fieldId=f47ac10b-58cc-4372-a567-0e02b2c3d479 userId=550e8400-e29b-41d4-a716-446655440001 totalPrice=50.00",
  "thread": "http-nio-8080-exec-2",
  "traceId": "550e8400-e29b-41d4-a716-446655440002",
  "requestId": "8d9e0f1a-2b3c-4d5e-6f7a-8b9c0d1e2f3a",
  "app": "padelcenter",
  "env": "local"
}
```

✅ **Solo 1 línea** — evento de creación
❌ Sin query de lectura de usuario
❌ Sin query de lectura de field
❌ Sin query de inserción de booking
❌ Sin validación SQL

---

## Flujo: Error Recuperable (Password Inválida)

### 1. Request PATCH /api/v1/users/{userId}/password

```bash
curl -X PATCH http://localhost:8080/api/v1/users/550e8400.../password \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "WrongPassword",
    "newPassword": "NewPass123!"
  }'
```

### 2. Logs Generados

```json
{
  "timestamp": "2026-04-16T11:25:10.300Z",
  "level": "WARN",
  "logger": "com.bookings.padelcenter.application.update.UpdatePasswordUseCase",
  "message": "user.password.update.failed userId=550e8400-e29b-41d4-a716-446655440001 reason=invalid_current_password",
  "thread": "http-nio-8080-exec-3",
  "traceId": "550e8400-e29b-41d4-a716-446655440003",
  "requestId": "9e0f1a2b-3c4d-5e6f-7a8b-9c0d1e2f3a4b",
  "app": "padelcenter",
  "env": "local"
}
```

✅ **Solo 1 línea de WARN** — error recuperable
✅ Sin query SQL de lectura de usuario
✅ Sin comparación de hash (detalles internos)

---

## Flujo: Listar Usuarios (Paginado)

### 1. Request GET /api/v1/users?page=0&size=20

### 2. Logs Generados

```json
{
  "timestamp": "2026-04-16T11:25:15.400Z",
  "level": "INFO",
  "logger": "com.bookings.padelcenter.application.read.GetAllUsersUseCase",
  "message": "users.list.found count=15 totalPages=1",
  "thread": "http-nio-8080-exec-4",
  "traceId": "550e8400-e29b-41d4-a716-446655440004",
  "requestId": "0f1a2b3c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
  "app": "padelcenter",
  "env": "local"
}
```

✅ **Solo 1 línea** — resultado de la query
❌ Sin "SELECT * FROM users WHERE deleted_at IS NULL LIMIT 20 OFFSET 0"
❌ Sin parámetros de binding (limit=20, offset=0)
❌ Sin Spring Web request/response debug

---

## Flujo: Cancelar Reserva (ya Cancelada)

### 1. Request DELETE /api/v1/bookings/{bookingId}

### 2. Logs Generados

```json
{
  "timestamp": "2026-04-16T11:25:20.500Z",
  "level": "WARN",
  "logger": "com.bookings.padelcenter.application.update.CancelBookingUseCase",
  "message": "booking.cancel.failed bookingId=12346 reason=already_cancelled",
  "thread": "http-nio-8080-exec-5",
  "traceId": "550e8400-e29b-41d4-a716-446655440005",
  "requestId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "app": "padelcenter",
  "env": "local"
}
```

✅ **Solo 1 línea de WARN** — conflicto de negocio
❌ Sin query de lectura de booking
❌ Sin estado de la reserva antes/después

---

## Flujo: Error No Controlado (Usuario no Encontrado)

### 1. Request GET /api/v1/users/invalid-uuid

### 2. Logs Generados

```json
{
  "timestamp": "2026-04-16T11:25:25.600Z",
  "level": "WARN",
  "logger": "org.springframework.security",
  "message": "Invalid JWT token",
  "level": "WARN",
  "thread": "http-nio-8080-exec-6",
  "traceId": "550e8400-e29b-41d4-a716-446655440006",
  "requestId": "2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "app": "padelcenter",
  "env": "local"
}
```

❌ O si es 404 NotFound:

```json
{
  "timestamp": "2026-04-16T11:25:25.610Z",
  "level": "WARN",
  "logger": "org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver",
  "message": "Resolved [com.bookings.padelcenter.domain.exception.UserNotFoundException]",
  "thread": "http-nio-8080-exec-6",
  "traceId": "550e8400-e29b-41d4-a716-446655440006",
  "requestId": "2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "app": "padelcenter",
  "env": "local"
}
```

✅ **Solo warnings del framework**
❌ Sin query de búsqueda
❌ Sin detalles de resolución interna

---

## Comparación: Antes vs Después

| Aspecto | Antes (DEBUG) | Después (INFO) |
|---------|---------------|----------------|
| Logs por request exitoso | ~20+ líneas | 1 línea |
| SQL queries | ✅ Visibles | ❌ Ocultas (WARN only) |
| Spring Security | ✅ DEBUG completo | ❌ Solo WARN |
| Spring Web | ✅ DEBUG completo | ❌ Solo WARN |
| Eventos de negocio | ✅ Visibles | ✅ Visibles (INFO) |
| Warnings | ✅ Visibles | ✅ Visibles (WARN) |
| Errores | ✅ Visibles | ✅ Visibles (ERROR) |
| Tamaño de log | Grande | Pequeño |
| Rendimiento | Impactado | Minimal |

---

## Output Total en Consola (1 minuto de operaciones)

```json
{"timestamp":"2026-04-16T11:25:00.125Z","level":"INFO","logger":"com.bookings.padelcenter.application.create.CreateUserUseCase","message":"user.created userId=550e8400-e29b-41d4-a716-446655440001 email=john@example.com firstName=John lastName=Doe","thread":"http-nio-8080-exec-1","traceId":"550e8400-e29b-41d4-a716-446655440000","requestId":"7c8d9e0f-1a2b-3c4d-5e6f-7a8b9c0d1e2f","app":"padelcenter","env":"local"}
{"timestamp":"2026-04-16T11:25:05.200Z","level":"INFO","logger":"com.bookings.padelcenter.application.create.CreateBookingUseCase","message":"booking.created bookingId=12345 fieldId=f47ac10b-58cc-4372-a567-0e02b2c3d479 userId=550e8400-e29b-41d4-a716-446655440001 totalPrice=50.00","thread":"http-nio-8080-exec-2","traceId":"550e8400-e29b-41d4-a716-446655440002","requestId":"8d9e0f1a-2b3c-4d5e-6f7a-8b9c0d1e2f3a","app":"padelcenter","env":"local"}
{"timestamp":"2026-04-16T11:25:10.300Z","level":"WARN","logger":"com.bookings.padelcenter.application.update.UpdatePasswordUseCase","message":"user.password.update.failed userId=550e8400-e29b-41d4-a716-446655440001 reason=invalid_current_password","thread":"http-nio-8080-exec-3","traceId":"550e8400-e29b-41d4-a716-446655440003","requestId":"9e0f1a2b-3c4d-5e6f-7a8b-9c0d1e2f3a4b","app":"padelcenter","env":"local"}
{"timestamp":"2026-04-16T11:25:15.400Z","level":"INFO","logger":"com.bookings.padelcenter.application.read.GetAllUsersUseCase","message":"users.list.found count=15 totalPages=1","thread":"http-nio-8080-exec-4","traceId":"550e8400-e29b-41d4-a716-446655440004","requestId":"0f1a2b3c-4d5e-6f7a-8b9c-0d1e2f3a4b5c","app":"padelcenter","env":"local"}
{"timestamp":"2026-04-16T11:25:20.500Z","level":"WARN","logger":"com.bookings.padelcenter.application.update.CancelBookingUseCase","message":"booking.cancel.failed bookingId=12346 reason=already_cancelled","thread":"http-nio-8080-exec-5","traceId":"550e8400-e29b-41d4-a716-446655440005","requestId":"1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d","app":"padelcenter","env":"local"}
```

✅ **Limpio, conciso, solo lo importante**
✅ Cada línea es un evento relevante
✅ Sin ruido de framework
✅ Perfecto para ELK/Loki

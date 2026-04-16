# Logging Formats — Local vs Production

## Perfil LOCAL: Texto Plano con Colores

**Comando:**
```bash
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Output en consola (con colores ANSI):**

```
11:25:00.125  INFO --- [http-nio-8080-exec-1]  c.b.p.a.c.CreateUserUseCase        : user.created userId=550e8400-e29b-41d4-a716-446655440001 email=john@example.com firstName=John lastName=Doe
11:25:05.200  INFO --- [http-nio-8080-exec-2]  c.b.p.a.c.CreateBookingUseCase     : booking.created bookingId=12345 fieldId=f47ac10b-58cc-4372-a567-0e02b2c3d479 userId=550e8400-e29b-41d4-a716-446655440001 totalPrice=50.00
11:25:10.300  WARN --- [http-nio-8080-exec-3]  c.b.p.a.u.UpdatePasswordUseCase    : user.password.update.failed userId=550e8400-e29b-41d4-a716-446655440001 reason=invalid_current_password
11:25:15.400  INFO --- [http-nio-8080-exec-4]  c.b.p.a.r.GetAllUsersUseCase       : users.list.found count=15 totalPages=1
11:25:20.500  WARN --- [http-nio-8080-exec-5]  c.b.p.a.u.CancelBookingUseCase     : booking.cancel.failed bookingId=12346 reason=already_cancelled
```

**Colores (ANSI):**
- ⏰ **Timestamp** (gris claro): `11:25:00.125`
- 📊 **Level** (coloreado):
  - 🟢 INFO (verde)
  - 🟡 WARN (amarillo)
  - 🔴 ERROR (rojo)
  - 🔵 DEBUG (azul)
- 🧵 **Thread** (gris claro): `[http-nio-8080-exec-1]`
- 📝 **Logger** (cyan): `c.b.p.a.c.CreateUserUseCase`
- 💬 **Mensaje** (blanco): `user.created userId=...`

**Ventajas:**
- ✅ Fácil de leer en el terminal
- ✅ Colores ayudan a identificar niveles (rojo = ERROR, amarillo = WARN)
- ✅ Compacto, no HTML/JSON
- ✅ Logger names abreviados (com.bookings.padelcenter → c.b.p)
- ✅ Perfecto para desarrollo local

---

## Perfil STAGING/PRODUCTION: JSON Estructurado

**Comando:**
```bash
docker compose up -d postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=staging
```

**Output en consola (JSON, una línea por evento):**

```json
{"timestamp":"2026-04-16T11:25:00.125Z","level":"INFO","logger":"com.bookings.padelcenter.application.create.CreateUserUseCase","message":"user.created userId=550e8400-e29b-41d4-a716-446655440001 email=john@example.com firstName=John lastName=Doe","thread":"http-nio-8080-exec-1","app":"padelcenter"}
{"timestamp":"2026-04-16T11:25:05.200Z","level":"INFO","logger":"com.bookings.padelcenter.application.create.CreateBookingUseCase","message":"booking.created bookingId=12345 fieldId=f47ac10b-58cc-4372-a567-0e02b2c3d479 userId=550e8400-e29b-41d4-a716-446655440001 totalPrice=50.00","thread":"http-nio-8080-exec-2","app":"padelcenter"}
{"timestamp":"2026-04-16T11:25:10.300Z","level":"WARN","logger":"com.bookings.padelcenter.application.update.UpdatePasswordUseCase","message":"user.password.update.failed userId=550e8400-e29b-41d4-a716-446655440001 reason=invalid_current_password","thread":"http-nio-8080-exec-3","app":"padelcenter"}
{"timestamp":"2026-04-16T11:25:15.400Z","level":"INFO","logger":"com.bookings.padelcenter.application.read.GetAllUsersUseCase","message":"users.list.found count=15 totalPages=1","thread":"http-nio-8080-exec-4","app":"padelcenter"}
{"timestamp":"2026-04-16T11:25:20.500Z","level":"WARN","logger":"com.bookings.padelcenter.application.update.CancelBookingUseCase","message":"booking.cancel.failed bookingId=12346 reason=already_cancelled","thread":"http-nio-8080-exec-5","app":"padelcenter"}
```

**Estructura JSON (campos):**
- `timestamp`: ISO 8601 con timezone
- `level`: INFO, WARN, ERROR, etc
- `logger`: nombre completo de la clase
- `message`: evento con parámetros key=value
- `thread`: nombre del thread que ejecutó
- `app`: identificador de aplicación (padelcenter)

**Ventajas:**
- ✅ Parseable por máquinas
- ✅ Cada línea es un documento JSON válido (NDJSON)
- ✅ Ingestible directamente en ELK/Grafana Loki/Splunk
- ✅ Campos estandarizados
- ✅ Sin ambigüedades de escape de caracteres
- ✅ Perfecto para observabilidad en cloud

---

## Comparación Directa

| Aspecto | Local (Colored) | Production (JSON) |
|---------|-----------------|-------------------|
| **Formato** | Texto plano con ANSI | JSON (NDJSON) |
| **Legibilidad humana** | ✅ Excelente | ⚠️ Manual parsing |
| **Parseable por máquinas** | ❌ No | ✅ Sí |
| **Colores** | ✅ Sí | ❌ No (no necesario) |
| **Logger names** | 🔤 Abreviados (c.b.p) | 🔤 Completos |
| **ELK/Loki** | ❌ Requiere parsing | ✅ Directo |
| **Tamaño línea** | ~120 caracteres | ~300+ caracteres |
| **Rendimiento** | Minimal | Minimal |

---

## Ejemplo con Exception

### Local

```
11:26:00.500  ERROR --- [http-nio-8080-exec-6]  o.s.w.s.m.s.DefaultHandlerExceptionResolver : Resolved [com.bookings.padelcenter.domain.exception.UserNotFoundException]
java.util.NoSuchElementException: No value present
	at java.base/java.util.Optional.orElseThrow(Optional.java:375)
	at com.bookings.padelcenter.domain.repository.UserRepository.findById(UserRepository.java:45)
	at com.bookings.padelcenter.application.delete.DeleteUserUseCase.execute(DeleteUserUseCase.java:23)
	...
```

### Production (JSON)

```json
{"timestamp":"2026-04-16T11:26:00.500Z","level":"ERROR","logger":"org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver","message":"Resolved [com.bookings.padelcenter.domain.exception.UserNotFoundException]","thread":"http-nio-8080-exec-6","app":"padelcenter","exception":"java.util.NoSuchElementException: No value present\n\tat java.base/java.util.Optional.orElseThrow(Optional.java:375)\n\tat com.bookings.padelcenter.domain.repository.UserRepository.findById(UserRepository.java:45)"}
```

---

## Configuración por Perfil

| Perfil | Appender | Nivel Root | Descripción |
|--------|----------|------------|-------------|
| `local` | CONSOLE_COLOR | INFO | Desarrollo local, colores ANSI |
| `test` | CONSOLE_COLOR | INFO | Tests, colores ANSI |
| `staging` | CONSOLE_JSON | INFO | Staging, JSON para observabilidad |
| `prod` | CONSOLE_JSON | INFO | Producción, JSON para observabilidad |
| _(default)_ | CONSOLE_JSON | INFO | Cualquier otro, JSON |

**Activar perfil:**
```bash
# Local
-Dspring-boot.run.profiles=local

# Staging
-Dspring-boot.run.profiles=staging

# Producción
-Dspring-boot.run.profiles=prod
```

---

## Ingestión en Observabilidad

### Grafana Loki (Logstash Pipeline)

```yaml
scrape_configs:
  - job_name: padelcenter-prod
    static_configs:
      - targets:
          - localhost
        labels:
          app: padelcenter
          env: prod
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            logger: logger
            message: message
            app: app
      - labels:
          level:
          app:
```

**Query en Grafana:**
```logql
{app="padelcenter", level="ERROR"} | json | message=~"user\\..*"
```

### Elasticsearch (Logstash Filter)

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

**Query en Kibana:**
```
app:padelcenter AND level:WARN
```

---

## Recomendaciones

### 🚀 Desarrollo Local
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
- Colores en consola
- Fácil de leer durante debugging
- Tails con `tail -f` visible

### 📊 Staging/Pre-prod
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=staging
```
- JSON en stdout
- Redireccionar a file o syslog:
  ```bash
  ./app.jar > /var/log/padelcenter/app.log 2>&1
  ```
- Logstash/Filebeat ingestiona JSON

### 🔒 Producción
```bash
# Docker container con perfil prod (default !local)
SPRING_PROFILES_ACTIVE=prod java -jar padelcenter.jar
```
- JSON al stdout del container
- Orquestador (K8s, Docker Compose) redirige a observabilidad
- Sin colores, sin overhead innecesario

---

## Verificación

**Local (con colores):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local 2>&1 | grep "user.created"
```
Espera: linea con colores ANSI

**Producción (JSON):**
```bash
java -jar target/padelcenter-0.0.1-SNAPSHOT.jar 2>&1 | grep "user.created" | jq .
```
Espera: JSON válido parseado por `jq`

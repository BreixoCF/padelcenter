# Logback Configuration — Spring Boot Converters

## Conversion Rules Registrados

En `logback-spring.xml`, el bloque de configuración registra dos **custom conversion rules** de Spring Boot:

```xml
<conversionRule conversionWord="clr"
    converterClass="org.springframework.boot.logging.logback.ColorConverter"/>
<conversionRule conversionWord="wEx"
    converterClass="org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter"/>
```

### 1. `%clr` — ColorConverter

**Qué hace:**
- Añade códigos ANSI de color al texto
- Detecta automáticamente si el terminal soporta colores
- Desactiva colores si la salida no es TTY (logs a archivo)

**Sintaxis:**
```
%clr(texto){color}
```

**Colores disponibles:**
- `{faint}` — gris claro / dim
- `{highlight}` — color resaltado según nivel (INFO=cian, WARN=amarillo, ERROR=rojo)
- `{cyan}`, `{yellow}`, `{magenta}`, `{red}`, `{green}`, `{blue}`

**Ejemplo en pattern:**
```
%clr(%d{HH:mm:ss.SSS}){faint}  --> timestamp en gris claro
%clr(%5p){highlight}           --> nivel (INFO/WARN/ERROR) con color automático
%clr(%-40.40logger{39}){cyan}  --> logger name en cyan
```

### 2. `%wEx` — WhitespaceThrowableProxyConverter

**Qué hace:**
- Formatea exception stack traces con indentación
- Añade whitespace inteligente entre frames
- Hace los stack traces más legibles en consola

**Ejemplo:**
```
java.util.NoSuchElementException: No value present
	at java.base/java.util.Optional.orElseThrow(Optional.java:375)
	at com.bookings.padelcenter.domain.repository.UserRepository.findById(UserRepository.java:45)
	at com.bookings.padelcenter.application.delete.DeleteUserUseCase.execute(DeleteUserUseCase.java:23)
```

Sin `%wEx` (formato estándar `%ex`):
```
java.util.NoSuchElementException: No value present at java.base/java.util.Optional.orElseThrow(Optional.java:375) at com.bookings.padelcenter...
```

---

## Pattern Completo (Local)

```
%clr(%d{HH:mm:ss.SSS}){faint} %clr(%5p){highlight} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx
```

Desglose:
| Parte | Significado | Ejemplo Output |
|-------|-------------|-----------------|
| `%d{HH:mm:ss.SSS}` | Timestamp | `11:25:00.125` |
| `%5p` | Nivel (5 caracteres) | `INFO ` / `WARN ` |
| `[%15.15t]` | Thread name (15 chars) | `[http-nio-8080-1]` |
| `%-40.40logger{39}` | Logger class (40 chars) | `c.b.p.a.c.CreateUserUseCase` |
| `%m` | Mensaje | `user.created userId=...` |
| `%n` | Newline | |
| `%wEx` | Exception con whitespace | Stack trace indentado |

### Resultado Visual (con colores)

```
11:25:00.125  INFO --- [http-nio-8080-exec-1]  c.b.p.a.c.CreateUserUseCase        : user.created userId=... email=...
                     ^^^^                                                          ^^^^^^^^
                    (color)                                                      (cyan)
```

- ⏰ `11:25:00.125` → gris (faint)
- 🟢 `INFO` → verde (highlight for INFO level)
- 🧵 `[http-nio-8080-exec-1]` → gris (faint)
- 📝 Logger name → cyan
- `:` → gris (faint)
- Mensaje → blanco (default)

---

## Por Qué Se Necesitan Estos Converters

### Sin Registro (Error)
```
19:30:15.235 WARN  Error in initialization of FrameworkServlet 'dispatcherServlet'
...
ERROR in ch.qos.logback.core.util.OptionConverter - Could not find class [org.springframework.boot.logging.logback.ColorConverter]
ERROR in ch.qos.logback.core.util.OptionConverter - Could not find class [org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter]
WARNING in ch.qos.logback.core.util.OptionConverter - Unknown word for conversion [clr]
WARNING in ch.qos.logback.core.util.OptionConverter - Unknown word for conversion [wEx]
```

### Con Registro (Correcto)
```
11:25:00.125  INFO --- [http-nio-8080-exec-1]  o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port(s): 8080 (http) with context path ''
11:25:00.250  INFO --- [main]  c.b.p.PadelCenterApplication : Started PadelCenterApplication in 3.245 seconds (process running for 3.456)
```

---

## Activation en Diferentes Perfiles

### Local (Colores)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
- Usa `CONSOLE_COLOR` appender
- Patrón con `%clr` y `%wEx`
- Salida: colores ANSI en consola

### Staging/Producción (JSON)
```bash
java -jar target/padelcenter-*.jar
```
- Usa `CONSOLE_JSON` appender (profile `!local`)
- LogstashEncoder (no necesita `%clr`)
- Salida: JSON estructurado, sin colores

---

## Verificación

### Compilación
```bash
mvn clean compile
```
✅ Sin errores "Unknown word for conversion"

### Ejecución Local
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local 2>&1 | head -20
```

Espera:
- ✅ Logs con colores en ANSI
- ✅ ERROR en rojo, WARN en amarillo, INFO en verde/cian
- ✅ Timestamps en gris
- ✅ Logger names en cyan
- ❌ Sin mensajes de error de Logback

### Ejecución Producción
```bash
java -jar target/padelcenter-*.jar 2>&1 | head -5 | jq .
```

Espera:
- ✅ JSON válido (parseable por `jq`)
- ✅ Campos: timestamp, level, logger, message, thread, app
- ❌ Sin colores ANSI (natural en JSON)

---

## Referencias

- [Logback Manual — Conversion Word](https://logback.qos.ch/manual/layouts.html#conversionWord)
- [Spring Boot — Logback Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging.logback-extensions)
- [Spring Boot Logging Logback](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/logging/logback)

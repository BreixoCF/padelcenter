Copia este prompt y rellena las secciones entre [] antes de enviarlo.

---

Implementa un nuevo caso de uso siguiendo todas las convenciones
del proyecto y la arquitectura hexagonal.

## Contexto de negocio

Nombre: [ej: CancelBookingUseCase]
Descripción: [ej: Permite a un usuario cancelar una reserva propia
siempre que falten más de 24h para el inicio]
Actor: [ej: Usuario autenticado propietario de la reserva]

## Reglas de negocio

1. [ej: Solo el usuario propietario de la reserva puede cancelarla]
2. [ej: No se puede cancelar si faltan menos de 24h para startTime]
3. [ej: Solo se pueden cancelar reservas en estado PENDING o CONFIRMED]
4. [ej: Al cancelar, el estado cambia a CANCELLED]

## Contrato API (si el caso de uso se expone como endpoint)

Actualiza primero el contrato OpenAPI antes de escribir código.

Método HTTP: [POST / GET / PUT / PATCH / DELETE]
Ruta: [ej: PATCH /api/v1/bookings/{bookingId}/cancel]
Request body: [ej: ninguno / { "reason": "string" }]
Response: [ej: BookingResponse con status CANCELLED]
HTTP status éxito: [ej: 200]
Errores posibles:
- [ej: 404 si la reserva no existe]
- [ej: 422 si no cumple las reglas de negocio]
- [ej: 403 si el usuario no es el propietario]

## Implementación — sigue este orden exacto

### Paso 1: Actualizar contrato OpenAPI (si hay endpoint nuevo)
- Añade el endpoint en docs/openapi/paths/[fichero].yaml
- Añade o reutiliza schemas en docs/openapi/components/schemas/
- Ejecuta mvn generate-sources para regenerar interfaces
- Verifica que la interfaz generada tiene el método esperado

### Paso 2: Excepción de dominio (si hace falta una nueva)
- Crea en domain/exception/ extendiendo la base correcta:
    - ResourceNotFoundException → para entidades no encontradas
    - DomainException → para violaciones de reglas de negocio
- Añade el mapeo en GlobalExceptionHandler con ProblemDetail

### Paso 3: Puerto de entrada (si es un caso de uso nuevo)
- Crea la interfaz en domain/port/in/[NombreUseCase].java
- Añade Javadoc: descripción, @param, @return, @throws
- Define el record Command o Query en el mismo paquete

### Paso 4: Puerto de salida (si necesita acceso a datos nuevo)
- Añade el método en la interfaz de repositorio en domain/port/out/
- Añade el método en la interfaz JPA (*JpaRepository)
- Implementa en el adaptador (*RepositoryImpl)

### Paso 5: Caso de uso
- Implementa en application/usecase/[NombreUseCase].java
- @Service, @RequiredArgsConstructor, @Transactional (escritura)
  o @Transactional(readOnly = true) (lectura)
- Sigue el orden: validar existencia → validar reglas → ejecutar → publicar evento (si aplica)
- Log DEBUG al inicio, INFO al completar:
  log.debug("booking.cancel.start bookingId={} userId={}", ...)
  log.info("booking.cancelled bookingId={} userId={}", ...)

### Paso 6: Controlador
- Implementa el método en el controlador correspondiente
- Extrae userId del JWT: authResolver.currentUser()
- Delega directamente en el caso de uso
- Sin lógica de negocio en el controlador

### Paso 7: Tests unitarios
- Crea [NombreUseCase]Test.java en el paquete de tests
- Cubre: happy path + cada regla de negocio que puede fallar
- @ExtendWith(MockitoExtension.class), sin Spring context
- Nomenclatura: execute_condition_expectedResult

### Paso 8: Integration test (si hay endpoint nuevo)
- Añade el caso en el IT del controlador correspondiente
- Cubre: happy path (status correcto + body) + error principal
- Usa @Sql(cleanup.sql) para aislamiento

## Verificación final

Ejecuta mvn clean compile
Ejecuta mvn test -Dtest="*Test,ArchitectureTest"
Verifica en Swagger UI que el endpoint aparece documentado.

git commit -m "feat([area]): [descripción del caso de uso]"
---
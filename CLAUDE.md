# CLAUDE.md — Java 21 + Spring Boot Architect Engineer

## 🧭 Role & Mindset

You are a **Senior Architect Engineer** specializing in Java 21 and Spring Boot.
Your decisions prioritize **correctness, maintainability, performance, and scalability** — in that order.
You think in systems, not just files. You question requirements before implementing them.
You never write code that you wouldn't be proud to defend in a code review.

---

## ☕ Java 21 — Standards & Features

### Always use modern Java idioms

```java
// ✅ Use records for immutable data carriers
public record UserDto(UUID id, String email, String fullName) {}

// ✅ Use sealed classes for exhaustive domain hierarchies
public sealed interface PaymentResult
		permits PaymentResult.Success, PaymentResult.Failure, PaymentResult.Pending {}

// ✅ Use pattern matching in switch
String describe(Object obj) {
	return switch (obj) {
		case Integer i  -> "Integer: " + i;
		case String s   -> "String of length " + s.length();
		case null       -> "null value";
		default         -> "Unknown: " + obj.getClass().getSimpleName();
	};
}

// ✅ Use text blocks for multiline strings (SQL, JSON, templates)
String query = """
		SELECT u.id, u.email
		FROM users u
		WHERE u.active = true
		  AND u.created_at > :since
		ORDER BY u.created_at DESC
		""";
```

### Virtual Threads (Project Loom)
- Enable virtual threads in Spring Boot: `spring.threads.virtual.enabled=true`
- Use for I/O-bound work; avoid for CPU-intensive computation
- Never use `ThreadLocal` patterns with virtual threads without understanding pinning risks

### Prefer
- `var` for local variables when type is obvious from context
- `Optional` only for return types, never for fields or parameters
- `Stream` API with method references over verbose lambdas
- `SequencedCollection` interfaces where applicable
- Immutable collections: `List.of()`, `Map.of()`, `Set.copyOf()`

---

## 🌱 Spring Boot — Architecture Rules

### Project Structure (Hexagonal / Ports & Adapters)

```
src/
├── main/
│   └── java/com/company/app/
│       ├── domain/                  # Pure domain — NO Spring dependencies
│       │   ├── model/               # Entities, Value Objects, Aggregates
│       │   ├── port/
│       │   │   ├── in/              # Use case interfaces (driving ports)
│       │   │   └── out/             # Repository/service interfaces (driven ports)
│       │   └── service/             # Domain services
│       ├── application/             # Use case implementations
│       │   └── usecase/
│       ├── infrastructure/          # Adapters
│       │   ├── persistence/         # JPA repositories, mappers, entities
│       │   ├── web/                 # REST controllers, DTOs, mappers
│       │   ├── messaging/           # Kafka/RabbitMQ producers & consumers
│       │   └── client/              # External HTTP clients (Feign/WebClient)
│       └── config/                  # Spring configuration classes
└── test/
    ├── unit/                        # Pure unit tests (no Spring context)
    ├── integration/                 # @SpringBootTest slices
    └── architecture/                # ArchUnit tests
```

### Layer Rules (enforced via ArchUnit)
- `domain` → no dependencies on `infrastructure`, `application`, or Spring
- `application` → depends only on `domain`
- `infrastructure` → depends on `application` and `domain`
- `web` controllers → never call repositories directly; always go through use cases

---

## 🔧 Spring Boot Configuration

### application.yml structure
```yaml
spring:
  application:
    name: my-service
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
  jpa:
    open-in-view: false          # Always false — prevents N+1 footguns
    show-sql: false
    hibernate:
      ddl-auto: validate         # Use Flyway/Liquibase for migrations
  threads:
    virtual:
      enabled: true              # Java 21 virtual threads

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### Configuration Properties (type-safe)
```java
@ConfigurationProperties(prefix = "app.payment")
@Validated
public record PaymentProperties(
		@NotBlank String apiUrl,
		@Positive int timeoutSeconds,
		@Valid RetryConfig retry
) {
	public record RetryConfig(@Min(1) int maxAttempts, @Positive long backoffMs) {}
}
```

---

## 🗄️ Persistence Layer

### JPA / Spring Data Rules
- Never use `@OneToMany` without `fetch = FetchType.LAZY`
- Never expose JPA entities through REST — always map to DTOs
- Use `@Transactional(readOnly = true)` on all read operations
- Use `@Version` for optimistic locking on concurrent aggregates
- Prefer `Projections` over full entity fetching when only a subset of fields is needed

```java
// ✅ Correct: lazy loading, explicit transaction
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

	@Query("SELECT o FROM Order o JOIN FETCH o.lines WHERE o.id = :id")
	Optional<Order> findByIdWithLines(@Param("id") UUID id);

	@Transactional(readOnly = true)
	List<OrderSummary> findByCustomerId(UUID customerId); // projection
}

// ✅ Flyway migration naming: V{version}__{description}.sql
// V1__create_orders_table.sql
// V2__add_payment_status_index.sql
```

---

## 🌐 REST API Design

### Controllers
```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

	private final CreateOrderUseCase createOrder;
	private final GetOrderUseCase getOrder;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse create(@RequestBody @Valid CreateOrderRequest request) {
		return createOrder.execute(request);
	}

	@GetMapping("/{id}")
	public OrderResponse getById(@PathVariable UUID id) {
		return getOrder.execute(id);
	}
}
```

### Global Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	ProblemDetail handleNotFound(EntityNotFoundException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Resource Not Found");
		return problem;
	}

	@ExceptionHandler(DomainException.class)
	ProblemDetail handleDomain(DomainException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
		problem.setTitle("Business Rule Violation");
		return problem;
	}
}
```

- Always return `ProblemDetail` (RFC 9457) for errors
- Use `@Valid` on all request bodies and path variables
- Version APIs via URL path (`/api/v1/`), not headers

---

## 🏗️ API & Data Strategy (Contract-First)

### 📖 OpenAPI Source of Truth
* **Spec location:** `src/main/resources/api/openapi.yaml`.
* **Workflow:** 1. Define/Update YAML spec.
   2. Run `./gradlew openApiGenerate` to sync Interfaces and DTOs.
   3. Implement generated interfaces in `@RestController` classes.
* **DTO Mapping:** Generated DTOs (Records) are strictly for the Web Layer. Use MapStruct to convert them to Domain Objects before passing them to Use Cases.

### 🆔 Identity Strategy (UUID v7)
* Use **UUID v7** for all primary keys to ensure time-ordered sortability and high DB performance.
* **Generation:** Prefer generation at the Application layer using `UuidCreator.getTimeOrderedEpoch()`.
* **Database:** Configure columns as `UUID` type (PostgreSQL) or `BINARY(16)` (MySQL).

### 🛡️ Concurrency & Virtual Threads (Loom)
* **Zero `synchronized`:** Strictly forbidden to prevent carrier thread pinning. Use `ReentrantLock` if a lock is absolutely necessary.
* **Thread Safety:** Use `java.util.concurrent` collections. Prefer immutability via `records` to avoid shared state issues.

### 🔐 Security Context Management
* **Decoupled Security:** Domain and Application layers must remain agnostic of `SecurityContextHolder`.
* **Context Propagation:** The Web Adapter must extract the `UserId`/`TenantId` from the JWT and pass it as an explicit parameter to Use Case methods.

## 🔒 Security

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						.anyRequest().authenticated()
				)
				.build();
	}
}
```

### Rules
- **No secrets in code or application.yml** — use environment variables or Vault
- Use `@PreAuthorize("hasRole('ADMIN')")` at the use case level, not controller
- Always validate JWT issuer and audience
- Use HTTPS everywhere, even internally between services

---

## 🧪 Testing Strategy

### Pyramid
- **70% Unit tests** — fast, no Spring context, mock collaborators
- **20% Integration tests** — `@SpringBootTest` slices, real DB (Testcontainers)
- **10% E2E / Contract tests** — Pact, RestAssured

### Unit Test Template
```java
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

	@Mock OrderRepository orderRepository;
	@Mock PaymentGateway paymentGateway;
	@InjectMocks CreateOrderUseCase useCase;

	@Test
	@DisplayName("should create order and initiate payment when request is valid")
	void createOrder_validRequest_returnsOrderWithPendingPayment() {
		// given
		var request = CreateOrderRequestMother.valid();
		given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		// when
		var result = useCase.execute(request);

		// then
		assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
		then(paymentGateway).should().initiate(any());
	}
}
```

### Integration Test Template
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class OrderControllerIT {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
	}

	@Autowired TestRestTemplate restTemplate;

	@Test
	void createOrder_validPayload_returns201() {
		var response = restTemplate.postForEntity("/api/v1/orders",
				CreateOrderRequestMother.valid(), OrderResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody().id()).isNotNull();
	}
}
```

### ArchUnit — Enforce Architecture
```java
@AnalyzeClasses(packages = "com.company.app")
class ArchitectureTest {

	@ArchTest
	ArchRule domainIsIsolated = noClasses()
			.that().resideInAPackage("..domain..")
			.should().dependOnClassesThat()
			.resideInAnyPackage("..infrastructure..", "org.springframework..");

	@ArchTest
	ArchRule controllersShouldNotAccessRepositories = noClasses()
			.that().resideInAPackage("..web..")
			.should().dependOnClassesThat()
			.resideInAPackage("..persistence..");
}
```

---

## 📊 Observability

```java
// ✅ Structured logging with context
@Slf4j
public class CreateOrderUseCase {
	public OrderResponse execute(CreateOrderRequest request) {
		log.info("Creating order for customer={} items={}",
				request.customerId(), request.items().size());
		// ...
		log.info("Order created orderId={} status={}", order.getId(), order.getStatus());
	}
}
```

### Rules
- Use **SLF4J + Logback** — never `System.out.println`
- Log at `DEBUG` for flow, `INFO` for business events, `WARN` for recoverable issues, `ERROR` for unhandled failures
- Use **Micrometer** for metrics — annotate with `@Timed` or programmatically
- Export traces via **OpenTelemetry** — inject trace/span IDs into MDC for correlation
- Always include `traceId` in error responses

---

## ⚡ Performance Rules

- Enable **second-level cache** (Caffeine/Redis) for read-heavy, rarely-changing aggregates
- Use `@Async` only with a custom `ThreadPoolTaskExecutor` — never the default
- Use **pagination** (`Pageable`) on all list endpoints — never return unbounded collections
- Use **projections** in JPA for list queries to reduce data transfer
- For bulk writes, use `saveAll()` with batch size configured:
  ```yaml
  spring.jpa.properties.hibernate.jdbc.batch_size: 50
  ```
- Prefer `WebClient` (reactive) for outbound HTTP calls even in imperative apps

---

## 📦 Build & Dependency Management — Gradle (Kotlin DSL)

### Estructura de ficheros
```
├── build.gradle.kts          # Dependencias y plugins del módulo
├── settings.gradle.kts       # Nombre del proyecto y módulos
├── gradle.properties         # Versiones centralizadas y flags JVM
└── gradle/
    └── libs.versions.toml    # Version catalog (fuente de verdad de versiones)
```

### gradle/libs.versions.toml — Version Catalog
```toml
[versions]
spring-boot        = "3.4.4"
spring-dependency  = "1.1.7"
java               = "21"
mapstruct          = "1.6.3"
archunit           = "1.3.0"
testcontainers     = "1.20.4"

[libraries]
# Spring
spring-web            = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-validation     = { module = "org.springframework.boot:spring-boot-starter-validation" }
spring-actuator       = { module = "org.springframework.boot:spring-boot-starter-actuator" }
spring-jpa            = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }
spring-security-oauth = { module = "org.springframework.boot:spring-boot-starter-oauth2-resource-server" }
spring-test           = { module = "org.springframework.boot:spring-boot-starter-test" }

# Persistence
flyway                = { module = "org.flywaydb:flyway-core" }
postgresql            = { module = "org.postgresql:postgresql" }

# Observability
micrometer-prometheus = { module = "io.micrometer:micrometer-registry-prometheus" }
micrometer-otel       = { module = "io.micrometer:micrometer-tracing-bridge-otel" }

# Mapping
mapstruct             = { module = "org.mapstruct:mapstruct", version.ref = "mapstruct" }
mapstruct-processor   = { module = "org.mapstruct:mapstruct-processor", version.ref = "mapstruct" }

# Testing
archunit              = { module = "com.tngtech.archunit:archunit-junit5", version.ref = "archunit" }
testcontainers-pg     = { module = "org.testcontainers:postgresql", version.ref = "testcontainers" }
testcontainers-junit  = { module = "org.testcontainers:junit-jupiter", version.ref = "testcontainers" }

[plugins]
spring-boot           = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency     = { id = "io.spring.dependency-management", version.ref = "spring-dependency" }
```

### build.gradle.kts
```kotlin
plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)  // Toolchain, no JAVA_HOME hardcodeado
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Core
    implementation(libs.spring.web)
    implementation(libs.spring.validation)
    implementation(libs.spring.actuator)

    // Persistence
    implementation(libs.spring.jpa)
    implementation(libs.flyway)
    runtimeOnly(libs.postgresql)

    // Security
    implementation(libs.spring.security.oauth)

    // Observability
    implementation(libs.micrometer.prometheus)
    implementation(libs.micrometer.otel)

    // Mapping
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    // Testing
    testImplementation(libs.spring.test)
    testImplementation(libs.archunit)
    testImplementation(libs.testcontainers.pg)
    testImplementation(libs.testcontainers.junit)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "-parameters",               // Necesario para Spring MVC y MapStruct
        "-Xlint:unchecked",
        "-Xlint:deprecation"
    ))
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading") // Silencia warnings de Mockito en Java 21
    systemProperty("spring.profiles.active", "test")
}
```

### gradle.properties
```properties
# JVM flags globales para todos los subproyectos
org.gradle.jvmargs=-Xmx2g -XX:+UseG1GC
org.gradle.caching=true
org.gradle.parallel=true

# Silencia la advertencia de Kotlin daemon (si se usa .kts)
kotlin.daemon.jvm.options=-Xmx1g
```

### 🛠️ OpenAPI Generator Configuration (Gradle)
* **Plugin:** `id("org.openapi.generator")`
* **Generator:** `spring`
* **Key Options:**
   * `useSpringBoot3: true`
   * `useRecordsModels: true` (Generates DTOs as Java Records)
   * `interfaceOnly: true` (You implement the logic)
   * `skipDefaultInterface: true` (Keep implementation classes clean)

### Comandos clave
```bash
./gradlew build                   # Compila + tests + jar
./gradlew test                    # Solo tests
./gradlew bootRun                 # Arranca la app en local
./gradlew dependencies            # Árbol de dependencias (detectar conflictos)
./gradlew dependencyUpdates       # Con plugin 'com.github.ben-manes.versions'
./gradlew bootBuildImage          # Build OCI image con Cloud Native Buildpacks
```

### Reglas Gradle
- **Siempre** usar Version Catalog (`libs.versions.toml`) — cero versiones hardcodeadas en `build.gradle.kts`
- **Siempre** definir Java Toolchain en lugar de `sourceCompatibility`/`targetCompatibility`
- **Nunca** usar `compile` o `runtime` (configuraciones obsoletas) — usar `implementation` / `runtimeOnly`
- Separar dependencias de test con `testImplementation` y `testRuntimeOnly`
- En proyectos multi-módulo, extraer dependencias comunes a un `build-logic` convention plugin

---

## 📝 Javadoc — Enfoque Minimalista

El código bien nombrado **es** documentación. El Javadoc se reserva exclusivamente para los **contratos públicos** que otros módulos o equipos consumen. Documentar implementaciones internas es ruido que envejece mal.

### Dónde SÍ escribir Javadoc

#### 1. Interfaces de puerto (contratos del dominio)
```java
/**
 * Driving port: defines the contract for creating a new order.
 *
 * <p>Implementations must guarantee that the returned order has been
 * persisted and its initial payment process has been initiated.
 */
public interface CreateOrderUseCase {

    /**
     * Creates an order from the given request.
     *
     * @param request validated order creation data; must not be {@code null}
     * @return the persisted order with its assigned ID and initial status
     * @throws InsufficientStockException if any requested item lacks available stock
     * @throws CustomerNotFoundException  if the referenced customer does not exist
     */
    OrderResponse execute(CreateOrderRequest request);
}
```

#### 2. Value Objects y Records del dominio con reglas de negocio no obvias
```java
/**
 * Represents a monetary amount with its currency.
 *
 * <p>Arithmetic operations always return a new instance (immutable).
 * Two {@code Money} instances are only addable if they share the same currency;
 * attempting to add different currencies throws {@link CurrencyMismatchException}.
 */
public record Money(BigDecimal amount, Currency currency) { ... }
```

#### 3. Excepciones de dominio
```java
/**
 * Thrown when a requested operation would leave a customer's credit limit exceeded.
 *
 * <p>Callers should present this as a 422 Unprocessable Entity to the API client.
 */
public class CreditLimitExceededException extends DomainException { ... }
```

#### 4. `@ConfigurationProperties` con propiedades no autoexplicativas
```java
/**
 * Configuration for the outbound payment gateway client.
 *
 * @param apiUrl         base URL of the payment provider REST API
 * @param timeoutSeconds max seconds to wait for a payment confirmation response
 * @param retry          retry policy applied on transient gateway failures
 */
@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(String apiUrl, int timeoutSeconds, RetryConfig retry) { ... }
```

---

### Dónde NO escribir Javadoc

```java
// ❌ Javadoc que repite el nombre — ruido puro
/**
 * Creates the order.
 */
public OrderResponse execute(CreateOrderRequest request) { ... }

// ❌ Javadoc en implementaciones internas — nadie las consume directamente
/**
 * Implementation of CreateOrderUseCase.
 */
@Service
class CreateOrderUseCaseImpl implements CreateOrderUseCase { ... }

// ❌ Javadoc en controladores REST — la especificación OpenAPI (SpringDoc) es la fuente de verdad
/**
 * Creates a new order.
 */
@PostMapping
public OrderResponse create(@RequestBody @Valid CreateOrderRequest request) { ... }
```

---

### Reglas de formato
- Primera frase: resumen conciso en una línea, termina en punto
- Segundo párrafo (si existe): contexto, restricciones o invariantes importantes
- `@param` solo cuando el nombre no es suficientemente descriptivo o hay restricciones (`must not be null`, rangos)
- `@return` solo cuando el valor devuelto tiene semántica no obvia
- `@throws` para **cada** excepción comprobada y para las de dominio relevantes no comprobadas
- Usar `{@code null}`, `{@link ClassName}` y `{@snippet}` (Java 18+) — nunca HTML crudo innecesario

### Instrucción para Claude
- Generar Javadoc **únicamente** en interfaces de puerto, value objects con reglas no triviales, excepciones de dominio y `@ConfigurationProperties`
- En cualquier otro caso, elegir nombres más expresivos en lugar de añadir comentario



| ❌ Anti-Pattern | ✅ Correct Approach |
|---|---|
| `@Autowired` on fields | Constructor injection (Lombok `@RequiredArgsConstructor`) |
| Business logic in controllers | Move to use cases / domain services |
| `spring.jpa.open-in-view=true` | Set to `false`, fetch eagerly where needed |
| Catching and swallowing exceptions | Log + rethrow or map to domain exception |
| Hardcoded configuration values | `@ConfigurationProperties` + env vars |
| `@SpringBootTest` for unit tests | `@ExtendWith(MockitoExtension.class)` |
| Mutable domain objects with setters | Encapsulate mutations in domain methods |
| `Optional.get()` without `isPresent()` | Use `orElseThrow()`, `map()`, `ifPresent()` |
| Returning `null` | Return `Optional` or throw domain exception |
| Fat services with 10+ dependencies | Split by responsibility, revisit domain model |

---

## 🤖 Instructions for Claude

### When generating code
1. **Always** apply hexagonal architecture — place classes in the correct layer
2. **Always** use Java 21 features where appropriate (records, sealed classes, pattern matching)
3. **Always** write the corresponding unit test alongside any use case or service
4. **Always** use constructor injection — never field injection
5. **Always** validate input at the controller boundary with `@Valid`
6. **Never** put business logic in JPA entities or controllers
7. **Never** use `@Transactional` on controllers

### When reviewing code
- Flag any violation of the layer rules
- Flag any missing `@Transactional(readOnly = true)` on reads
- Flag any eager loading `@OneToMany`
- Flag any exposed JPA entity on REST endpoints
- Suggest virtual thread compatibility issues

### When designing APIs
- Propose the domain model first, then derive the API
- Identify aggregates and their boundaries before writing any code
- Ask about consistency requirements before choosing synchronous vs. event-driven

### Output format
- Code blocks with correct language tag
- Explain *why* a decision was made, not just *what*
- Highlight trade-offs when multiple valid approaches exist
- When refactoring, show before/after clearly

---

## 📋 Definition of Done

A feature is complete when:
- [ ] Domain model captures the business rules
- [ ] Use case is implemented and covered by unit tests (>80% coverage)
- [ ] REST endpoint is documented (OpenAPI / SpringDoc)
- [ ] Integration test passes with Testcontainers
- [ ] No architecture violations (`ArchUnit` green)
- [ ] No `@SuppressWarnings` without explanation
- [ ] Flyway migration script added (if schema changed)
- [ ] Actuator health check works
- [ ] Logs include correlation IDs
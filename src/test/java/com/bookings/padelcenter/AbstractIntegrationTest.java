package com.bookings.padelcenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Base class for all integration tests.
 *
 * <p>Starts a single shared {@link PostgreSQLContainer} for the test suite,
 * injects the dynamic JDBC URL via {@link DynamicPropertySource}, and
 * provides ready-made {@code MockMvc} JWT helpers for admin and regular-user calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

	/** Fixed UUID used as the authenticated admin subject in JWT mocks. */
	static final UUID ADMIN_USER_ID =
			UUID.fromString("00000000-0000-0000-0000-000000000001");

	/** Fixed UUID used as the authenticated regular user subject in JWT mocks. */
	static final UUID REGULAR_USER_ID =
			UUID.fromString("00000000-0000-0000-0000-000000000002");

	@Container
	static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>("postgres:16-alpine")
					.withDatabaseName("padelcenter_test")
					.withUsername("test")
					.withPassword("test");

	@DynamicPropertySource
	static void configureDataSource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
		registry.add("spring.datasource.username", POSTGRES::getUsername);
		registry.add("spring.datasource.password", POSTGRES::getPassword);
	}

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	/**
	 * Returns a {@code RequestPostProcessor} that sets a mock JWT for an ADMIN user.
	 * Authorities include {@code ROLE_ADMIN}; the JWT {@code sub} is {@link #ADMIN_USER_ID}.
	 */
	protected static org.springframework.test.web.servlet.request.RequestPostProcessor adminJwt() {
		return jwt()
				.jwt(j -> j.subject(ADMIN_USER_ID.toString())
						.claim("roles", List.of("ADMIN")))
				.authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
	}

	/**
	 * Returns a {@code RequestPostProcessor} that sets a mock JWT for a regular USER.
	 * Authorities include {@code ROLE_USER} only; the JWT {@code sub} is {@link #REGULAR_USER_ID}.
	 */
	protected static org.springframework.test.web.servlet.request.RequestPostProcessor userJwt() {
		return jwt()
				.jwt(j -> j.subject(REGULAR_USER_ID.toString())
						.claim("roles", List.of("USER")))
				.authorities(new SimpleGrantedAuthority("ROLE_USER"));
	}
}

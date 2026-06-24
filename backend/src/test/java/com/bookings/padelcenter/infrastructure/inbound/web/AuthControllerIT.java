package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("AuthController Integration Tests")
class AuthControllerIT extends AbstractIntegrationTest {

	private static final String AUTH_URL = "/api/v1/auth";
	private static final String USERS_URL = "/api/v1/users";
	private static final String ME_URL = "/api/v1/me";

	@BeforeEach
	void seedRegularUser() {
		jdbcTemplate.update("""
				INSERT INTO users (user_id, first_name, last_name, email, password_hash, created_at)
				VALUES (?, 'Regular', 'User', 'regular@test.com', 'dummyhash', NOW())
				ON CONFLICT (user_id) DO NOTHING
				""", REGULAR_USER_ID);
	}

	// ── POST /api/v1/auth/login ───────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/auth/login — credenciales válidas devuelve 200 con accessToken")
	void login_validCredentials_returns200() throws Exception {
		mockMvc.perform(post(USERS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "firstName":   "Login",
								  "lastName":    "Test",
								  "email":       "login.test@example.com",
								  "password":    "securePass123!",
								  "phoneNumber": "600000004"
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post(AUTH_URL + "/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email":    "login.test@example.com",
								  "password": "securePass123!"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.user.email").value("login.test@example.com"))
				.andExpect(header().exists("Set-Cookie"));
	}

	@Test
	@DisplayName("POST /api/v1/auth/login — contraseña incorrecta devuelve 401")
	void login_wrongPassword_returns401() throws Exception {
		mockMvc.perform(post(USERS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "firstName":   "Login",
								  "lastName":    "Fail",
								  "email":       "login.fail@example.com",
								  "password":    "securePass123!",
								  "phoneNumber": "600000005"
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post(AUTH_URL + "/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email":    "login.fail@example.com",
								  "password": "wrongPassword!"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	// ── POST /api/v1/auth/refresh ─────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/auth/refresh — sin cookie devuelve 401")
	void refreshToken_withoutCookie_returns401() throws Exception {
		mockMvc.perform(post(AUTH_URL + "/refresh"))
				.andExpect(status().isUnauthorized());
	}

	// ── POST /api/v1/auth/logout ──────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/auth/logout — devuelve 204 y limpia la cookie")
	void logout_returns204() throws Exception {
		mockMvc.perform(post(AUTH_URL + "/logout"))
				.andExpect(status().isNoContent())
				.andExpect(header().exists("Set-Cookie"));
	}

	// ── GET /api/v1/me ────────────────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/me — usuario autenticado devuelve su perfil")
	void getMeProfile_authenticated_returns200() throws Exception {
		mockMvc.perform(get(ME_URL).with(userJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(REGULAR_USER_ID.toString()));
	}

	@Test
	@DisplayName("GET /api/v1/me — sin autenticar devuelve 401")
	void getMeProfile_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get(ME_URL))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /api/v1/me/bookings — usuario autenticado devuelve página vacía")
	void getMyBookings_authenticated_returns200() throws Exception {
		mockMvc.perform(get(ME_URL + "/bookings").with(userJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	@DisplayName("GET /api/v1/me/tournaments — usuario autenticado devuelve página vacía")
	void getMyTournaments_authenticated_returns200() throws Exception {
		mockMvc.perform(get(ME_URL + "/tournaments").with(userJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	@DisplayName("GET /api/v1/me/matches — usuario autenticado devuelve página vacía")
	void getMyMatches_authenticated_returns200() throws Exception {
		mockMvc.perform(get(ME_URL + "/matches").with(userJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}
}

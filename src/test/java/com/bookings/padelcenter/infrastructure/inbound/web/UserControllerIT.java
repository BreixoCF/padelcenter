package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerIT extends AbstractIntegrationTest {

	private static final String BASE_URL = "/api/v1/users";

	private static final String VALID_USER_JSON = """
			{
			  "firstName": "John",
			  "lastName":  "Doe",
			  "email":     "john.doe@example.com",
			  "password":  "password123",
			  "phoneNumber": "600123456"
			}
			""";

	// ── POST /api/v1/users ────────────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/users — valid request returns 200 with user id")
	void createUser_validRequest_returns200() throws Exception {
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.email").value("john.doe@example.com"))
				.andExpect(jsonPath("$.firstName").value("John"))
				.andExpect(jsonPath("$.lastName").value("Doe"));
	}

	@Test
	@DisplayName("POST /api/v1/users — duplicate email returns 409")
	void createUser_duplicateEmail_returns409() throws Exception {
		// First registration succeeds
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isOk());

		// Second registration with the same email must be rejected
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("POST /api/v1/users — missing required field returns 400")
	void createUser_missingFirstName_returns400() throws Exception {
		String json = """
				{
				  "lastName":  "Doe",
				  "email":     "nodomain@example.com",
				  "password":  "password123"
				}
				""";
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}

	// ── GET /api/v1/users ─────────────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/users — admin role returns paginated result")
	void getAllUsers_asAdmin_returnsPaginatedResult() throws Exception {
		// Seed one user
		mockMvc.perform(post(BASE_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isOk());

		mockMvc.perform(get(BASE_URL)
						.with(adminJwt())
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20));
	}

	@Test
	@DisplayName("GET /api/v1/users — non-admin role returns 403")
	void getAllUsers_asNonAdmin_returns403() throws Exception {
		mockMvc.perform(get(BASE_URL).with(userJwt()))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /api/v1/users — unauthenticated returns 401")
	void getAllUsers_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get(BASE_URL))
				.andExpect(status().isUnauthorized());
	}
}

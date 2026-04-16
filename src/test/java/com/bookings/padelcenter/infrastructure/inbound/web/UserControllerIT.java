package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
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
	@DisplayName("POST /api/v1/users — valid request returns 201 with body")
	void createUser_validRequest_returns201() throws Exception {
		mockMvc.perform(post(BASE_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").isNotEmpty())
				.andExpect(jsonPath("$.email").value("john.doe@example.com"))
				.andExpect(jsonPath("$.firstName").value("John"))
				.andExpect(jsonPath("$.lastName").value("Doe"));
	}

	@Test
	@DisplayName("POST /api/v1/users — duplicate email returns 409")
	void createUser_duplicateEmail_returns409() throws Exception {
		mockMvc.perform(post(BASE_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isCreated());

		mockMvc.perform(post(BASE_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").isNotEmpty());
	}

	@Test
	@DisplayName("GET /api/v1/users — unauthenticated returns 401")
	void getUsers_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get(BASE_URL))
				.andExpect(status().isUnauthorized());
	}

	// ── GET /api/v1/users ─────────────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/users — admin gets paginated result")
	void getUsers_asAdmin_returnsPaginatedResult() throws Exception {
		// Seed a user
		mockMvc.perform(post(BASE_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isCreated());

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

	// ── Authorization ────────────────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/users — regular user returns 403")
	void listUsers_asUser_returns403() throws Exception {
		mockMvc.perform(get(BASE_URL)
						.with(userJwt()))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PUT /api/v1/users/{id} — different user without ADMIN returns 403")
	void updateUser_asDifferentUser_returns403() throws Exception {
		// Create a user
		String body = mockMvc.perform(post(BASE_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String userId = JsonPath.read(body, "$.userId");

		// Try to update as a different regular user
		mockMvc.perform(put(BASE_URL + "/" + userId)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "firstName": "Hacker",
								  "lastName":  "Doe",
								  "email":     "hacker@example.com",
								  "phoneNumber": "600000099"
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("DELETE /api/v1/users/{id} — different user without ADMIN returns 403")
	void deleteUser_asDifferentUser_returns403() throws Exception {
		String body = mockMvc.perform(post(BASE_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String userId = JsonPath.read(body, "$.userId");

		mockMvc.perform(delete(BASE_URL + "/" + userId)
						.with(userJwt()))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PATCH /api/v1/users/{id}/roles — regular user returns 403")
	void updateUserRoles_asUser_returns403() throws Exception {
		String body = mockMvc.perform(post(BASE_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_USER_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String userId = JsonPath.read(body, "$.userId");

		mockMvc.perform(patch(BASE_URL + "/" + userId + "/roles")
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("[]"))
				.andExpect(status().isForbidden());
	}

	// ── Validation ───────────────────────────────────────────────────────────

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
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}
}

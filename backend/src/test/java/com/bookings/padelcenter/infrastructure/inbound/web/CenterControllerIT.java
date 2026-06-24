package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("CenterController Integration Tests")
class CenterControllerIT extends AbstractIntegrationTest {

	private static final String CENTERS_URL = "/api/v1/centers";

	private static final String VALID_CENTER_JSON = """
			{
			  "name":        "Padel BCN",
			  "address":     "Calle Mayor 1",
			  "city":        "Barcelona",
			  "phoneNumber": "933000001",
			  "email":       "bcn@padel.com"
			}
			""";

	// ── POST /api/v1/centers ──────────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/centers — admin creates center returns 201")
	void createCenter_asAdmin_returns201() throws Exception {
		mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.centerId", notNullValue()))
				.andExpect(jsonPath("$.name").value("Padel BCN"))
				.andExpect(jsonPath("$.city").value("Barcelona"));

		int count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM centers WHERE name = ? AND deleted_at IS NULL",
				Integer.class, "Padel BCN");
		assertEquals(1, count);
	}

	@Test
	@DisplayName("POST /api/v1/centers — regular user returns 403")
	void createCenter_asUser_returns403() throws Exception {
		mockMvc.perform(post(CENTERS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isForbidden());

		int count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM centers WHERE name = ?",
				Integer.class, "Padel BCN");
		assertEquals(0, count);
	}

	@Test
	@DisplayName("POST /api/v1/centers/{id}/fields — regular user returns 403")
	void createField_asUser_returns403() throws Exception {
		// Create center as admin first
		String centerBody = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String centerId = com.jayway.jsonpath.JsonPath.read(centerBody, "$.centerId");

		mockMvc.perform(post(CENTERS_URL + "/" + centerId + "/fields")
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":         "Court 1",
								  "type":         "Indoor",
								  "pricePerHour": 25.00,
								  "isAvailable":  true
								}
								"""))
				.andExpect(status().isForbidden());
	}

	// ── GET /api/v1/centers ───────────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/centers — returns paginated result")
	void listCenters_returnsPaginatedResult() throws Exception {
		// Seed a center
		mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isCreated());

		mockMvc.perform(get(CENTERS_URL)
						.with(adminJwt())
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.currentPage").value(0))
				.andExpect(jsonPath("$.pageSize").value(20));
	}

	// ── GET /api/v1/centers/{centerId}/bookings ──────────────────────────────

	@Test
	@DisplayName("GET /api/v1/centers/{id}/bookings — without filters returns 200")
	void getCenterBookings_withoutFilters_returns200() throws Exception {
		String centerBody = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String centerId = com.jayway.jsonpath.JsonPath.read(centerBody, "$.centerId");

		mockMvc.perform(get(CENTERS_URL + "/" + centerId + "/bookings")
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray());
	}

	// ── DELETE /api/v1/centers/{id} ───────────────────────────────────────────

	@Test
	@DisplayName("DELETE /api/v1/centers/{id} — soft-deletes the row (not just a 204 response)")
	void deleteCenter_asAdmin_persistsSoftDelete() throws Exception {
		String centerBody = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String centerId = com.jayway.jsonpath.JsonPath.read(centerBody, "$.centerId");

		mockMvc.perform(delete(CENTERS_URL + "/" + centerId)
						.with(adminJwt()))
				.andExpect(status().isNoContent());

		Integer deletedAtIsNull = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM centers WHERE center_id = ?::uuid AND deleted_at IS NULL",
				Integer.class, centerId);
		assertEquals(0, deletedAtIsNull,
				"deleted_at must be set after DELETE — the entity should no longer be visible");

		mockMvc.perform(get(CENTERS_URL + "/" + centerId)
						.with(adminJwt()))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /api/v1/centers/{id} — with active fields returns 409")
	void deleteCenter_withActiveFields_returns409() throws Exception {
		String centerBody = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String centerId = com.jayway.jsonpath.JsonPath.read(centerBody, "$.centerId");

		mockMvc.perform(post(CENTERS_URL + "/" + centerId + "/fields")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":         "Court 1",
								  "type":         "Indoor",
								  "pricePerHour": 25.00,
								  "isAvailable":  true
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(delete(CENTERS_URL + "/" + centerId)
						.with(adminJwt()))
				.andExpect(status().isConflict());

		Integer notDeleted = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM centers WHERE center_id = ?::uuid AND deleted_at IS NULL",
				Integer.class, centerId);
		assertEquals(1, notDeleted, "center must remain undeleted while it still has active fields");
	}
}

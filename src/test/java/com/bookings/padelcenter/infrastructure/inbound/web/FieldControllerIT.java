package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("FieldController Integration Tests")
class FieldControllerIT extends AbstractIntegrationTest {

	private static final String FIELDS_URL  = "/api/v1/fields";
	private static final String CENTERS_URL = "/api/v1/centers";

	// ── helpers ───────────────────────────────────────────────────────────────

	private void seedField() throws Exception {
		String centerBody = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":    "Field Test Center",
								  "address": "Calle Test 1",
								  "city":    "Madrid"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String centerId = JsonPath.read(centerBody, "$.centerId");

		mockMvc.perform(post(CENTERS_URL + "/" + centerId + "/fields")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":         "Court 1",
								  "type":         "Indoor",
								  "pricePerHour": 30.00,
								  "isAvailable":  true
								}
								"""))
				.andExpect(status().isCreated());
	}

	// ── GET /api/v1/fields ────────────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/fields — returns paginated result")
	void listFields_returnsPaginatedResult() throws Exception {
		seedField();

		int count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM fields WHERE name = ? AND deleted_at IS NULL",
				Integer.class, "Court 1");
		assertEquals(1, count);

		mockMvc.perform(get(FIELDS_URL)
						.with(adminJwt())
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$.currentPage").value(0))
				.andExpect(jsonPath("$.pageSize").value(20));
	}

	// ── GET /api/v1/fields/{id} ───────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/fields/{id} — non-existent returns 404")
	void getFieldById_notFound_returns404() throws Exception {
		mockMvc.perform(get(FIELDS_URL + "/00000000-0000-0000-0000-000000000099")
						.with(adminJwt()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").isNotEmpty());
	}
}

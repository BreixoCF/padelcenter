package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
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
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.name").value("Padel BCN"))
				.andExpect(jsonPath("$.city").value("Barcelona"));
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
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20));
	}
}

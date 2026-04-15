package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
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

	private static final String VALID_FIELD_JSON = """
			{
			  "name":         "Court A",
			  "type":         "Indoor",
			  "pricePerHour": 30.00,
			  "isAvailable":  true
			}
			""";

	// ── POST /api/v1/centers ──────────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/centers — admin creates center successfully")
	void createCenter_asAdmin_returns200() throws Exception {
		mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.name").value("Padel BCN"))
				.andExpect(jsonPath("$.city").value("Barcelona"));
	}

	@Test
	@DisplayName("POST /api/v1/centers — non-admin role returns 403")
	void createCenter_asNonAdmin_returns403() throws Exception {
		mockMvc.perform(post(CENTERS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("POST /api/v1/centers — missing required field returns 400")
	void createCenter_missingName_returns400() throws Exception {
		String json = """
				{
				  "address": "Calle Mayor 1",
				  "city":    "Barcelona"
				}
				""";
		mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}

	// ── POST /api/v1/centers/{centerId}/fields ────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/centers/{id}/fields — admin creates field in existing center")
	void createField_asAdmin_withExistingCenter_returns200() throws Exception {
		// First create the center and extract its id
		String centerResponse = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_CENTER_JSON))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String centerId = JsonPath.read(centerResponse, "$.id");

		// Then create a field in that center
		mockMvc.perform(post(CENTERS_URL + "/" + centerId + "/fields")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_FIELD_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.name").value("Court A"))
				.andExpect(jsonPath("$.pricePerHour").value(30.00));
	}

	@Test
	@DisplayName("POST /api/v1/centers/{id}/fields — non-existent center returns 404")
	void createField_nonExistentCenter_returns404() throws Exception {
		String randomCenterId = "00000000-0000-0000-0000-000000000099";

		mockMvc.perform(post(CENTERS_URL + "/" + randomCenterId + "/fields")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_FIELD_JSON))
				.andExpect(status().isNotFound());
	}
}

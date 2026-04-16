package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("BookingController Integration Tests")
class BookingControllerIT extends AbstractIntegrationTest {

	private static final String BOOKINGS_URL = "/api/v1/bookings";
	private static final String USERS_URL    = "/api/v1/users";
	private static final String CENTERS_URL  = "/api/v1/centers";

	// ── helpers ───────────────────────────────────────────────────────────────

	private UUID createUser(String email) throws Exception {
		String json = """
				{
				  "firstName":   "Test",
				  "lastName":    "User",
				  "email":       "%s",
				  "password":    "password123",
				  "phoneNumber": "600000001"
				}
				""".formatted(email);

		String body = mockMvc.perform(post(USERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(body, "$.userId"));
	}

	private UUID createField() throws Exception {
		String centerBody = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":    "Test Center",
								  "address": "Calle Test 1",
								  "city":    "Madrid"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		String centerId = JsonPath.read(centerBody, "$.centerId");

		String fieldBody = mockMvc.perform(post(CENTERS_URL + "/" + centerId + "/fields")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":         "Court B",
								  "type":         "Outdoor",
								  "pricePerHour": 25.00,
								  "isAvailable":  true
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(fieldBody, "$.fieldId"));
	}

	// ── POST /api/v1/bookings ─────────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/bookings — valid request returns 201")
	void createBooking_validRequest_returns201() throws Exception {
		createUser("booking.user@example.com");
		UUID fieldId = createField();

		String json = """
				{
				  "fieldId":     "%s",
				  "startTime":   "2025-06-01T10:00:00Z",
				  "endTime":     "2025-06-01T11:00:00Z",
				  "totalPrice":  25.00
				}
				""".formatted(fieldId);

		mockMvc.perform(post(BOOKINGS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.bookingId", notNullValue()))
				.andExpect(jsonPath("$.field.fieldId").value(fieldId.toString()))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	// ── GET /api/v1/bookings/{id} ─────────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/bookings/{id} — non-existent returns 404")
	void getBookingById_notFound_returns404() throws Exception {
		mockMvc.perform(get(BOOKINGS_URL + "/999999")
						.with(userJwt()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").isNotEmpty());
	}
}

package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@DisplayName("BookingController Integration Tests")
class BookingControllerIT extends AbstractIntegrationTest {

	private static final String BOOKINGS_URL  = "/api/v1/bookings";
	private static final String USERS_URL     = "/api/v1/users";
	private static final String CENTERS_URL   = "/api/v1/centers";

	// ── helpers ───────────────────────────────────────────────────────────────

	/** Creates a user and returns their UUID. */
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
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(body, "$.id"));
	}

	/** Creates a center + field (as admin) and returns the field UUID. */
	private UUID createField() throws Exception {
		// Center
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
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		String centerId = JsonPath.read(centerBody, "$.id");

		// Field
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
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(fieldBody, "$.id"));
	}

	// ── POST /api/v1/bookings ─────────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/bookings — valid request returns 201 with booking id")
	void createBooking_validRequest_returns201() throws Exception {
		UUID userId  = createUser("booking.user@example.com");
		UUID fieldId = createField();

		String json = """
				{
				  "userId":      "%s",
				  "fieldId":     "%s",
				  "startTime":   "2025-06-01T10:00:00",
				  "endTime":     "2025-06-01T11:00:00",
				  "totalPrice":  25.00
				}
				""".formatted(userId, fieldId);

		mockMvc.perform(post(BOOKINGS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.userId").value(userId.toString()))
				.andExpect(jsonPath("$.fieldId").value(fieldId.toString()))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	@DisplayName("POST /api/v1/bookings — non-existent user returns 404")
	void createBooking_userNotFound_returns404() throws Exception {
		UUID fieldId = createField();

		String json = """
				{
				  "userId":     "00000000-0000-0000-0000-000000000099",
				  "fieldId":    "%s",
				  "startTime":  "2025-06-01T10:00:00",
				  "endTime":    "2025-06-01T11:00:00",
				  "totalPrice": 25.00
				}
				""".formatted(fieldId);

		mockMvc.perform(post(BOOKINGS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("POST /api/v1/bookings — non-existent field returns 404")
	void createBooking_fieldNotFound_returns404() throws Exception {
		UUID userId = createUser("field.notfound@example.com");

		String json = """
				{
				  "userId":     "%s",
				  "fieldId":    "00000000-0000-0000-0000-000000000099",
				  "startTime":  "2025-06-01T10:00:00",
				  "endTime":    "2025-06-01T11:00:00",
				  "totalPrice": 25.00
				}
				""".formatted(userId);

		mockMvc.perform(post(BOOKINGS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isNotFound());
	}

	// ── GET /api/v1/bookings/users/{userId} ───────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/bookings/users/{userId} — user with no bookings returns empty list")
	void getBookingHistory_userWithNoBookings_returnsEmptyList() throws Exception {
		UUID userId = createUser("history.empty@example.com");

		mockMvc.perform(get(BOOKINGS_URL + "/users/" + userId)
						.with(userJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	@DisplayName("GET /api/v1/bookings/users/{userId} — user with one booking returns it")
	void getBookingHistory_userWithBooking_returnsList() throws Exception {
		UUID userId  = createUser("history.booked@example.com");
		UUID fieldId = createField();

		// Create a booking
		mockMvc.perform(post(BOOKINGS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "userId":     "%s",
								  "fieldId":    "%s",
								  "startTime":  "2025-07-01T09:00:00",
								  "endTime":    "2025-07-01T10:00:00",
								  "totalPrice": 30.00
								}
								""".formatted(userId, fieldId)))
				.andExpect(status().isCreated());

		mockMvc.perform(get(BOOKINGS_URL + "/users/" + userId)
						.with(userJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].status").value("PENDING"));
	}

	@Test
	@DisplayName("GET /api/v1/bookings/users/{userId} — non-existent user returns 404")
	void getBookingHistory_userNotFound_returns404() throws Exception {
		mockMvc.perform(get(BOOKINGS_URL + "/users/00000000-0000-0000-0000-000000000099")
						.with(userJwt()))
				.andExpect(status().isNotFound());
	}
}

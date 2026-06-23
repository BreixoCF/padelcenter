package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("BookingController Integration Tests")
class BookingControllerIT extends AbstractIntegrationTest {

	private static final String BOOKINGS_URL = "/api/v1/bookings";
	private static final String USERS_URL    = "/api/v1/users";
	private static final String CENTERS_URL  = "/api/v1/centers";

	@BeforeEach
	void seedRegularUser() {
		jdbcTemplate.update("""
				INSERT INTO users (user_id, first_name, last_name, email, password_hash, created_at)
				VALUES (?, 'Regular', 'User', 'regular@test.com', 'dummyhash', NOW())
				ON CONFLICT (user_id) DO NOTHING
				""", REGULAR_USER_ID);
	}

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
				.andExpect(jsonPath("$.status").value("CONFIRMED"));

		// status = 2 → CONFIRMED
		int count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM bookings WHERE field_id = ? AND status = 2 AND deleted_at IS NULL",
				Integer.class, fieldId);
		assertEquals(1, count);
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

	// ── Authorization ────────────────────────────────────────────────────────

	@Test
	@DisplayName("PUT /api/v1/bookings/{id} — non-owner returns 403")
	void updateBooking_asNonOwner_returns403() throws Exception {
		// Create prerequisite data as admin/user
		createUser("booking.owner@example.com");
		UUID fieldId = createField();

		// Create booking as REGULAR_USER_ID
		String bookingJson = """
				{
				  "fieldId":     "%s",
				  "startTime":   "2025-06-01T10:00:00Z",
				  "endTime":     "2025-06-01T11:00:00Z",
				  "totalPrice":  25.00
				}
				""".formatted(fieldId);

		String bookingBody = mockMvc.perform(post(BOOKINGS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(bookingJson))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Long bookingId = ((Number) JsonPath.read(bookingBody, "$.bookingId")).longValue();

		// Try to update as a different user
		UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
		var otherUserJwt = jwt()
				.jwt(j -> j.subject(otherUserId.toString())
						.claim("roles", List.of("USER")))
				.authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));

		mockMvc.perform(put(BOOKINGS_URL + "/" + bookingId)
						.with(otherUserJwt)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "startTime":  "2025-06-01T12:00:00Z",
								  "endTime":    "2025-06-01T13:00:00Z",
								  "totalPrice": 30.00
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PATCH /api/v1/bookings/{id}/cancel — non-owner returns 403")
	void cancelBooking_asNonOwner_returns403() throws Exception {
		createUser("cancel.owner@example.com");
		UUID fieldId = createField();

		String bookingJson = """
				{
				  "fieldId":     "%s",
				  "startTime":   "2025-06-01T14:00:00Z",
				  "endTime":     "2025-06-01T15:00:00Z",
				  "totalPrice":  25.00
				}
				""".formatted(fieldId);

		String bookingBody = mockMvc.perform(post(BOOKINGS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(bookingJson))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Long bookingId = ((Number) JsonPath.read(bookingBody, "$.bookingId")).longValue();

		UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
		var otherUserJwt = jwt()
				.jwt(j -> j.subject(otherUserId.toString())
						.claim("roles", List.of("USER")))
				.authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));

		mockMvc.perform(patch(BOOKINGS_URL + "/" + bookingId + "/cancel")
						.with(otherUserJwt))
				.andExpect(status().isForbidden());

		// Booking must remain CONFIRMED (status = 2) — cancel was rejected
		int status = jdbcTemplate.queryForObject(
				"SELECT status FROM bookings WHERE booking_id = ?",
				Integer.class, bookingId);
		assertEquals(2, status);
	}
}

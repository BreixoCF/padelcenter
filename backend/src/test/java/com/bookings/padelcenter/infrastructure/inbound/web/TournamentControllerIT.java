package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("TournamentController Integration Tests")
class TournamentControllerIT extends AbstractIntegrationTest {

	private static final String TOURNAMENTS_URL = "/api/v1/tournaments";
	private static final String CENTERS_URL = "/api/v1/centers";
	private static final String USERS_URL = "/api/v1/users";

	// ── helpers ───────────────────────────────────────────────────────────────

	private UUID createUser(String email) throws Exception {
		String json = """
				{
				  "firstName":   "Player",
				  "lastName":    "Test",
				  "email":       "%s",
				  "password":    "password123",
				  "phoneNumber": "600000002"
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

	private UUID createCenter() throws Exception {
		String body = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":    "Tournament Test Center",
								  "address": "Calle Torneo 1",
								  "city":    "Madrid"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(body, "$.centerId"));
	}

	private UUID createTournament(UUID centerId) throws Exception {
		String json = """
				{
				  "centerId":    "%s",
				  "name":        "Copa IT",
				  "description": "Torneo de prueba",
				  "format":      "ROUND_ROBIN",
				  "maxPairs":    8,
				  "startDate":   "%s",
				  "endDate":     "%s"
				}
				""".formatted(centerId, LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));

		String body = mockMvc.perform(post(TOURNAMENTS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(body, "$.tournamentId"));
	}

	private UUID registerPair(UUID tournamentId, UUID player1, UUID player2) throws Exception {
		String json = """
				{
				  "player1Id": "%s",
				  "player2Id": "%s",
				  "teamName":  "Pareja IT"
				}
				""".formatted(player1, player2);

		String body = mockMvc.perform(post(TOURNAMENTS_URL + "/" + tournamentId + "/pairs")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(body, "$.pairId"));
	}

	// ── POST /api/v1/tournaments ─────────────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/tournaments — admin creates tournament returns 201")
	void createTournament_asAdmin_returns201() throws Exception {
		UUID centerId = createCenter();

		String json = """
				{
				  "centerId":  "%s",
				  "name":      "Copa Primavera",
				  "format":    "ROUND_ROBIN",
				  "maxPairs":  8,
				  "startDate": "%s",
				  "endDate":   "%s"
				}
				""".formatted(centerId, LocalDate.now().plusDays(7), LocalDate.now().plusDays(14));

		mockMvc.perform(post(TOURNAMENTS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tournamentId", notNullValue()))
				.andExpect(jsonPath("$.status").value("DRAFT"));
	}

	@Test
	@DisplayName("POST /api/v1/tournaments — regular user returns 403")
	void createTournament_asUser_returns403() throws Exception {
		UUID centerId = createCenter();

		mockMvc.perform(post(TOURNAMENTS_URL)
						.with(userJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "centerId":  "%s",
								  "name":      "Copa Prohibida",
								  "format":    "ROUND_ROBIN",
								  "maxPairs":  8,
								  "startDate": "%s",
								  "endDate":   "%s"
								}
								""".formatted(centerId, LocalDate.now().plusDays(7), LocalDate.now().plusDays(14))))
				.andExpect(status().isForbidden());
	}

	// ── GET /api/v1/tournaments/{id} ─────────────────────────────────────────

	@Test
	@DisplayName("GET /api/v1/tournaments/{id} — non-existent returns 404")
	void getTournamentById_notFound_returns404() throws Exception {
		mockMvc.perform(get(TOURNAMENTS_URL + "/00000000-0000-0000-0000-000000000099"))
				.andExpect(status().isNotFound());
	}

	// ── Ciclo de vida completo ───────────────────────────────────────────────

	@Test
	@DisplayName("Ciclo completo: open → register x4 → confirm x4 → close genera matches")
	void fullLifecycle_roundRobin_generatesMatchesOnClose() throws Exception {
		UUID centerId = createCenter();
		UUID tournamentId = createTournament(centerId);

		mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/registration/open")
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REGISTRATION_OPEN"));

		for (int i = 1; i <= 4; i++) {
			UUID p1 = createUser("p" + i + "a@example.com");
			UUID p2 = createUser("p" + i + "b@example.com");
			UUID pairId = registerPair(tournamentId, p1, p2);

			mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/pairs/" + pairId + "/confirm")
							.with(adminJwt()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("CONFIRMED"));
		}

		mockMvc.perform(get(TOURNAMENTS_URL + "/" + tournamentId + "/pairs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)));

		mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/registration/close")
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		// Round robin con 4 parejas: C(4,2) = 6 partidos
		mockMvc.perform(get(TOURNAMENTS_URL + "/" + tournamentId + "/matches")
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(6)));

		mockMvc.perform(get(TOURNAMENTS_URL + "/" + tournamentId + "/standings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(4)));
	}

	@Test
	@DisplayName("PATCH /registration/close — sin suficientes parejas confirmadas devuelve 422")
	void closeRegistration_notEnoughPairs_returns422() throws Exception {
		UUID centerId = createCenter();
		UUID tournamentId = createTournament(centerId);

		mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/registration/open")
						.with(adminJwt()))
				.andExpect(status().isOk());

		mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/registration/close")
						.with(adminJwt()))
				.andExpect(status().isUnprocessableEntity());
	}

	// ── DELETE /api/v1/tournaments/{id} ──────────────────────────────────────

	@Test
	@DisplayName("DELETE /api/v1/tournaments/{id} — soft-deletes the row")
	void deleteTournament_asAdmin_persistsSoftDelete() throws Exception {
		UUID centerId = createCenter();
		UUID tournamentId = createTournament(centerId);

		mockMvc.perform(delete(TOURNAMENTS_URL + "/" + tournamentId)
						.with(adminJwt()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(TOURNAMENTS_URL + "/" + tournamentId))
				.andExpect(status().isNotFound());

		Integer deletedAtIsNull = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM tournaments WHERE tournament_id = ?::uuid AND deleted_at IS NULL",
				Integer.class, tournamentId.toString());
		assertEquals(0, deletedAtIsNull, "deleted_at must be set after DELETE");
	}
}

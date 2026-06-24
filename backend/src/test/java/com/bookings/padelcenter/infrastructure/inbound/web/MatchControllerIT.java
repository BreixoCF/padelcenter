package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/db/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("MatchController Integration Tests")
class MatchControllerIT extends AbstractIntegrationTest {

	private static final String TOURNAMENTS_URL = "/api/v1/tournaments";
	private static final String CENTERS_URL = "/api/v1/centers";
	private static final String USERS_URL = "/api/v1/users";
	private static final String MATCHES_URL = "/api/v1/matches";

	// ── helpers ───────────────────────────────────────────────────────────────

	private UUID createUser(String email) throws Exception {
		String body = mockMvc.perform(post(USERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "firstName":   "Player",
								  "lastName":    "Test",
								  "email":       "%s",
								  "password":    "password123",
								  "phoneNumber": "600000003"
								}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(body, "$.userId"));
	}

	private UUID createTournamentWithGeneratedMatches() throws Exception {
		String centerBody = mockMvc.perform(post(CENTERS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name":    "Match Test Center",
								  "address": "Calle Match 1",
								  "city":    "Madrid"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String centerId = JsonPath.read(centerBody, "$.centerId");

		String tournamentBody = mockMvc.perform(post(TOURNAMENTS_URL)
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "centerId":  "%s",
								  "name":      "Copa Match IT",
								  "format":    "ROUND_ROBIN",
								  "maxPairs":  8,
								  "startDate": "%s",
								  "endDate":   "%s"
								}
								""".formatted(centerId, LocalDate.now().plusDays(7), LocalDate.now().plusDays(14))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String tournamentId = JsonPath.read(tournamentBody, "$.tournamentId");

		mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/registration/open")
						.with(adminJwt()))
				.andExpect(status().isOk());

		for (int i = 1; i <= 4; i++) {
			UUID p1 = createUser("m" + i + "a@example.com");
			UUID p2 = createUser("m" + i + "b@example.com");

			String pairBody = mockMvc.perform(post(TOURNAMENTS_URL + "/" + tournamentId + "/pairs")
							.with(adminJwt())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "player1Id": "%s",
									  "player2Id": "%s",
									  "teamName":  "Pareja %d"
									}
									""".formatted(p1, p2, i)))
					.andExpect(status().isCreated())
					.andReturn().getResponse().getContentAsString();
			String pairId = JsonPath.read(pairBody, "$.pairId");

			mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/pairs/" + pairId + "/confirm")
							.with(adminJwt()))
					.andExpect(status().isOk());
		}

		mockMvc.perform(patch(TOURNAMENTS_URL + "/" + tournamentId + "/registration/close")
						.with(adminJwt()))
				.andExpect(status().isOk());

		return UUID.fromString(tournamentId);
	}

	private UUID firstMatchId(UUID tournamentId) throws Exception {
		String matchesBody = mockMvc.perform(get(TOURNAMENTS_URL + "/" + tournamentId + "/matches")
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		return UUID.fromString(JsonPath.read(matchesBody, "$.content[0].matchId"));
	}

	// ── POST /api/v1/matches/{id}/result ─────────────────────────────────────

	@Test
	@DisplayName("POST /api/v1/matches/{id}/result — admin reports result returns 200")
	void reportMatchResult_asAdmin_returns200() throws Exception {
		UUID tournamentId = createTournamentWithGeneratedMatches();
		UUID matchId = firstMatchId(tournamentId);

		String matchBody = mockMvc.perform(get(TOURNAMENTS_URL + "/" + tournamentId + "/matches")
						.param("page", "0")
						.param("size", "1"))
				.andReturn().getResponse().getContentAsString();
		String winnerPairId = JsonPath.read(matchBody, "$.content[0].pairAId");

		mockMvc.perform(post(MATCHES_URL + "/" + matchId + "/result")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "winnerPairId": "%s",
								  "scoreA": 6,
								  "scoreB": 3
								}
								""".formatted(winnerPairId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.result.winnerPairId").value(winnerPairId));
	}

	@Test
	@DisplayName("POST /api/v1/matches/{id}/result — non-existent match returns 404")
	void reportMatchResult_notFound_returns404() throws Exception {
		mockMvc.perform(post(MATCHES_URL + "/00000000-0000-0000-0000-000000000099/result")
						.with(adminJwt())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "winnerPairId": "00000000-0000-0000-0000-000000000001",
								  "scoreA": 6,
								  "scoreB": 3
								}
								"""))
				.andExpect(status().isNotFound());
	}
}

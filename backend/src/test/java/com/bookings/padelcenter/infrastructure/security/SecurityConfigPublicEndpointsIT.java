package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SecurityConfig public endpoints Integration Tests")
class SecurityConfigPublicEndpointsIT extends AbstractIntegrationTest {

	private static final UUID RANDOM_ID = UUID.randomUUID();

	@ParameterizedTest
	@DisplayName("GET public read endpoints — anonymous request never returns 401")
	@ValueSource(strings = {
			"/api/v1/centers/" + "%s",
			"/api/v1/centers/" + "%s" + "/tournaments",
			"/api/v1/tournaments/" + "%s",
			"/api/v1/tournaments/" + "%s" + "/pairs",
			"/api/v1/tournaments/" + "%s" + "/matches",
			"/api/v1/tournaments/" + "%s" + "/standings"
	})
	void publicEndpoint_withoutToken_isNotUnauthorized(String urlTemplate) throws Exception {
		String url = urlTemplate.formatted(RANDOM_ID);

		mockMvc.perform(get(url))
				.andExpect(result -> {
					int status = result.getResponse().getStatus();
					org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
					org.junit.jupiter.api.Assertions.assertNotEquals(403, status);
				});
	}

	@Test
	@DisplayName("POST /api/v1/centers — anonymous request still returns 401")
	void createCenter_withoutToken_isUnauthorized() throws Exception {
		mockMvc.perform(post("/api/v1/centers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("DELETE /api/v1/centers/{id} — anonymous request still returns 401")
	void deleteCenter_withoutToken_isUnauthorized() throws Exception {
		mockMvc.perform(delete("/api/v1/centers/" + RANDOM_ID))
				.andExpect(status().isUnauthorized());
	}
}

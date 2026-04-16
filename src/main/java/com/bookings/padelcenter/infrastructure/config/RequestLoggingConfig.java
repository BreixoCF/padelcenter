package com.bookings.padelcenter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingConfig {

	@Bean
	@Profile("local")
	public CommonsRequestLoggingFilter requestLoggingFilter() {
		var filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(false); // nunca loguear bodies con passwords
		filter.setIncludeHeaders(false); // nunca loguear Authorization header
		filter.setIncludeClientInfo(true);
		filter.setMaxPayloadLength(1000);
		filter.setBeforeMessagePrefix("HTTP REQUEST: ");
		filter.setAfterMessagePrefix("HTTP RESPONSE: ");
		return filter;
	}
}

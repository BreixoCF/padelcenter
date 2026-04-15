package com.bookings.padelcenter.config;

import com.bookings.padelcenter.infrastructure.inbound.security.JwtAuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final JwtAuthenticatedUserResolver jwtAuthenticatedUserResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(jwtAuthenticatedUserResolver);
	}
}

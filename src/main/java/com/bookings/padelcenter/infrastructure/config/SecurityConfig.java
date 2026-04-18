package com.bookings.padelcenter.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsConfigurationSource corsConfigurationSource;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/actuator/health",
								"/actuator/info",
								"/actuator/prometheus",
								"/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html"
						).permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/sync").permitAll()
						.anyRequest().authenticated()
				)
				.build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		var converter = new JwtGrantedAuthoritiesConverter();
		converter.setAuthoritiesClaimName("roles");
		converter.setAuthorityPrefix("ROLE_");
		var jwtConverter = new JwtAuthenticationConverter();
		jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
		return jwtConverter;
	}
}

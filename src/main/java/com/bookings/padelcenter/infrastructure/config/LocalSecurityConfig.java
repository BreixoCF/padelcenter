package com.bookings.padelcenter.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@Profile("local")
public class LocalSecurityConfig {

	@Bean
	JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
		var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		return NimbusJwtDecoder.withSecretKey(key).build();
	}
}

package com.bookings.padelcenter.infrastructure.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public NimbusJwtDecoder jwtDecoder(JwtProperties props) {
        var key = secretKey(props);
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public NimbusJwtEncoder jwtEncoder(JwtProperties props) {
        var key = secretKey(props);
        var jwk = new OctetSequenceKey.Builder(key.getEncoded()).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    private SecretKeySpec secretKey(JwtProperties props) {
        return new SecretKeySpec(
            props.getSecret().getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
    }
}

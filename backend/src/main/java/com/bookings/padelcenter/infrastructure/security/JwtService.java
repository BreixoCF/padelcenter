package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final NimbusJwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        var roles = user.centerRoles().stream()
            .map(cr -> cr.role().name())
            .distinct()
            .toList();

        var claims = JwtClaimsSet.builder()
            .subject(user.userId().toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(jwtProperties.getAccessTokenExpiry()))
            .claim("email", user.email())
            .claim("roles", roles)
            .build();

        return encode(claims);
    }

    public String generateRefreshToken(UUID userId) {
        var claims = JwtClaimsSet.builder()
            .subject(userId.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpiry()))
            .claim("type", "refresh")
            .build();

        return encode(claims);
    }

    public long refreshTokenExpirySeconds() {
        return jwtProperties.getRefreshTokenExpiry();
    }

    private String encode(JwtClaimsSet claims) {
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

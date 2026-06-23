package com.bookings.padelcenter.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "padelcenter-dev-secret-key-change-in-production-min32chars!!";
    private long accessTokenExpiry = 900;     // 15 min
    private long refreshTokenExpiry = 604800; // 7 days
}

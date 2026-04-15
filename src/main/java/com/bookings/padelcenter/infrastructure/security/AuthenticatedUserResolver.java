package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

/**
 * Extracts the authenticated user's UUID from the JWT {@code sub} claim.
 *
 * <p>Designed to be injected into controllers and referenced from SpEL
 * expressions in {@code @PreAuthorize} annotations as {@code @authResolver}.
 *
 * @throws UnauthorizedException if the authentication is missing, not a JWT,
 *                               or the {@code sub} claim is absent / not a valid UUID
 */
@Component("authResolver")
public class AuthenticatedUserResolver {

	public UUID resolveUserId(Authentication authentication) {
		if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
			throw new UnauthorizedException("Expected JWT authentication");
		}
		var subject = jwtAuth.getToken().getSubject();
		if (subject == null || subject.isBlank()) {
			throw new UnauthorizedException("JWT does not contain a 'sub' claim");
		}
		try {
			return UUID.fromString(subject);
		} catch (IllegalArgumentException e) {
			throw new UnauthorizedException("JWT 'sub' claim is not a valid UUID: " + subject);
		}
	}

	public UUID resolveUserId(Principal principal) {
		if (!(principal instanceof JwtAuthenticationToken jwtAuth)) {
			throw new UnauthorizedException("Expected JWT authentication");
		}
		return resolveUserId((Authentication) jwtAuth);
	}

	public AuthenticatedUser currentUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		return new AuthenticatedUser(resolveUserId(authentication));
	}
}

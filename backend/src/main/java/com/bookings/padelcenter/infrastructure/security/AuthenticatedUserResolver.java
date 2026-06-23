package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.UnauthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;
import java.util.UUID;

/**
 * Extracts the authenticated user's UUID from the JWT {@code sub} claim.
 *
 * <p>Used in two contexts:
 * <ul>
 *   <li>As {@code @authResolver} bean in SpEL {@code @PreAuthorize} expressions.</li>
 *   <li>As a Spring MVC {@link HandlerMethodArgumentResolver} that injects
 *       {@link AuthenticatedUser} into controller method parameters.</li>
 * </ul>
 *
 * @throws UnauthorizedException if authentication is missing, not a JWT,
 *                               or the {@code sub} claim is absent / not a valid UUID
 */
@Component("authResolver")
public class AuthenticatedUserResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return AuthenticatedUser.class.equals(parameter.getParameterType());
	}

	@Override
	public AuthenticatedUser resolveArgument(MethodParameter parameter,
	                                          ModelAndViewContainer mavContainer,
	                                          NativeWebRequest webRequest,
	                                          WebDataBinderFactory binderFactory) {
		return currentUser();
	}

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

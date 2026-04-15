package com.bookings.padelcenter.infrastructure.inbound.security;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * Resolves {@link AuthenticatedUser} method parameters in controllers by extracting
 * the user's UUID from the {@code sub} claim of the validated JWT.
 */
@Component
public class JwtAuthenticatedUserResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return AuthenticatedUser.class.equals(parameter.getParameterType());
	}

	@Override
	public AuthenticatedUser resolveArgument(MethodParameter parameter,
	                                          ModelAndViewContainer mavContainer,
	                                          NativeWebRequest webRequest,
	                                          WebDataBinderFactory binderFactory) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof JwtAuthenticationToken jwtAuth) {
			var subject = jwtAuth.getToken().getSubject();
			return new AuthenticatedUser(UUID.fromString(subject));
		}

		throw new IllegalStateException(
				"Expected JwtAuthenticationToken in security context but found: " +
				(authentication == null ? "null" : authentication.getClass().getSimpleName())
		);
	}
}

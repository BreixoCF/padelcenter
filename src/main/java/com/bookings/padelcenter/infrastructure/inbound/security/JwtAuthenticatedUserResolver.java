package com.bookings.padelcenter.infrastructure.inbound.security;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.infrastructure.security.AuthenticatedUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link AuthenticatedUser} method parameters in controllers by
 * delegating to {@link AuthenticatedUserResolver}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticatedUserResolver implements HandlerMethodArgumentResolver {

	private final AuthenticatedUserResolver authResolver;

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
		return new AuthenticatedUser(authResolver.resolveUserId(authentication));
	}
}

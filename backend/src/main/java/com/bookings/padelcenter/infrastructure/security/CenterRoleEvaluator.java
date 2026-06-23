package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.domain.model.Role;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Evaluates whether an authenticated user holds a specific role in a given center.
 *
 * <p>Intended for use in {@code @PreAuthorize} SpEL expressions:
 * <pre>
 * {@literal @}PreAuthorize("@centerRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #centerId)")
 * </pre>
 */
@Component("centerRoleEvaluator")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CenterRoleEvaluator {

	private final UserRepository userRepository;

	public boolean isAdminOf(UUID userId, UUID centerId) {
		return hasRoleIn(userId, centerId, Role.ADMIN);
	}

	public boolean hasRoleIn(UUID userId, UUID centerId, Role role) {
		return userRepository.findById(userId)
				.map(user -> user.getRoleInCenter(centerId) == role)
				.orElse(false);
	}
}

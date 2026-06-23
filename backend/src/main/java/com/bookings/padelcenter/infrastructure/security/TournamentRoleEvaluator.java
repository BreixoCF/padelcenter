package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Evaluates whether an authenticated user is an admin of the center that owns a given tournament.
 *
 * <p>Intended for use in {@code @PreAuthorize} SpEL expressions:
 * <pre>
 * {@literal @}PreAuthorize("@tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #tournamentId)")
 * </pre>
 */
@Component("tournamentRoleEvaluator")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentRoleEvaluator {

	private final TournamentRepository tournamentRepository;
	private final CenterRoleEvaluator centerRoleEvaluator;

	public boolean isAdminOf(UUID userId, UUID tournamentId) {
		return tournamentRepository.findById(tournamentId)
				.map(tournament -> centerRoleEvaluator.isAdminOf(userId, tournament.centerId()))
				.orElse(false);
	}
}

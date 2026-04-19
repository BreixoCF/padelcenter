package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.SyncUserCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Upserts a local User record from a Keycloak JWT on first login.
 *
 * <p>If a user with the given {@code keycloakId} already exists, it is returned unchanged.
 * Otherwise a new User is created using the JWT claims (email, given_name, family_name).
 * The {@code passwordHash} is left blank — authentication is fully delegated to Keycloak.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SyncUserUseCase implements CommandUseCase<SyncUserCommand, User> {

	private final UserRepository userRepository;

	@Override
	public User execute(SyncUserCommand command) {
		// 1. Already synced — return as-is
		var byKeycloak = userRepository.findByKeycloakId(command.keycloakId());
		if (byKeycloak.isPresent()) {
			log.debug("user.sync.found keycloakId={}", command.keycloakId());
			return byKeycloak.get();
		}

		// 2. Pre-existing user with same email but no keycloakId — link accounts
		var byEmail = userRepository.findByEmail(command.email());
		if (byEmail.isPresent()) {
			log.info("user.sync.linked keycloakId={} email={}", command.keycloakId(), command.email());
			var linked = byEmail.get().linkKeycloak(command.keycloakId());
			return userRepository.save(linked);
		}

		// 3. Brand new user — create
		log.info("user.sync.created keycloakId={} email={}", command.keycloakId(), command.email());
		var newUser = new User(
				null,
				command.keycloakId(),
				command.firstName(),
				command.lastName(),
				command.email(),
				"",
				null,
				Collections.emptySet(),
				Auditable.newAudit()
		);
		return userRepository.save(newUser);
	}
}

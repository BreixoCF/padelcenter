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
		log.info("sync.start keycloakId={} email={}", command.keycloakId(), command.email());

		// 1. Already synced — return as-is
		log.info("sync.step1 findByKeycloakId={}", command.keycloakId());
		var byKeycloak = userRepository.findByKeycloakId(command.keycloakId());
		if (byKeycloak.isPresent()) {
			log.info("sync.step1.found userId={}", byKeycloak.get().userId());
			return byKeycloak.get();
		}
		log.info("sync.step1.notFound");

		// 2. Pre-existing user with same email but no keycloakId — link accounts
		log.info("sync.step2 findByEmail={}", command.email());
		var byEmail = userRepository.findByEmail(command.email());
		if (byEmail.isPresent()) {
			log.info("sync.step2.found userId={} keycloakId={}", byEmail.get().userId(), byEmail.get().keycloakId());
			var linked = byEmail.get().linkKeycloak(command.keycloakId());
			log.info("sync.saving user={}", linked.email());
			return userRepository.save(linked);
		}
		log.info("sync.step2.notFound");

		// 3. Brand new user — create
		log.info("sync.step3 creating new user email={}", command.email());
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
		log.info("sync.saving user={}", newUser.email());
		return userRepository.save(newUser);
	}
}

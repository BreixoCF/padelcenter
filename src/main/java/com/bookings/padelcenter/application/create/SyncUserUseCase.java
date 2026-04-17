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
		return userRepository.findByKeycloakId(command.keycloakId())
				.orElseGet(() -> {
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
					var saved = userRepository.save(newUser);
					log.info("user.synced.created userId={} keycloakId={} email={}",
							saved.userId(), command.keycloakId(), command.email());
					return saved;
				});
	}
}

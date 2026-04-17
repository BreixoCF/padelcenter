package com.bookings.padelcenter.application.mapper;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;

@Component
public class UserCommandMapper {

	public User toDomain(CreateUserCommand command) {
		return new User(
				null,
				null,
				command.firstName(),
				command.lastName(),
				command.email(),
				command.password(),
				command.phoneNumber(),
				Collections.emptySet(),
				Auditable.newAudit()
		);
	}

	public User updateFromCommand(UpdateUserCommand command, User user) {
		return new User(
				user.userId(),
				user.keycloakId(),
				command.firstName(),
				command.lastName(),
				command.email(),
				command.password(),
				command.phoneNumber(),
				user.centerRoles(),
				user.audit().update(user.audit().modifiedBy())
		);
	}
}

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
		var auditable = Auditable.newAudit();
		return new User(
				null,
				command.firstName(),
				command.lastName(),
				command.email(),
				command.password(),
				command.phoneNumber(),
				Collections.emptySet(),
				auditable
		);
	}

	public User updateFromCommand(UpdateUserCommand command, User user) {
		var updatedAuditable = user.audit().update(user.audit().modifiedBy());
		return new User(
				user.id(),
				command.firstName(),
				command.lastName(),
				command.email(),
				command.password(),
				command.phoneNumber(),
				user.centerRoles(),
				updatedAuditable
		);
	}
}

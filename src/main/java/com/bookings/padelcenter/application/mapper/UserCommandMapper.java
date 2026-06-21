package com.bookings.padelcenter.application.mapper;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class UserCommandMapper {

	private final PasswordEncoder passwordEncoder;

	public User toDomain(CreateUserCommand command) {
		return new User(
				UuidCreator.getTimeOrderedEpoch(),
				command.firstName(),
				command.lastName(),
				command.email(),
				passwordEncoder.encode(command.password()),
				command.phoneNumber(),
				Collections.emptySet(),
				Auditable.newAudit()
		);
	}

	public User updateFromCommand(UpdateUserCommand command, User user) {
		return new User(
				user.userId(),
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

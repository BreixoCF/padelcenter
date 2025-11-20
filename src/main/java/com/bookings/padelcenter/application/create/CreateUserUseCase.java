package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase implements CommandUseCase<CreateUserCommand, User> {

	private final UserRepository userRepository;

	@Override
	public User execute(CreateUserCommand command) {
		return null;
	}
}

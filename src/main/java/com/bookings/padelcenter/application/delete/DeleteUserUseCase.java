package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteUserCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteUserUseCase implements CommandUseCase<DeleteUserCommand, User> {

	private final UserRepository userRepository;

	@Override
	public User execute(DeleteUserCommand command) {
		var user = userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));
		var deletedUser = user.delete(command.authenticatedUser().userId());
		return userRepository.save(deletedUser);
	}
}

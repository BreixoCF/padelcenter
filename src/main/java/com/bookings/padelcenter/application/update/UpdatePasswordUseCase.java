package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdatePasswordCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.InvalidPasswordException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.port.out.PasswordHasher;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdatePasswordUseCase implements CommandUseCase<UpdatePasswordCommand, User> {

	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	@Override
	public User execute(UpdatePasswordCommand command) {
		var user = userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));

		if (!passwordHasher.matches(command.currentPassword(), user.passwordHash())) {
			throw new InvalidPasswordException();
		}

		var newPasswordHash = passwordHasher.hash(command.newPassword());
		var updatedUser = user.updatePassword(newPasswordHash, command.modifiedBy());
		return userRepository.save(updatedUser);
	}
}

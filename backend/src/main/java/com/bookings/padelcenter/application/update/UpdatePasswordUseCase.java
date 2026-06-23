package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdatePasswordCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.InvalidPasswordException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.port.out.PasswordHasher;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdatePasswordUseCase implements CommandUseCase<UpdatePasswordCommand, User> {

	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	@Override
	public User execute(UpdatePasswordCommand command) {
		log.debug("user.password.update.start userId={}",
			command.userId());

		var user = userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));

		if (!passwordHasher.matches(command.currentPassword(), user.passwordHash())) {
			log.warn("user.password.update.failed userId={} reason=invalid_current_password",
				command.userId());
			throw new InvalidPasswordException();
		}

		var newPasswordHash = passwordHasher.hash(command.newPassword());
		var updatedUser = user.updatePassword(newPasswordHash, command.authenticatedUser().userId());
		var savedUser = userRepository.save(updatedUser);

		log.info("user.password.updated userId={}",
			savedUser.userId());

		return savedUser;
	}
}

package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteUserCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteUserUseCase implements CommandUseCase<DeleteUserCommand, User> {

	private final UserRepository userRepository;

	@Override
	@PreAuthorize("@authResolver.resolveUserId(authentication).equals(#command.userId) or hasRole('ADMIN')")
	public User execute(DeleteUserCommand command) {
		log.debug("user.delete.start userId={} deletedBy={}",
			command.userId(), command.authenticatedUser().userId());

		var user = userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));
		var deletedUser = user.delete(command.authenticatedUser().userId());
		var savedUser = userRepository.save(deletedUser);

		log.info("user.deleted userId={} email={} deletedBy={}",
			savedUser.userId(), savedUser.email(), command.authenticatedUser().userId());

		return savedUser;
	}
}

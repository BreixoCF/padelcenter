package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.application.mapper.UserCommandMapper;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.EmailAlreadyExistsException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateUserUseCase implements CommandUseCase<UpdateUserCommand, User> {

	private final UserRepository userRepository;
	private final UserCommandMapper userCommandMapper;

	@Override
	public User execute(UpdateUserCommand command) {
		log.debug("user.update.start userId={} email={}",
			command.userId(), command.email());

		var user = userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));
		if (!user.email().equals(command.email()) && userRepository.existsByEmail(command.email())) {
			throw new EmailAlreadyExistsException(command.email());
		}
		var updatedUser = userCommandMapper.updateFromCommand(command, user);
		var savedUser = userRepository.save(updatedUser);

		log.info("user.updated userId={} email={} firstName={} lastName={}",
			savedUser.userId(), savedUser.email(), savedUser.firstName(), savedUser.lastName());

		return savedUser;
	}
}

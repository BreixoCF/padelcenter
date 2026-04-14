package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.application.mapper.UserCommandMapper;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.EmailAlreadyExistsException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateUserUseCase implements CommandUseCase<UpdateUserCommand, User> {

	private final UserRepository userRepository;
	private final UserCommandMapper userCommandMapper;

	@Override
	public User execute(UpdateUserCommand command) {
		var user = userRepository.findById(command.id())
				.orElseThrow(() -> new UserNotFoundException(command.id()));
		if (!user.email().equals(command.email()) && userRepository.existsByEmail(command.email())) {
			throw new EmailAlreadyExistsException(command.email());
		}
		var updatedUser = userCommandMapper.updateFromCommand(command, user);
		return userRepository.save(updatedUser);
	}
}

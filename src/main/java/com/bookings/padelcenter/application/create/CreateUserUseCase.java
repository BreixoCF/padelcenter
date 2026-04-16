package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.mapper.UserCommandMapper;
import com.bookings.padelcenter.application.shared.CommandUseCase;
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
public class CreateUserUseCase implements CommandUseCase<CreateUserCommand, User> {

	private final UserRepository userRepository;
	private final UserCommandMapper userCommandMapper;

	@Override
	public User execute(CreateUserCommand command) {
		log.debug("user.create.start email={} firstName={} lastName={}",
			command.email(), command.firstName(), command.lastName());

		var userToCreate = userCommandMapper.toDomain(command);
		var user = userRepository.save(userToCreate);

		log.info("user.created userId={} email={} firstName={} lastName={}",
			user.id(), user.email(), user.firstName(), user.lastName());

		return user;
	}
}

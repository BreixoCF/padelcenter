package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateUserRolesCommand;
import com.bookings.padelcenter.application.mapper.UserCommandMapper;
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
public class UpdateUserRolesUseCase implements CommandUseCase<UpdateUserRolesCommand, User> {

	private final UserRepository userRepository;
	private final UserCommandMapper userCommandMapper;

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public User execute(UpdateUserRolesCommand command) {
		log.debug("user.roles.update.start userId={} newRolesCount={}",
			command.userId(), command.newRoles().size());

		var user = userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));
		var updatedUser = user.updateRoles(command.newRoles(), command.authenticatedUser().userId());
		userRepository.save(updatedUser);

		log.info("user.roles.updated userId={} rolesCount={}",
			updatedUser.userId(), updatedUser.centerRoles().size());

		return updatedUser;
	}
}

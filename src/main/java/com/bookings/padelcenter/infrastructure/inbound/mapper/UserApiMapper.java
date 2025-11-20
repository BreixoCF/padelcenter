package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.inbound.dto.UserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserApiMapper {

	public CreateUserCommand toCommand(UserRequest request) {
		return new CreateUserCommand(
				request.firstName(),
				request.lastName(),
				request.email(),
				request.password(),
				request.phoneNumber()
		);
	}

	public UserResponse toResponse(User user) {
		return new UserResponse(
				user.id(),
				user.firstName(),
				user.lastName(),
				user.email(),
				user.phoneNumber()
		);
	}
}

package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.create.CreateUserUseCase;
import com.bookings.padelcenter.application.delete.DeleteUserUseCase;
import com.bookings.padelcenter.application.query.GetAllUsersQuery;
import com.bookings.padelcenter.application.read.GetAllUsersUseCase;
import com.bookings.padelcenter.application.update.UpdateUserUseCase;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.inbound.dto.UserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.UserResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.UserApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final CreateUserUseCase createUserUseCase;
	private final GetAllUsersUseCase getAllUsersUseCase;
	private final UpdateUserUseCase updateUserUseCase;
	private final DeleteUserUseCase deleteUserUseCase;

	private final UserApiMapper mapper;

	@PostMapping
	public ResponseEntity<UserResponse> createUser(UserRequest request) {
		var command = mapper.toCommand(request);
		var createdUser = createUserUseCase.execute(command);
		var response = mapper.toResponse(createdUser);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		var query = new GetAllUsersQuery();
		var users = getAllUsersUseCase.execute(query);
		var response = users.stream()
				.map(mapper::toResponse)
				.toList();
		return ResponseEntity.ok(response);
	}
}

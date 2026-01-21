package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateUserUseCase;
import com.bookings.padelcenter.application.delete.DeleteUserUseCase;
import com.bookings.padelcenter.application.query.GetAllUsersQuery;
import com.bookings.padelcenter.application.read.GetAllUsersUseCase;
import com.bookings.padelcenter.application.update.UpdateUserUseCase;
import com.bookings.padelcenter.infrastructure.inbound.dto.CreateUserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.CreateUserResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.UserApiMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	public ResponseEntity<@NonNull CreateUserResponse> createUser(@RequestBody CreateUserRequest request) {
		var command = mapper.toCommand(request);
		var createdUser = createUserUseCase.execute(command);
		var response = mapper.toResponse(createdUser);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<@NonNull List<CreateUserResponse>> getAllUsers() {
		var query = new GetAllUsersQuery();
		var users = getAllUsersUseCase.execute(query);
		var response = users.stream()
				.map(mapper::toResponse)
				.toList();
		return ResponseEntity.ok(response);
	}

	//@PutMapping("/{id}")
	//public ResponseEntity<UpdateUserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
	//	var command = mapper.toCommand(id, request);
	//	var updatedUser = updateUserUseCase.execute(command);
	//	var response = mapper.toResponse(updatedUser);
	//	return ResponseEntity.ok(response);
	//}
}

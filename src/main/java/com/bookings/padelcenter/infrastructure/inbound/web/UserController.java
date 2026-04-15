package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateUserUseCase;
import com.bookings.padelcenter.application.delete.DeleteUserUseCase;
import com.bookings.padelcenter.application.query.GetAllUsersQuery;
import com.bookings.padelcenter.application.read.GetAllUsersUseCase;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.application.update.UpdatePasswordUseCase;
import com.bookings.padelcenter.application.update.UpdateUserRolesUseCase;
import com.bookings.padelcenter.application.update.UpdateUserUseCase;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CenterRoleRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateUserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.UpdatePasswordRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.UpdateUserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateUserResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.UpdatePasswordResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.UpdateUserResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.UserApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final CreateUserUseCase createUserUseCase;
	private final GetAllUsersUseCase getAllUsersUseCase;
	private final UpdateUserUseCase updateUserUseCase;
	private final UpdateUserRolesUseCase updateUserRolesUseCase;
	private final UpdatePasswordUseCase updatePasswordUseCase;
	private final DeleteUserUseCase deleteUserUseCase;

	private final UserApiMapper mapper;

	@PostMapping
	public ResponseEntity<@NonNull CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		var command = mapper.toCommand(request);
		var createdUser = createUserUseCase.execute(command);
		var response = mapper.toResponse(createdUser);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<UpdateUserResponse> updateUser(@PathVariable UUID id,
	                                                      @Valid @RequestBody UpdateUserRequest request) {
		var command = mapper.toCommand(id, request);
		var updatedUser = updateUserUseCase.execute(command);
		var response = mapper.toUpdateUserResponse(updatedUser);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{id}/roles")
	public ResponseEntity<UpdateUserResponse> updateUserRoles(@PathVariable UUID id,
	                                                           @RequestBody Set<CenterRoleRequest> rolesRequest,
	                                                           AuthenticatedUser authenticatedUser) {
		var command = mapper.toUpdateUserRolesCommand(id, rolesRequest, authenticatedUser);
		var updatedUser = updateUserRolesUseCase.execute(command);
		var response = mapper.toUpdateUserResponse(updatedUser);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<@NonNull PageResult<CreateUserResponse>> getAllUsers(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		var query = new GetAllUsersQuery(page, size);
		var pageResult = getAllUsersUseCase.execute(query);
		var mapped = new PageResult<>(
				pageResult.content().stream().map(mapper::toResponse).toList(),
				pageResult.page(),
				pageResult.size(),
				pageResult.totalElements(),
				pageResult.totalPages()
		);
		return ResponseEntity.ok(mapped);
	}

	@PatchMapping("/{id}/password")
	public ResponseEntity<UpdatePasswordResponse> updatePassword(@PathVariable UUID id,
	                                                              @Valid @RequestBody UpdatePasswordRequest request,
	                                                              AuthenticatedUser authenticatedUser) {
		var command = mapper.toUpdatePasswordCommand(id, request, authenticatedUser);
		var updatedUser = updatePasswordUseCase.execute(command);
		var response = mapper.toUpdatePasswordResponse(updatedUser);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable UUID id, AuthenticatedUser authenticatedUser) {
		var command = mapper.toDeleteUserCommand(id, authenticatedUser);
		deleteUserUseCase.execute(command);
		return ResponseEntity.noContent().build();
	}
}

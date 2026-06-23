package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateUserUseCase;
import com.bookings.padelcenter.application.delete.DeleteUserUseCase;
import com.bookings.padelcenter.application.query.GetAllUsersQuery;
import com.bookings.padelcenter.application.query.GetBookingHistoryQuery;
import com.bookings.padelcenter.application.query.GetUserByIdQuery;
import com.bookings.padelcenter.application.read.GetAllUsersUseCase;
import com.bookings.padelcenter.application.read.GetBookingHistoryUseCase;
import com.bookings.padelcenter.application.read.GetUserByIdUseCase;
import com.bookings.padelcenter.application.update.UpdatePasswordUseCase;
import com.bookings.padelcenter.application.update.UpdateUserRolesUseCase;
import com.bookings.padelcenter.application.update.UpdateUserUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.BookingApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.UserApiMapper;
import com.bookings.padelcenter.infrastructure.security.AuthenticatedUserResolver;
import com.padelcenter.infrastructure.web.generated.api.UsersApi;
import com.padelcenter.infrastructure.web.generated.model.CenterRoleRequest;
import com.padelcenter.infrastructure.web.generated.model.GetMyBookings200Response;
import com.padelcenter.infrastructure.web.generated.model.ListUsers200Response;
import com.padelcenter.infrastructure.web.generated.model.MessageResponse;
import com.padelcenter.infrastructure.web.generated.model.PasswordUpdateRequest;
import com.padelcenter.infrastructure.web.generated.model.UserRequest;
import com.padelcenter.infrastructure.web.generated.model.UserResponse;
import com.padelcenter.infrastructure.web.generated.model.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

	private final CreateUserUseCase createUserUseCase;
	private final GetAllUsersUseCase getAllUsersUseCase;
	private final GetUserByIdUseCase getUserByIdUseCase;
	private final UpdateUserUseCase updateUserUseCase;
	private final UpdateUserRolesUseCase updateUserRolesUseCase;
	private final UpdatePasswordUseCase updatePasswordUseCase;
	private final DeleteUserUseCase deleteUserUseCase;
	private final GetBookingHistoryUseCase getBookingHistoryUseCase;

	private final UserApiMapper userMapper;
	private final BookingApiMapper bookingMapper;
	private final AuthenticatedUserResolver authResolver;

	@Override
	public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
		var command = userMapper.toCommand(userRequest);
		var user = createUserUseCase.execute(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
	}

	@Override
	public ResponseEntity<ListUsers200Response> listUsers(Integer page, Integer size, String sort) {
		var query = new GetAllUsersQuery(page, size);
		var pageResult = getAllUsersUseCase.execute(query);
		List<UserResponse> content = pageResult.content().stream()
				.map(userMapper::toResponse)
				.toList();
		var response = new ListUsers200Response(
				content,
				pageResult.totalElements(),
				pageResult.totalPages(),
				pageResult.page(),
				pageResult.size()
		);
		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<UserResponse> getUserById(UUID id) {
		var user = getUserByIdUseCase.execute(new GetUserByIdQuery(id));
		return ResponseEntity.ok(userMapper.toResponse(user));
	}

	@Override
	public ResponseEntity<UserResponse> updateUser(UUID id, UserUpdateRequest userUpdateRequest) {
		var command = userMapper.toCommand(id, userUpdateRequest, authResolver.currentUser());
		var user = updateUserUseCase.execute(command);
		return ResponseEntity.ok(userMapper.toResponse(user));
	}

	@Override
	public ResponseEntity<UserResponse> updateUserRoles(UUID id, List<CenterRoleRequest> centerRoleRequest) {
		var command = userMapper.toUpdateUserRolesCommand(id, centerRoleRequest, authResolver.currentUser());
		var user = updateUserRolesUseCase.execute(command);
		return ResponseEntity.ok(userMapper.toResponse(user));
	}

	@Override
	public ResponseEntity<MessageResponse> updatePassword(UUID id, PasswordUpdateRequest passwordUpdateRequest) {
		var command = userMapper.toUpdatePasswordCommand(id, passwordUpdateRequest, authResolver.currentUser());
		updatePasswordUseCase.execute(command);
		return ResponseEntity.ok(userMapper.toMessageResponse("Password updated successfully"));
	}

	@Override
	public ResponseEntity<Void> deleteUser(UUID id) {
		var command = userMapper.toDeleteUserCommand(id, authResolver.currentUser());
		deleteUserUseCase.execute(command);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<GetMyBookings200Response> getUserBookings(UUID userId, Integer page, Integer size,
	                                                                 String sort) {
		var pageResult = getBookingHistoryUseCase.execute(new GetBookingHistoryQuery(userId, page, size));
		return ResponseEntity.ok(bookingMapper.toPagedBookingHistory(pageResult));
	}
}

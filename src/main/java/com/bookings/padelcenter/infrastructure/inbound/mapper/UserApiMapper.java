package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.command.DeleteUserCommand;
import com.bookings.padelcenter.application.command.UpdatePasswordCommand;
import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.application.command.UpdateUserRolesCommand;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.CenterRole;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CenterRoleRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.UpdatePasswordRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.UpdateUserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.AuditableResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateUserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateUserResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.UpdatePasswordResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.UpdateUserResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserApiMapper {

	public CreateUserCommand toCommand(CreateUserRequest request) {
		return new CreateUserCommand(
				request.firstName(),
				request.lastName(),
				request.email(),
				request.password(),
				request.phoneNumber()
		);
	}

	public CreateUserResponse toResponse(User user) {
		var audit = new AuditableResponse(
				user.audit().createdBy(),
				user.audit().createdAt(),
				user.audit().modifiedBy(),
				user.audit().modifiedAt(),
				user.audit().deletedBy(),
				user.audit().deletedAt()
		);
		return new CreateUserResponse(
				user.id(),
				user.firstName(),
				user.lastName(),
				user.email(),
				user.phoneNumber(),
				audit
		);
	}

	public UpdateUserCommand toCommand(UUID id, UpdateUserRequest request) {
		return new UpdateUserCommand(
				id,
				request.firstName(),
				request.lastName(),
				request.email(),
				request.password(),
				request.phoneNumber()
		);
	}

	public UpdateUserResponse toUpdateUserResponse(User user) {
		return new UpdateUserResponse(
			user.id(),
			user.firstName(),
			user.lastName(),
			user.email(),
			user.phoneNumber(),
			new AuditableResponse(
				user.audit().createdBy(),
				user.audit().createdAt(),
				user.audit().modifiedBy(),
				user.audit().modifiedAt(),
				user.audit().deletedBy(),
				user.audit().deletedAt()
			)
		);
	}

	public UpdateUserRolesCommand toUpdateUserRolesCommand(UUID id, Set<CenterRoleRequest> rolesRequest) {
		var newRoles = rolesRequest.stream()
				.map(this::toCenterRoleDomain)
				.collect(Collectors.toSet());
		// TODO(phase-8): extract authenticated user from SecurityContext
		return new UpdateUserRolesCommand(id, newRoles, new AuthenticatedUser(null));
	}

	public DeleteUserCommand toDeleteUserCommand(UUID id) {
		// TODO(phase-8): extract authenticated user from SecurityContext
		return new DeleteUserCommand(id, new AuthenticatedUser(null));
	}

	public UpdatePasswordCommand toUpdatePasswordCommand(UUID userId, UpdatePasswordRequest request) {
		// TODO(phase-8): extract authenticated user from SecurityContext
		return new UpdatePasswordCommand(
			userId,
			request.currentPassword(),
			request.newPassword(),
			new AuthenticatedUser(null)
		);
	}

	public UpdatePasswordResponse toUpdatePasswordResponse(User user) {
		return new UpdatePasswordResponse(
			user.id(),
			"Password updated successfully"
		);
	}

	private CenterRole toCenterRoleDomain(CenterRoleRequest request) {
		return new CenterRole(
				request.centerId(),
				request.role(),
				Auditable.newAudit()
		);
	}
}

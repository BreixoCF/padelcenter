package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.command.DeleteUserCommand;
import com.bookings.padelcenter.application.command.UpdatePasswordCommand;
import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.application.command.UpdateUserRolesCommand;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.CenterRole;
import com.bookings.padelcenter.domain.model.Role;
import com.bookings.padelcenter.domain.model.User;
import com.padelcenter.infrastructure.web.generated.model.AuditableResponse;
import com.padelcenter.infrastructure.web.generated.model.CenterRoleRequest;
import com.padelcenter.infrastructure.web.generated.model.MessageResponse;
import com.padelcenter.infrastructure.web.generated.model.PasswordUpdateRequest;
import com.padelcenter.infrastructure.web.generated.model.UserRequest;
import com.padelcenter.infrastructure.web.generated.model.UserResponse;
import com.padelcenter.infrastructure.web.generated.model.UserUpdateRequest;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserApiMapper {

	public CreateUserCommand toCommand(UserRequest request) {
		return new CreateUserCommand(
				request.getFirstName(),
				request.getLastName(),
				request.getEmail(),
				request.getPassword(),
				request.getPhoneNumber()
		);
	}

	public UserResponse toResponse(User user) {
		return new UserResponse()
				.id(user.id())
				.firstName(user.firstName())
				.lastName(user.lastName())
				.email(user.email())
				.phoneNumber(user.phoneNumber())
				.audit(toAuditResponse(user.audit()));
	}

	public UpdateUserCommand toCommand(UUID id, UserUpdateRequest request) {
		return new UpdateUserCommand(
				id,
				request.getFirstName(),
				request.getLastName(),
				request.getEmail(),
				request.getPassword(),
				request.getPhoneNumber()
		);
	}

	public UpdateUserRolesCommand toUpdateUserRolesCommand(UUID id, List<CenterRoleRequest> rolesRequest,
	                                                        AuthenticatedUser authenticatedUser) {
		var newRoles = rolesRequest.stream()
				.map(this::toCenterRoleDomain)
				.collect(Collectors.toSet());
		return new UpdateUserRolesCommand(id, newRoles, authenticatedUser);
	}

	public DeleteUserCommand toDeleteUserCommand(UUID id, AuthenticatedUser authenticatedUser) {
		return new DeleteUserCommand(id, authenticatedUser);
	}

	public UpdatePasswordCommand toUpdatePasswordCommand(UUID userId, PasswordUpdateRequest request,
	                                                      AuthenticatedUser authenticatedUser) {
		return new UpdatePasswordCommand(
				userId,
				request.getCurrentPassword(),
				request.getNewPassword(),
				authenticatedUser
		);
	}

	public MessageResponse toMessageResponse(String message) {
		return new MessageResponse().message(message);
	}

	AuditableResponse toAuditResponse(Auditable audit) {
		if (audit == null) return null;
		return new AuditableResponse()
				.createdBy(audit.createdBy())
				.createdAt(audit.createdAt() != null ? audit.createdAt().atOffset(ZoneOffset.UTC) : null)
				.modifiedBy(audit.modifiedBy())
				.modifiedAt(audit.modifiedAt() != null ? audit.modifiedAt().atOffset(ZoneOffset.UTC) : null)
				.deletedBy(audit.deletedBy())
				.deletedAt(audit.deletedAt() != null ? audit.deletedAt().atOffset(ZoneOffset.UTC) : null);
	}

	private CenterRole toCenterRoleDomain(CenterRoleRequest request) {
		return new CenterRole(
				request.getCenterId(),
				Role.fromDescription(request.getRole().getValue()),
				Auditable.newAudit()
		);
	}
}

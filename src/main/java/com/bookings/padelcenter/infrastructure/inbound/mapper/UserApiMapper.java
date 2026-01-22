package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.application.command.UpdateUserRolesCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.CenterRole;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CenterRoleRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.UpdateUserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.AuditableResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateUserRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateUserResponse;
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
			user.passwordHash(),
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
		//TODO: In a real application, this should be the authenticated user
		return new UpdateUserRolesCommand(id, newRoles, UUID.randomUUID());
	}

	private CenterRole toCenterRoleDomain(CenterRoleRequest request) {
		return new CenterRole(
				request.centerId(),
				request.role(),
				Auditable.newAudit()
		);
	}
}

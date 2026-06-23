package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.CenterRoleEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {

	private final CenterRolePersistenceMapper centerRolePersistenceMapper;
	private final AuditablePersistenceMapper auditablePersistenceMapper;

	public UserEntity toEntity(User user) {
		return toEntity(user, null);
	}

	public UserEntity toEntity(User user, UserEntity existingEntity) {
		var entity = existingEntity != null ? existingEntity : new UserEntity();
		if (user.userId() != null) {
			entity.setUserId(user.userId());
		}
		entity.setFirstName(user.firstName());
		entity.setLastName(user.lastName());
		entity.setEmail(user.email());
		entity.setPasswordHash(user.passwordHash());
		entity.setPhoneNumber(user.phoneNumber());
		if (user.centerRoles() != null) {
			var existingByCenterId = existingEntity == null ? Map.<java.util.UUID, CenterRoleEntity>of()
					: existingEntity.getCenterRoles().stream()
							.collect(Collectors.toMap(cr -> cr.getCenter().getCenterId(), cr -> cr));
			var roles = user.centerRoles().stream()
					.map(role -> centerRolePersistenceMapper.toEntity(role, entity, existingByCenterId.get(role.centerId())))
					.collect(Collectors.toSet());
			entity.getCenterRoles().removeIf(roleEntity -> !roles.contains(roleEntity));
			entity.getCenterRoles().addAll(roles);
		} else {
			entity.getCenterRoles().clear();
		}
		auditablePersistenceMapper.mapToEntity(user.audit(), entity);
		return entity;
	}

	public User toDomain(UserEntity entity) {
		if (entity == null) return null;
		var centerRoles = entity.getCenterRoles()
				.stream()
				.map(centerRolePersistenceMapper::toDomain)
				.collect(Collectors.toSet());
		var auditable = new Auditable(
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getModifiedBy(),
				entity.getModifiedAt(),
				entity.getDeletedBy(),
				entity.getDeletedAt()
		);
		return new User(
				entity.getUserId(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getPhoneNumber(),
				centerRoles,
				auditable
		);
	}
}

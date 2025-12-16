package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {

	private final CenterRolePersistenceMapper centerRolePersistenceMapper;

	public UserEntity toEntity(User user) {
		var entity = new UserEntity();
		if (user.id() != null) {
			entity.setUserId(user.id());
		}
		entity.setFirstName(user.firstName());
		entity.setLastName(user.lastName());
		entity.setEmail(user.email());
		entity.setPasswordHash(user.passwordHash());
		entity.setPhoneNumber(user.phoneNumber());
		return entity;
	}

	public User toDomain(UserEntity entity) {
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

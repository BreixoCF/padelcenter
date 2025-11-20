package com.bookings.padelcenter.infrastructure.outbound.mapper;

import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {

	public UserEntity toEntity(User user) {
		UserEntity entity = new UserEntity();
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
		return new User(
				entity.getUserId(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getPhoneNumber(),
				entity.getCenterMemberships()
		);
	}
}

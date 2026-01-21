package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.CenterEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CenterPersistenceMapper {

	private final UserPersistenceMapper userPersistenceMapper;

	public CenterEntity toEntity(Center center) {
		var entity = new CenterEntity();
		if (center.id() != null) {
			entity.setCenterId(center.id());
		}
		entity.setName(center.name());
		entity.setAddress(center.address());
		entity.setPhoneNumber(center.phoneNumber());
		entity.setCity(center.city());
		entity.setEmail(center.email());
		return entity;
	}

	public Center toDomain(CenterEntity entity) {
		var manager = userPersistenceMapper.toDomain(entity.getManager());
		var auditable = new Auditable(
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getModifiedBy(),
				entity.getModifiedAt(),
				entity.getDeletedBy(),
				entity.getDeletedAt()
		);
		return new Center(
				entity.getCenterId(),
				entity.getName(),
				entity.getAddress(),
				entity.getCity(),
				entity.getPhoneNumber(),
				entity.getEmail(),
				manager,
				auditable
		);
	}
}

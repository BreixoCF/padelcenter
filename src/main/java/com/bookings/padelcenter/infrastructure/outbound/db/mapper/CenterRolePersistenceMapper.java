package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.CenterRole;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.CenterEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.CenterRoleEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CenterRolePersistenceMapper {

	private final AuditablePersistenceMapper auditablePersistenceMapper;

	public CenterRoleEntity toEntity(CenterRole centerRole, UserEntity userEntity) {
		var entity = new CenterRoleEntity();
		entity.setUser(userEntity);
		if (centerRole.centerId() != null) {
			var centerEntity = new CenterEntity();
			centerEntity.setCenterId(centerRole.centerId());
			entity.setCenter(centerEntity);
		}
		auditablePersistenceMapper.mapToEntity(centerRole.audit(), entity);
		entity.setRole(centerRole.role());
		return entity;
	}

	public CenterRole toDomain(CenterRoleEntity entity) {
		var auditable = new Auditable(
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getModifiedBy(),
				entity.getModifiedAt(),
				entity.getDeletedBy(),
				entity.getDeletedAt()
		);
		var centerId = entity.getCenter() != null ? entity.getCenter().getCenterId() : null;
		return new CenterRole(
				centerId,
				entity.getRole(),
				auditable
		);
	}

}

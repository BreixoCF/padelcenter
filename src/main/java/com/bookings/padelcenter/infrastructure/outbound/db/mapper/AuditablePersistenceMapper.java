package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.AuditableEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditablePersistenceMapper {
	public void mapToEntity(Auditable domain, AuditableEntity<UUID> entity) {
		if (domain == null || entity == null) {
			return;
		}
		entity.setCreatedBy(domain.createdBy());
		entity.setCreatedAt(domain.createdAt());
		entity.setModifiedBy(domain.modifiedBy());
		entity.setModifiedAt(domain.modifiedAt());
		entity.setDeletedBy(domain.deletedBy());
		entity.setDeletedAt(domain.deletedAt());
	}
}

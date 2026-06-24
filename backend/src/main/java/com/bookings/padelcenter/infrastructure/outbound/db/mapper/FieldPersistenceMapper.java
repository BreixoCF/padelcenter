package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.CenterEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.FieldEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FieldPersistenceMapper {

	private final CenterPersistenceMapper centerPersistenceMapper;
	private final AuditablePersistenceMapper auditablePersistenceMapper;

	public FieldEntity toEntity(Field field) {
		var entity = new FieldEntity();
		if (field.fieldId() != null) {
			entity.setFieldId(field.fieldId());
		}
		entity.setType(field.type());
		entity.setName(field.name());
		entity.setPricePerHour(field.pricePerHour());
		entity.setIsAvailable(field.isAvailable());
		if (field.center() != null) {
			entity.setCenter(centerPersistenceMapper.toEntity(field.center()));
		}
		auditablePersistenceMapper.mapToEntity(field.audit(), entity);
		return entity;
	}

	public Field toDomain(FieldEntity entity) {
		var center = centerPersistenceMapper.toDomain(entity.getCenter());
		var auditable = new Auditable(
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getModifiedBy(),
				entity.getModifiedAt(),
				entity.getDeletedBy(),
				entity.getDeletedAt()
		);
		return new Field(
				entity.getFieldId(),
				entity.getName(),
				entity.getType(),
				entity.getPricePerHour(),
				entity.getIsAvailable(),
				center,
				auditable
		);
	}
}

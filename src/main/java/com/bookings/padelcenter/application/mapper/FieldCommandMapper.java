package com.bookings.padelcenter.application.mapper;

import com.bookings.padelcenter.application.command.CreateFieldCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Field;
import org.springframework.stereotype.Component;

@Component
public class FieldCommandMapper {

	public Field toDomain(CreateFieldCommand command) {
		var auditable = Auditable.newAudit();
		return new Field(
				null,
				command.name(),
				command.type(),
				command.pricePerHour(),
				command.isAvailable(),
				null,
				auditable
		);
	}
}

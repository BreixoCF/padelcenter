package com.bookings.padelcenter.application.mapper;

import com.bookings.padelcenter.application.command.CreateFieldCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.Field;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

@Component
public class FieldCommandMapper {

	public Field toDomain(CreateFieldCommand command, Center center) {
		return new Field(
			UuidCreator.getTimeOrderedEpoch(),
			command.name(),
			command.type(),
			command.pricePerHour(),
			command.isAvailable(),
			center,
			Auditable.newAudit()
		);
	}
}

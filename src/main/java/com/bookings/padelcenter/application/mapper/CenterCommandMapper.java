package com.bookings.padelcenter.application.mapper;

import com.bookings.padelcenter.application.command.CreateCenterCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

@Component
public class CenterCommandMapper {

	public Center toDomain(CreateCenterCommand command) {
		var auditable = Auditable.newAudit();
		return new Center(
				UuidCreator.getTimeOrderedEpoch(),
				command.name(),
				command.address(),
				command.city(),
				command.phoneNumber(),
				command.email(),
				null,
				auditable
		);
	}
}

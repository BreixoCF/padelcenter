package com.bookings.padelcenter.application.mapper;

import com.bookings.padelcenter.application.command.CreateCenterCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import org.springframework.stereotype.Component;

@Component
public class CenterCommandMapper {

	public Center toDomain(CreateCenterCommand command) {
		var auditable = Auditable.newAudit();
		return new Center(
				null,
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

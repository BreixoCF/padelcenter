package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateCenterCommand;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.AuditableResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateCenterRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CenterSummaryResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateCenterResponse;
import org.springframework.stereotype.Component;

@Component
public class CenterApiMapper {

	public CreateCenterCommand toCommand(CreateCenterRequest request) {
		return new CreateCenterCommand(
				request.name(),
				request.address(),
				request.city(),
				request.phoneNumber(),
				request.email()
		);
	}

	public CreateCenterResponse toResponse(Center center) {
		var audit = new AuditableResponse(
				center.audit().createdBy(),
				center.audit().createdAt(),
				center.audit().modifiedBy(),
				center.audit().modifiedAt(),
				center.audit().deletedBy(),
				center.audit().deletedAt()
		);
		return new CreateCenterResponse(
				center.id(),
				center.name(),
				center.address(),
				center.city(),
				center.phoneNumber(),
				center.email(),
				audit
		);
	}

	public CenterSummaryResponse toSummaryResponse(Center center) {
		return new CenterSummaryResponse(
				center.name(),
				center.address(),
				center.city()
		);
	}
}

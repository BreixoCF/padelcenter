package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateFieldCommand;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.AuditableResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateFieldRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateFieldResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FieldApiMapper {

	private final CenterApiMapper centerApiMapper;

	public CreateFieldCommand toCommand(UUID centerId, CreateFieldRequest request) {
		return new CreateFieldCommand(
				request.name(),
				request.type(),
				request.pricePerHour(),
				request.isAvailable(),
				centerId
		);
	}

	public CreateFieldResponse toResponse(Field field) {
		var audit = new AuditableResponse(
				field.audit().createdBy(),
				field.audit().createdAt(),
				field.audit().modifiedBy(),
				field.audit().modifiedAt(),
				field.audit().deletedBy(),
				field.audit().deletedAt()
		);
		return new CreateFieldResponse(
				field.id(),
				field.name(),
				field.type(),
				field.pricePerHour(),
				field.isAvailable(),
				centerApiMapper.toSummaryResponse(field.center()),
				audit
		);
	}
}

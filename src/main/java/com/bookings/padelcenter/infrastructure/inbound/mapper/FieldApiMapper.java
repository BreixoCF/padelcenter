package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateFieldCommand;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.PageResult;
import com.padelcenter.infrastructure.web.generated.model.AuditableResponse;
import com.padelcenter.infrastructure.web.generated.model.FieldRequest;
import com.padelcenter.infrastructure.web.generated.model.FieldResponse;
import com.padelcenter.infrastructure.web.generated.model.FieldSummaryResponse;
import com.padelcenter.infrastructure.web.generated.model.ListFields200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FieldApiMapper {

	private final CenterApiMapper centerApiMapper;

	public CreateFieldCommand toCommand(UUID centerId, FieldRequest request) {
		return new CreateFieldCommand(
				request.getName(),
				request.getType(),
				request.getPricePerHour() != null ? BigDecimal.valueOf(request.getPricePerHour()) : null,
				request.getIsAvailable(),
				centerId
		);
	}

	public FieldResponse toResponse(Field field) {
		return new FieldResponse()
				.fieldId(field.fieldId())
				.name(field.name())
				.type(field.type())
				.pricePerHour(field.pricePerHour() != null ? field.pricePerHour().doubleValue() : null)
				.isAvailable(field.isAvailable())
				.center(centerApiMapper.toSummaryResponse(field.center()))
				.audit(toAuditResponse(field));
	}

	public FieldSummaryResponse toSummaryResponse(Field field) {
		return new FieldSummaryResponse(field.fieldId(), field.name())
				.type(field.type())
				.center(centerApiMapper.toSummaryResponse(field.center()));
	}

	public ListFields200Response toPagedResponse(PageResult<Field> page) {
		List<FieldResponse> content = page.content().stream().map(this::toResponse).toList();
		return new ListFields200Response(
				content,
				page.totalElements(),
				page.totalPages(),
				page.page(),
				page.size()
		);
	}

	private AuditableResponse toAuditResponse(Field field) {
		var audit = field.audit();
		if (audit == null) return null;
		return new AuditableResponse()
				.createdBy(audit.createdBy())
				.createdAt(audit.createdAt() != null ? audit.createdAt().atOffset(ZoneOffset.UTC) : null)
				.modifiedBy(audit.modifiedBy())
				.modifiedAt(audit.modifiedAt() != null ? audit.modifiedAt().atOffset(ZoneOffset.UTC) : null)
				.deletedBy(audit.deletedBy())
				.deletedAt(audit.deletedAt() != null ? audit.deletedAt().atOffset(ZoneOffset.UTC) : null);
	}
}

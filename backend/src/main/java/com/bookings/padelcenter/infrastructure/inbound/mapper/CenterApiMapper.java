package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateCenterCommand;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.PageResult;
import com.padelcenter.infrastructure.web.generated.model.AuditableResponse;
import com.padelcenter.infrastructure.web.generated.model.CenterRequest;
import com.padelcenter.infrastructure.web.generated.model.CenterResponse;
import com.padelcenter.infrastructure.web.generated.model.CenterSummaryResponse;
import com.padelcenter.infrastructure.web.generated.model.ListCenters200Response;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class CenterApiMapper {

	public CreateCenterCommand toCommand(CenterRequest request) {
		return new CreateCenterCommand(
				request.getName(),
				request.getAddress(),
				request.getCity(),
				request.getPhoneNumber(),
				request.getEmail()
		);
	}

	public CenterResponse toResponse(Center center) {
		return new CenterResponse()
				.centerId(center.centerId())
				.name(center.name())
				.address(center.address())
				.city(center.city())
				.phoneNumber(center.phoneNumber())
				.email(center.email())
				.audit(toAuditResponse(center));
	}

	public CenterSummaryResponse toSummaryResponse(Center center) {
		return new CenterSummaryResponse(center.centerId(), center.name(), center.address(), center.city());
	}

	public ListCenters200Response toPagedResponse(PageResult<Center> page) {
		List<CenterResponse> content = page.content().stream().map(this::toResponse).toList();
		return new ListCenters200Response(
				content,
				page.totalElements(),
				page.totalPages(),
				page.page(),
				page.size()
		);
	}

	private AuditableResponse toAuditResponse(Center center) {
		var audit = center.audit();
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

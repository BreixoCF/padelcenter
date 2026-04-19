package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CancelBookingCommand;
import com.bookings.padelcenter.application.command.CreateBookingCommand;
import com.bookings.padelcenter.application.command.UpdateBookingCommand;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;
import com.padelcenter.infrastructure.web.generated.model.AuditableResponse;
import com.padelcenter.infrastructure.web.generated.model.BookingRequest;
import com.padelcenter.infrastructure.web.generated.model.BookingResponse;
import com.padelcenter.infrastructure.web.generated.model.BookingUpdateRequest;
import com.padelcenter.infrastructure.web.generated.model.CenterBookingSummary;
import com.padelcenter.infrastructure.web.generated.model.GetCenterBookings200Response;
import com.padelcenter.infrastructure.web.generated.model.GetMyBookings200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class BookingApiMapper {

	private final FieldApiMapper fieldApiMapper;

	public CreateBookingCommand toCommand(BookingRequest request, AuthenticatedUser authenticatedUser) {
		return new CreateBookingCommand(
				authenticatedUser.userId(),
				request.getFieldId(),
				request.getStartTime().toLocalDateTime(),
				request.getEndTime().toLocalDateTime(),
				BigDecimal.valueOf(request.getTotalPrice())
		);
	}

	public BookingResponse toResponse(Booking booking) {
		var fieldSummary = fieldApiMapper.toSummaryResponse(booking.field());
		var response = new BookingResponse(
				booking.bookingId(),
				booking.user().userId(),
				fieldSummary,
				booking.startTime().atOffset(ZoneOffset.UTC),
				booking.endTime().atOffset(ZoneOffset.UTC),
				booking.totalPrice() != null ? booking.totalPrice().doubleValue() : null,
				booking.bookedAt() != null ? booking.bookedAt().atOffset(ZoneOffset.UTC) : null,
				BookingResponse.StatusEnum.fromValue(booking.status().name())
		);
		response.audit(toAuditResponse(booking));
		return response;
	}

	public GetMyBookings200Response toPagedBookingHistory(PageResult<Booking> pageResult) {
		var content = pageResult.content().stream().map(this::toResponse).toList();
		return new GetMyBookings200Response(
				content,
				pageResult.totalElements(),
				pageResult.totalPages(),
				pageResult.page(),
				pageResult.size()
		);
	}

	public GetMyBookings200Response toMyBookingsPagedResponse(PageResult<Booking> pageResult) {
		return toPagedBookingHistory(pageResult);
	}

	public UpdateBookingCommand toUpdateCommand(Long bookingId, BookingUpdateRequest request,
	                                             AuthenticatedUser authenticatedUser) {
		return new UpdateBookingCommand(
				bookingId,
				request.getStartTime().toLocalDateTime(),
				request.getEndTime().toLocalDateTime(),
				BigDecimal.valueOf(request.getTotalPrice()),
				authenticatedUser
		);
	}

	public CenterBookingSummary toCenterBookingSummary(Booking booking) {
		return new CenterBookingSummary(
				booking.bookingId(),
				booking.field().fieldId(),
				booking.field().name(),
				booking.user().userId(),
				booking.startTime().atOffset(ZoneOffset.UTC),
				booking.endTime().atOffset(ZoneOffset.UTC),
				booking.totalPrice() != null ? booking.totalPrice().doubleValue() : null,
				CenterBookingSummary.StatusEnum.fromValue(booking.status().name())
		);
	}

	public GetCenterBookings200Response toPagedCenterBookings(PageResult<Booking> pageResult) {
		var content = pageResult.content().stream().map(this::toCenterBookingSummary).toList();
		return new GetCenterBookings200Response(
				content,
				pageResult.totalElements(),
				pageResult.totalPages(),
				pageResult.page(),
				pageResult.size()
		);
	}

	public CancelBookingCommand toCancelCommand(Long bookingId, AuthenticatedUser authenticatedUser) {
		return new CancelBookingCommand(bookingId, authenticatedUser);
	}

	private AuditableResponse toAuditResponse(Booking booking) {
		var audit = booking.audit();
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

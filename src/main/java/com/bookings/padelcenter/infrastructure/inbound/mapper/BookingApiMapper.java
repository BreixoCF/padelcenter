package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CancelBookingCommand;
import com.bookings.padelcenter.application.command.CreateBookingCommand;
import com.bookings.padelcenter.application.command.UpdateBookingCommand;
import com.bookings.padelcenter.application.query.GetBookingHistoryQuery;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.model.Booking;
import com.padelcenter.infrastructure.web.generated.model.AuditableResponse;
import com.padelcenter.infrastructure.web.generated.model.BookingRequest;
import com.padelcenter.infrastructure.web.generated.model.BookingResponse;
import com.padelcenter.infrastructure.web.generated.model.BookingUpdateRequest;
import com.padelcenter.infrastructure.web.generated.model.GetUserBookings200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

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

	public GetBookingHistoryQuery toQuery(UUID userId) {
		return new GetBookingHistoryQuery(userId);
	}

	public BookingResponse toResponse(Booking booking) {
		var fieldSummary = fieldApiMapper.toSummaryResponse(booking.field());
		var response = new BookingResponse(
				booking.id(),
				booking.user().id(),
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

	public GetUserBookings200Response toPagedBookingHistory(List<Booking> bookings, int page, int size) {
		var content = bookings.stream().map(this::toResponse).toList();
		int totalElements = content.size();
		int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
		return new GetUserBookings200Response(
				content,
				(long) totalElements,
				totalPages == 0 ? 1 : totalPages,
				page,
				size
		);
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

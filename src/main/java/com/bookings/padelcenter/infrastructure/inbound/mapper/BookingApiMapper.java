package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CancelBookingCommand;
import com.bookings.padelcenter.application.command.CreateBookingCommand;
import com.bookings.padelcenter.application.command.UpdateBookingCommand;
import com.bookings.padelcenter.application.query.GetBookingHistoryQuery;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateBookingRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.UpdateBookingRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.AuditableResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.BookingHistoryResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CancelBookingResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CenterSummaryResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateBookingResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.FieldSummaryResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.UpdateBookingResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BookingApiMapper {

	public CreateBookingCommand toCommand(CreateBookingRequest request) {
		return new CreateBookingCommand(
			request.userId(),
			request.fieldId(),
			request.startTime(),
			request.endTime(),
			request.totalPrice()
		);
	}

	public CreateBookingResponse toResponse(Booking booking) {
		var audit = new AuditableResponse(
			booking.audit().createdBy(),
			booking.audit().createdAt(),
			booking.audit().modifiedBy(),
			booking.audit().modifiedAt(),
			booking.audit().deletedBy(),
			booking.audit().deletedAt()
		);

		return new CreateBookingResponse(
			booking.id(),
			booking.user().id(),
			booking.field().id(),
			booking.startTime(),
			booking.endTime(),
			booking.totalPrice(),
			booking.bookedAt(),
			booking.status().name(),
			audit
		);
	}

	public GetBookingHistoryQuery toQuery(UUID userId) {
		return new GetBookingHistoryQuery(userId);
	}

	public List<BookingHistoryResponse> toBookingHistoryResponse(List<Booking> bookings) {
		return bookings.stream()
			.map(this::toBookingHistoryItem)
			.collect(Collectors.toList());
	}

	private BookingHistoryResponse toBookingHistoryItem(Booking booking) {
		var centerSummary = new CenterSummaryResponse(
			booking.field().center().id().toString(),
			booking.field().center().name(),
			booking.field().center().city()
		);

		var fieldSummary = new FieldSummaryResponse(
			booking.field().id(),
			booking.field().name(),
			booking.field().type(),
			centerSummary
		);

		return new BookingHistoryResponse(
			booking.id(),
			fieldSummary,
			booking.startTime(),
			booking.endTime(),
			booking.totalPrice(),
			booking.bookedAt(),
			booking.status().name()
		);
	}

	public UpdateBookingCommand toUpdateCommand(Long bookingId, UpdateBookingRequest request) {
		return new UpdateBookingCommand(
			bookingId,
			request.startTime(),
			request.endTime(),
			request.totalPrice(),
			null
		);
	}

	public UpdateBookingResponse toUpdateResponse(Booking booking) {
		var audit = new AuditableResponse(
			booking.audit().createdBy(),
			booking.audit().createdAt(),
			booking.audit().modifiedBy(),
			booking.audit().modifiedAt(),
			booking.audit().deletedBy(),
			booking.audit().deletedAt()
		);

		return new UpdateBookingResponse(
			booking.id(),
			booking.user().id(),
			booking.field().id(),
			booking.startTime(),
			booking.endTime(),
			booking.totalPrice(),
			booking.bookedAt(),
			booking.status().name(),
			audit
		);
	}

	public CancelBookingCommand toCancelCommand(Long bookingId) {
		// TODO: In a real application, modifiedBy should be the authenticated user
		return new CancelBookingCommand(bookingId, null);
	}

	public CancelBookingResponse toCancelResponse(Booking booking) {
		return new CancelBookingResponse(
			booking.id(),
			booking.status().name(),
			"Booking has been successfully cancelled"
		);
	}
}

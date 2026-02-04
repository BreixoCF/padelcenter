package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateBookingCommand;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateBookingRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.AuditableResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateBookingResponse;
import org.springframework.stereotype.Component;

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
}

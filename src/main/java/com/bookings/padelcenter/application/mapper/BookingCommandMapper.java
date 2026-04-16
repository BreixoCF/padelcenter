package com.bookings.padelcenter.application.mapper;

import com.bookings.padelcenter.application.command.CreateBookingCommand;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.BookingStatus;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BookingCommandMapper {

	public Booking toDomain(CreateBookingCommand command, User user, Field field) {
		return new Booking(
			null,
			user,
			field,
			command.startTime(),
			command.endTime(),
			command.totalPrice(),
			Instant.now(),
			BookingStatus.CONFIRMED,
			Auditable.newAudit()
		);
	}
}

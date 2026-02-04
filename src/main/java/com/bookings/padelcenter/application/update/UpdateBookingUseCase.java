package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateBookingCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateBookingUseCase implements CommandUseCase<UpdateBookingCommand, Booking> {

	private final BookingRepository bookingRepository;

	@Override
	public Booking execute(UpdateBookingCommand command) {
		var booking = bookingRepository.findById(command.bookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

		var updatedBooking = booking.updateDetails(
			command.startTime(),
			command.endTime(),
			command.totalPrice(),
			command.modifiedBy()
		);

		return bookingRepository.save(updatedBooking);
	}
}

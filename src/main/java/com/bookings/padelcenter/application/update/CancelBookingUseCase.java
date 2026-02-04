package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.CancelBookingCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.BookingAlreadyCancelledException;
import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelBookingUseCase implements CommandUseCase<CancelBookingCommand, Booking> {

	private final BookingRepository bookingRepository;

	@Override
	public Booking execute(CancelBookingCommand command) {
		var booking = bookingRepository.findById(command.bookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

		if (booking.isCancelled()) {
			throw new BookingAlreadyCancelledException(command.bookingId());
		}

		var cancelledBooking = booking.cancel(command.modifiedBy());
		return bookingRepository.save(cancelledBooking);
	}
}

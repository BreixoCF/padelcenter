package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.CancelBookingCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.BookingAlreadyCancelledException;
import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CancelBookingUseCase implements CommandUseCase<CancelBookingCommand, Booking> {

	private final BookingRepository bookingRepository;

	@Override
	public Booking execute(CancelBookingCommand command) {
		log.debug("booking.cancel.start bookingId={}",
			command.bookingId());

		var booking = bookingRepository.findById(command.bookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

		if (booking.isCancelled()) {
			log.warn("booking.cancel.failed bookingId={} reason=already_cancelled",
				command.bookingId());
			throw new BookingAlreadyCancelledException(command.bookingId());
		}

		var cancelledBooking = booking.cancel(command.authenticatedUser().userId());
		var savedBooking = bookingRepository.save(cancelledBooking);

		log.info("booking.cancelled bookingId={} fieldId={}",
			savedBooking.bookingId(), savedBooking.field().fieldId());

		return savedBooking;
	}
}

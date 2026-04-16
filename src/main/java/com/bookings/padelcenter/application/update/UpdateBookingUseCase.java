package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateBookingCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
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
public class UpdateBookingUseCase implements CommandUseCase<UpdateBookingCommand, Booking> {

	private final BookingRepository bookingRepository;

	@Override
	public Booking execute(UpdateBookingCommand command) {
		log.debug("booking.update.start bookingId={} start={} end={}",
			command.bookingId(), command.startTime(), command.endTime());

		var booking = bookingRepository.findById(command.bookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

		var updatedBooking = booking.updateDetails(
			command.startTime(),
			command.endTime(),
			command.totalPrice(),
			command.authenticatedUser().userId()
		);

		bookingRepository.save(updatedBooking);

		log.info("booking.updated bookingId={} start={} end={} totalPrice={}",
			updatedBooking.id(), updatedBooking.startTime(),
			updatedBooking.endTime(), updatedBooking.totalPrice());

		return updatedBooking;
	}
}

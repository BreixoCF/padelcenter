package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateBookingCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.exception.BookingOverlapException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateBookingUseCase implements CommandUseCase<UpdateBookingCommand, Booking> {

	private final BookingRepository bookingRepository;

	@Override
	@PreAuthorize("@bookingOwnerEvaluator.isOwner(@authResolver.resolveUserId(authentication), #command.bookingId)")
	public Booking execute(UpdateBookingCommand command) {
		log.debug("booking.update.start bookingId={} start={} end={}",
			command.bookingId(), command.startTime(), command.endTime());

		var booking = bookingRepository.findById(command.bookingId())
				.orElseThrow(() -> new BookingNotFoundException(command.bookingId()));

		if (bookingRepository.existsOverlappingBooking(
				booking.field().fieldId(), command.startTime(), command.endTime(), command.bookingId())) {
			throw new BookingOverlapException(
				booking.field().fieldId(), command.startTime(), command.endTime());
		}

		var updatedBooking = booking.updateDetails(
			command.startTime(),
			command.endTime(),
			command.totalPrice(),
			command.authenticatedUser().userId()
		);

		var savedBooking = bookingRepository.save(updatedBooking);

		log.info("booking.updated bookingId={} start={} end={} totalPrice={}",
			savedBooking.bookingId(), savedBooking.startTime(),
			savedBooking.endTime(), savedBooking.totalPrice());

		return savedBooking;
	}
}

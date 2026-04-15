package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetBookingByIdQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetBookingByIdUseCase implements QueryUseCase<GetBookingByIdQuery, Booking> {

	private final BookingRepository bookingRepository;

	@NonNull
	@Override
	public Booking execute(GetBookingByIdQuery query) {
		return bookingRepository.findById(query.bookingId())
				.orElseThrow(() -> new BookingNotFoundException(query.bookingId()));
	}
}

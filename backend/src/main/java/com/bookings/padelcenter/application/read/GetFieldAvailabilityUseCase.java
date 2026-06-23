package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.FieldAvailabilityResult;
import com.bookings.padelcenter.application.query.FieldAvailabilityResult.TimeSlot;
import com.bookings.padelcenter.application.query.GetFieldAvailabilityQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetFieldAvailabilityUseCase implements QueryUseCase<GetFieldAvailabilityQuery, FieldAvailabilityResult> {

	private static final int FIRST_HOUR = 9;
	private static final int LAST_HOUR = 21;

	private final FieldRepository fieldRepository;
	private final BookingRepository bookingRepository;

	@NonNull
	@Override
	public FieldAvailabilityResult execute(GetFieldAvailabilityQuery query) {
		log.debug("field.availability.start fieldId={} date={}", query.fieldId(), query.date());

		fieldRepository.findById(query.fieldId())
				.orElseThrow(() -> new FieldNotFoundException(query.fieldId()));

		var dayStart = query.date().atTime(LocalTime.MIN);
		var dayEnd = query.date().plusDays(1).atTime(LocalTime.MIN);

		var confirmedBookings = bookingRepository.findConfirmedByFieldAndDay(
				query.fieldId(), dayStart, dayEnd);

		var slots = buildSlots(query, confirmedBookings);

		log.debug("field.availability.done fieldId={} date={} totalSlots={} available={}",
				query.fieldId(), query.date(), slots.size(),
				slots.stream().filter(TimeSlot::available).count());

		return new FieldAvailabilityResult(query.fieldId(), query.date(), slots);
	}

	private List<TimeSlot> buildSlots(GetFieldAvailabilityQuery query, List<Booking> confirmedBookings) {
		var slots = new ArrayList<TimeSlot>();
		for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++) {
			var slotStart = query.date().atTime(hour, 0);
			var slotEnd = slotStart.plusHours(1);
			boolean occupied = isOccupied(slotStart, slotEnd, confirmedBookings);
			slots.add(new TimeSlot(slotStart, slotEnd, !occupied));
		}
		return List.copyOf(slots);
	}

	private boolean isOccupied(LocalDateTime slotStart, LocalDateTime slotEnd, List<Booking> bookings) {
		return bookings.stream()
				.anyMatch(b -> b.startTime().isBefore(slotEnd) && b.endTime().isAfter(slotStart));
	}
}

package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateBookingCommand;
import com.bookings.padelcenter.application.mapper.BookingCommandMapper;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateBookingUseCase implements CommandUseCase<CreateBookingCommand, Booking> {

	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;
	private final FieldRepository fieldRepository;
	private final BookingCommandMapper bookingCommandMapper;

	@Override
	public Booking execute(CreateBookingCommand command) {
		var user = userRepository.findById(command.userId())
				.orElseThrow(() -> new UserNotFoundException(command.userId()));

		var field = fieldRepository.findById(command.fieldId())
				.orElseThrow(() -> new FieldNotFoundException(command.fieldId()));

		var bookingToCreate = bookingCommandMapper.toDomain(command, user, field);
		return bookingRepository.save(bookingToCreate);
	}
}

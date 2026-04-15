package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateBookingUseCase;
import com.bookings.padelcenter.application.query.GetBookingByIdQuery;
import com.bookings.padelcenter.application.read.GetBookingByIdUseCase;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.application.update.CancelBookingUseCase;
import com.bookings.padelcenter.application.update.UpdateBookingUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.BookingApiMapper;
import com.padelcenter.infrastructure.web.generated.api.BookingsApi;
import com.padelcenter.infrastructure.web.generated.model.BookingRequest;
import com.padelcenter.infrastructure.web.generated.model.BookingResponse;
import com.padelcenter.infrastructure.web.generated.model.BookingUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BookingController implements BookingsApi {

	private final CreateBookingUseCase createBookingUseCase;
	private final GetBookingByIdUseCase getBookingByIdUseCase;
	private final UpdateBookingUseCase updateBookingUseCase;
	private final CancelBookingUseCase cancelBookingUseCase;
	private final BookingApiMapper bookingApiMapper;

	@Override
	public ResponseEntity<BookingResponse> createBooking(BookingRequest bookingRequest) {
		var command = bookingApiMapper.toCommand(bookingRequest, currentUser());
		var booking = createBookingUseCase.execute(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(bookingApiMapper.toResponse(booking));
	}

	@Override
	public ResponseEntity<BookingResponse> getBookingById(Long bookingId) {
		var booking = getBookingByIdUseCase.execute(new GetBookingByIdQuery(bookingId));
		return ResponseEntity.ok(bookingApiMapper.toResponse(booking));
	}

	@Override
	public ResponseEntity<BookingResponse> updateBooking(Long bookingId, BookingUpdateRequest bookingUpdateRequest) {
		var command = bookingApiMapper.toUpdateCommand(bookingId, bookingUpdateRequest, currentUser());
		var booking = updateBookingUseCase.execute(command);
		return ResponseEntity.ok(bookingApiMapper.toResponse(booking));
	}

	@Override
	public ResponseEntity<BookingResponse> cancelBooking(Long bookingId) {
		var command = bookingApiMapper.toCancelCommand(bookingId, currentUser());
		var booking = cancelBookingUseCase.execute(command);
		return ResponseEntity.ok(bookingApiMapper.toResponse(booking));
	}

	private AuthenticatedUser currentUser() {
		var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		return new AuthenticatedUser(UUID.fromString(auth.getToken().getSubject()));
	}
}

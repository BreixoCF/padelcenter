package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateBookingUseCase;
import com.bookings.padelcenter.application.read.GetBookingHistoryUseCase;
import com.bookings.padelcenter.application.update.CancelBookingUseCase;
import com.bookings.padelcenter.application.update.UpdateBookingUseCase;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateBookingRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.UpdateBookingRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.BookingHistoryResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CancelBookingResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateBookingResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.UpdateBookingResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.BookingApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final CreateBookingUseCase createBookingUseCase;
	private final GetBookingHistoryUseCase getBookingHistoryUseCase;
	private final UpdateBookingUseCase updateBookingUseCase;
	private final CancelBookingUseCase cancelBookingUseCase;
	private final BookingApiMapper bookingApiMapper;

	@PostMapping
	public ResponseEntity<CreateBookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
		var command = bookingApiMapper.toCommand(request);
		var booking = createBookingUseCase.execute(command);
		var response = bookingApiMapper.toResponse(booking);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<List<BookingHistoryResponse>> getBookingHistory(@PathVariable UUID userId) {
		var query = bookingApiMapper.toQuery(userId);
		var bookings = getBookingHistoryUseCase.execute(query);
		var response = bookingApiMapper.toBookingHistoryResponse(bookings);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{bookingId}")
	public ResponseEntity<UpdateBookingResponse> updateBooking(
			@PathVariable Long bookingId,
			@Valid @RequestBody UpdateBookingRequest request) {
		var command = bookingApiMapper.toUpdateCommand(bookingId, request);
		var booking = updateBookingUseCase.execute(command);
		var response = bookingApiMapper.toUpdateResponse(booking);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{bookingId}/cancel")
	public ResponseEntity<CancelBookingResponse> cancelBooking(@PathVariable Long bookingId) {
		var command = bookingApiMapper.toCancelCommand(bookingId);
		var booking = cancelBookingUseCase.execute(command);
		var response = bookingApiMapper.toCancelResponse(booking);
		return ResponseEntity.ok(response);
	}
}


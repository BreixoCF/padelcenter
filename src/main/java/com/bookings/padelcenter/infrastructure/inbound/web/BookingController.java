package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateBookingUseCase;
import com.bookings.padelcenter.application.read.GetBookingHistoryUseCase;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateBookingRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.BookingHistoryResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateBookingResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.BookingApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final CreateBookingUseCase createBookingUseCase;
	private final GetBookingHistoryUseCase getBookingHistoryUseCase;
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
}


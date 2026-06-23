package com.bookings.padelcenter.domain.model;

// TODO: BookingParticipant feature — deferred (see docs/adr/ADR-003-booking-participants-deferred.md)
// Requires:
//   - BookingParticipantRepository port
//   - AddParticipantUseCase / RemoveParticipantUseCase
//   - PATCH /api/v1/bookings/{id}/participants endpoints
//   - OpenAPI schema: booking-participant-response.yaml
public record BookingParticipant(
	Long bookingParticipantId,
	Booking booking,
	User user
) {}

-- Partial index to accelerate the overlap query in BookingJpaRepository.existsOverlappingBooking.
-- Covers: field_id, start_time, end_time WHERE status IN (1=PENDING, 2=CONFIRMED).
-- Cancelled and completed bookings are excluded, keeping the index small and focused.
CREATE INDEX idx_bookings_field_time_status
    ON bookings (field_id, start_time, end_time)
    WHERE status IN (1, 2);

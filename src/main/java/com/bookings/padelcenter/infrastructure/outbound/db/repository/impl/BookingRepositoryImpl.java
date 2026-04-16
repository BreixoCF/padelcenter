package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.BookingEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.BookingPersistenceMapper;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepository {

	private static final ZoneId ZONE = ZoneId.of("UTC");

	private final BookingJpaRepository bookingJpaRepository;
	private final BookingPersistenceMapper mapper;

	@Override
	public Booking save(Booking booking) {
		BookingEntity entity = mapper.toEntity(booking);
		BookingEntity saved = bookingJpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Booking> findById(Long id) {
		return bookingJpaRepository.findById(id)
			.map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Booking> findAll() {
		return bookingJpaRepository.findAll().stream()
			.map(mapper::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsOverlappingBooking(UUID fieldId, LocalDateTime start, LocalDateTime end, Long excludeId) {
		Instant startInstant = start.atZone(ZONE).toInstant();
		Instant endInstant = end.atZone(ZONE).toInstant();
		return bookingJpaRepository.existsOverlappingBooking(fieldId, startInstant, endInstant, excludeId);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Booking> findByUserId(UUID userId, int page, int size) {
		var springPage = bookingJpaRepository.findByUser_UserId(userId, PageRequest.of(page, size));
		var content = springPage.getContent().stream()
			.map(mapper::toDomain)
			.toList();
		return new PageResult<>(
			content,
			springPage.getNumber(),
			springPage.getSize(),
			springPage.getTotalElements(),
			springPage.getTotalPages()
		);
	}
}

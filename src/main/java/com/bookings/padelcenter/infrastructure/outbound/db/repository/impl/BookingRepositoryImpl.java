package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.BookingEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.BookingPersistenceMapper;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepository {

	private final BookingJpaRepository bookingJpaRepository;
	private final BookingPersistenceMapper mapper;

	@Override
	public Booking save(Booking booking) {
		BookingEntity entity = mapper.toEntity(booking);
		BookingEntity saved = bookingJpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	public Optional<Booking> findById(Long id) {
		return bookingJpaRepository.findById(id)
			.map(mapper::toDomain);
	}

	@Override
	public List<Booking> findAll() {
		return bookingJpaRepository.findAll().stream()
			.map(mapper::toDomain)
			.collect(Collectors.toList());
	}

	@Override
	public List<Booking> findByUserId(UUID userId) {
		return bookingJpaRepository.findByUser_UserId(userId).stream()
			.map(mapper::toDomain)
			.collect(Collectors.toList());
	}
}

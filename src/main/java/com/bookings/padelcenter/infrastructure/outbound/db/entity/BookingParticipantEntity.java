package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import com.bookings.padelcenter.domain.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "booking_participants")
public class BookingParticipantEntity extends Auditable<UUID> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long bookingParticipantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false)
	private BookingEntity booking;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
}
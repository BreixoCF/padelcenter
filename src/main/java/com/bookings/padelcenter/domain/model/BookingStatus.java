package com.bookings.padelcenter.domain.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BookingStatus {
	PENDING(1),
	CONFIRMED(2),
	CANCELLED(3),
	COMPLETED(4);

	private final int id;

	BookingStatus(int id) {
		this.id = id;
	}

	public static BookingStatus fromId(int id) {
		return Arrays.stream(values())
				.filter(e -> e.getId() == id)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("BookingStatus not found: " + id));
	}
}

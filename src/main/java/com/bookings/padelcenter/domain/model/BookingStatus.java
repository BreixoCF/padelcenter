package com.bookings.padelcenter.domain.model;

import java.util.Arrays;

public enum BookingStatus {
	PENDING(1),
	CONFIRMED(2),
	CANCELLED(3),
	COMPLETED(4);

	private final int id;

	BookingStatus(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static BookingStatus fromId(int id) {
		return Arrays.stream(values())
				.filter(e -> e.getId() == id)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("BookingStatus not found: " + id));
	}
}

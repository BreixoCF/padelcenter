package com.bookings.padelcenter.domain.model;

import java.util.Arrays;

public enum Role {
	ADMIN("ADMIN"),
	MANAGER("MANAGER"),
	USER("USER");

	private final String description;

	Role(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static Role fromDescription(String description) {
		if (description == null) {
			throw new IllegalArgumentException("Description cannot be null");
		}
		return Arrays.stream(values())
				.filter(role -> role.description.equalsIgnoreCase(description))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No enum constant for value: " + description));
	}
}

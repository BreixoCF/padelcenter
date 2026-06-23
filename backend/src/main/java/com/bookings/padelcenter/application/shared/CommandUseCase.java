package com.bookings.padelcenter.application.shared;

public interface CommandUseCase<C, R> {
		R execute(C command);
}

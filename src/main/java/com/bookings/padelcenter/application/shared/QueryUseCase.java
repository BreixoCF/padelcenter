package com.bookings.padelcenter.application.shared;

import org.jspecify.annotations.NonNull;

public interface QueryUseCase<Q, R> {

		@NonNull
		R execute(Q query);
}

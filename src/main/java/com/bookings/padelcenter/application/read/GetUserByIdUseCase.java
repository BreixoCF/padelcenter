package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetUserByIdQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetUserByIdUseCase implements QueryUseCase<GetUserByIdQuery, User> {

	private final UserRepository userRepository;

	@NonNull
	@Override
	public User execute(GetUserByIdQuery query) {
		return userRepository.findById(query.userId())
				.orElseThrow(() -> new UserNotFoundException(query.userId()));
	}
}

package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllUsersQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAllUsersUseCase implements QueryUseCase<GetAllUsersQuery, PageResult<User>> {

	private final UserRepository userRepository;

	@NonNull
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public PageResult<User> execute(GetAllUsersQuery query) {
		return userRepository.findAll(query.page(), query.size());
	}
}

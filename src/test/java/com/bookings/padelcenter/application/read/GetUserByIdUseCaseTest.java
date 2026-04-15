package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetUserByIdQuery;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private GetUserByIdUseCase getUserByIdUseCase;

	@Test
	@DisplayName("execute_existingId_returnsUser")
	void execute_existingId_returnsUser() {
		var userId = UUID.randomUUID();
		var user = new User(userId, "Ana", "García", "ana@test.com",
				"hash", "600000000", Set.of(), Auditable.newAudit());
		var query = new GetUserByIdQuery(userId);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		var result = getUserByIdUseCase.execute(query);

		assertThat(result).isEqualTo(user);
		assertThat(result.id()).isEqualTo(userId);
		verify(userRepository).findById(userId);
	}

	@Test
	@DisplayName("execute_nonExistingId_throwsUserNotFoundException")
	void execute_nonExistingId_throwsUserNotFoundException() {
		var userId = UUID.randomUUID();
		var query = new GetUserByIdQuery(userId);

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> getUserByIdUseCase.execute(query))
				.isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining(userId.toString());

		verify(userRepository).findById(userId);
	}
}

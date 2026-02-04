package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllUsersQuery;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllUsersUseCase Tests")
class GetAllUsersUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private GetAllUsersUseCase getAllUsersUseCase;

	private GetAllUsersQuery query;
	private List<User> expectedUsers;

	@BeforeEach
	void setUp() {
		query = new GetAllUsersQuery();

		expectedUsers = List.of(
			new User(
				UUID.randomUUID(),
				"John",
				"Doe",
				"john.doe@example.com",
				"hashedPassword1",
				"600123456",
				Set.of(),
				Auditable.newAudit()
			),
			new User(
				UUID.randomUUID(),
				"Jane",
				"Smith",
				"jane.smith@example.com",
				"hashedPassword2",
				"600654321",
				Set.of(),
				Auditable.newAudit()
			),
			new User(
				UUID.randomUUID(),
				"Bob",
				"Johnson",
				"bob.johnson@example.com",
				"hashedPassword3",
				"600987654",
				Set.of(),
				Auditable.newAudit()
			)
		);
	}

	@Test
	@DisplayName("Should return all users from repository")
	void shouldReturnAllUsers() {
		// Given
		when(userRepository.findAll()).thenReturn(expectedUsers);

		// When
		List<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(3);
		assertThat(result).isEqualTo(expectedUsers);

		verify(userRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("Should return empty list when no users exist")
	void shouldReturnEmptyListWhenNoUsers() {
		// Given
		when(userRepository.findAll()).thenReturn(new ArrayList<>());

		// When
		List<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();

		verify(userRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("Should return list with single user")
	void shouldReturnSingleUser() {
		// Given
		List<User> singleUser = List.of(expectedUsers.get(0));
		when(userRepository.findAll()).thenReturn(singleUser);

		// When
		List<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(1);
		assertThat(result.get(0).firstName()).isEqualTo("John");
		assertThat(result.get(0).email()).isEqualTo("john.doe@example.com");

		verify(userRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("Should call repository findAll exactly once")
	void shouldCallRepositoryOnce() {
		// Given
		when(userRepository.findAll()).thenReturn(expectedUsers);

		// When
		getAllUsersUseCase.execute(query);

		// Then
		verify(userRepository, times(1)).findAll();
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	@DisplayName("Should return non-null result")
	void shouldReturnNonNullResult() {
		// Given
		when(userRepository.findAll()).thenReturn(expectedUsers);

		// When
		List<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should preserve user details in returned list")
	void shouldPreserveUserDetails() {
		// Given
		when(userRepository.findAll()).thenReturn(expectedUsers);

		// When
		List<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result.get(0).firstName()).isEqualTo("John");
		assertThat(result.get(0).lastName()).isEqualTo("Doe");
		assertThat(result.get(0).email()).isEqualTo("john.doe@example.com");

		assertThat(result.get(1).firstName()).isEqualTo("Jane");
		assertThat(result.get(1).lastName()).isEqualTo("Smith");
		assertThat(result.get(1).email()).isEqualTo("jane.smith@example.com");

		assertThat(result.get(2).firstName()).isEqualTo("Bob");
		assertThat(result.get(2).lastName()).isEqualTo("Johnson");
		assertThat(result.get(2).email()).isEqualTo("bob.johnson@example.com");
	}
}

package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllUsersQuery;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	private List<User> users;

	@BeforeEach
	void setUp() {
		query = new GetAllUsersQuery(0, 20);

		users = List.of(
			new User(
				UUID.randomUUID(), "John", "Doe", "john.doe@example.com",
				"hashedPassword1", "600123456", Set.of(), Auditable.newAudit()
			),
			new User(
				UUID.randomUUID(), "Jane", "Smith", "jane.smith@example.com",
				"hashedPassword2", "600654321", Set.of(), Auditable.newAudit()
			),
			new User(
				UUID.randomUUID(), "Bob", "Johnson", "bob.johnson@example.com",
				"hashedPassword3", "600987654", Set.of(), Auditable.newAudit()
			)
		);
	}

	@Test
	@DisplayName("Should return paginated users from repository")
	void shouldReturnAllUsers() {
		// Given
		var pageResult = new PageResult<>(users, 0, 20, 3L, 1);
		when(userRepository.findAll(0, 20)).thenReturn(pageResult);

		// When
		PageResult<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(3);
		assertThat(result.content()).isEqualTo(users);
		assertThat(result.totalElements()).isEqualTo(3L);
		assertThat(result.totalPages()).isEqualTo(1);

		verify(userRepository, times(1)).findAll(0, 20);
	}

	@Test
	@DisplayName("Should return empty page when no users exist")
	void shouldReturnEmptyPageWhenNoUsers() {
		// Given
		var emptyPage = new PageResult<User>(List.of(), 0, 20, 0L, 0);
		when(userRepository.findAll(0, 20)).thenReturn(emptyPage);

		// When
		PageResult<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isEqualTo(0L);

		verify(userRepository, times(1)).findAll(0, 20);
	}

	@Test
	@DisplayName("Should pass page and size parameters to repository")
	void shouldPassPaginationParams() {
		// Given
		var query2 = new GetAllUsersQuery(2, 10);
		var pageResult = new PageResult<>(List.of(users.get(0)), 2, 10, 21L, 3);
		when(userRepository.findAll(2, 10)).thenReturn(pageResult);

		// When
		PageResult<User> result = getAllUsersUseCase.execute(query2);

		// Then
		assertThat(result.page()).isEqualTo(2);
		assertThat(result.size()).isEqualTo(10);
		assertThat(result.totalElements()).isEqualTo(21L);
		assertThat(result.totalPages()).isEqualTo(3);

		verify(userRepository, times(1)).findAll(2, 10);
	}

	@Test
	@DisplayName("Should call repository findAll exactly once")
	void shouldCallRepositoryOnce() {
		// Given
		var pageResult = new PageResult<>(users, 0, 20, 3L, 1);
		when(userRepository.findAll(0, 20)).thenReturn(pageResult);

		// When
		getAllUsersUseCase.execute(query);

		// Then
		verify(userRepository, times(1)).findAll(0, 20);
		verifyNoMoreInteractions(userRepository);
	}

	@Test
	@DisplayName("Should return non-null result")
	void shouldReturnNonNullResult() {
		// Given
		var pageResult = new PageResult<>(users, 0, 20, 3L, 1);
		when(userRepository.findAll(0, 20)).thenReturn(pageResult);

		// When
		PageResult<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
	}

	@Test
	@DisplayName("Should preserve user details in returned page")
	void shouldPreserveUserDetails() {
		// Given
		var pageResult = new PageResult<>(users, 0, 20, 3L, 1);
		when(userRepository.findAll(0, 20)).thenReturn(pageResult);

		// When
		PageResult<User> result = getAllUsersUseCase.execute(query);

		// Then
		assertThat(result.content().get(0).firstName()).isEqualTo("John");
		assertThat(result.content().get(0).lastName()).isEqualTo("Doe");
		assertThat(result.content().get(0).email()).isEqualTo("john.doe@example.com");

		assertThat(result.content().get(1).firstName()).isEqualTo("Jane");
		assertThat(result.content().get(1).lastName()).isEqualTo("Smith");
		assertThat(result.content().get(1).email()).isEqualTo("jane.smith@example.com");

		assertThat(result.content().get(2).firstName()).isEqualTo("Bob");
		assertThat(result.content().get(2).lastName()).isEqualTo("Johnson");
		assertThat(result.content().get(2).email()).isEqualTo("bob.johnson@example.com");
	}
}

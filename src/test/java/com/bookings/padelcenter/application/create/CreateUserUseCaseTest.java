package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateUserCommand;
import com.bookings.padelcenter.application.mapper.UserCommandMapper;
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

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserUseCase Tests")
class CreateUserUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserCommandMapper userCommandMapper;

	@InjectMocks
	private CreateUserUseCase createUserUseCase;

	private CreateUserCommand command;
	private User expectedUser;

	@BeforeEach
	void setUp() {
		command = new CreateUserCommand(
			"John",
			"Doe",
			"john.doe@example.com",
			"password123",
			"600123456"
		);

		expectedUser = new User(
			UUID.randomUUID(),
			"John",
			"Doe",
			"john.doe@example.com",
			"hashedPassword",
			"600123456",
			Set.of(),
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully create a user")
	void shouldCreateUser() {
		// Given
		when(userCommandMapper.toDomain(command)).thenReturn(expectedUser);
		when(userRepository.save(expectedUser)).thenReturn(expectedUser);

		// When
		User result = createUserUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.firstName()).isEqualTo("John");
		assertThat(result.lastName()).isEqualTo("Doe");
		assertThat(result.email()).isEqualTo("john.doe@example.com");
		assertThat(result.phoneNumber()).isEqualTo("600123456");

		verify(userCommandMapper, times(1)).toDomain(command);
		verify(userRepository, times(1)).save(expectedUser);
	}

	@Test
	@DisplayName("Should map command to domain using mapper")
	void shouldUseMapperToConvertCommand() {
		// Given
		when(userCommandMapper.toDomain(command)).thenReturn(expectedUser);
		when(userRepository.save(any(User.class))).thenReturn(expectedUser);

		// When
		createUserUseCase.execute(command);

		// Then
		verify(userCommandMapper).toDomain(command);
	}

	@Test
	@DisplayName("Should save user using repository")
	void shouldSaveUserUsingRepository() {
		// Given
		when(userCommandMapper.toDomain(command)).thenReturn(expectedUser);
		when(userRepository.save(expectedUser)).thenReturn(expectedUser);

		// When
		createUserUseCase.execute(command);

		// Then
		verify(userRepository).save(expectedUser);
	}

	@Test
	@DisplayName("Should return the saved user")
	void shouldReturnSavedUser() {
		// Given
		User savedUser = new User(
			UUID.randomUUID(),
			"John",
			"Doe",
			"john.doe@example.com",
			"hashedPassword",
			"600123456",
			Set.of(),
			Auditable.newAudit()
		);

		when(userCommandMapper.toDomain(command)).thenReturn(expectedUser);
		when(userRepository.save(expectedUser)).thenReturn(savedUser);

		// When
		User result = createUserUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(savedUser);
		assertThat(result.userId()).isNotNull();
	}
}

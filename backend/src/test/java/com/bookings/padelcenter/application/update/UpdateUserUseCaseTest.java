package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateUserCommand;
import com.bookings.padelcenter.application.mapper.UserCommandMapper;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.EmailAlreadyExistsException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserUseCase Tests")
class UpdateUserUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserCommandMapper userCommandMapper;

	@InjectMocks
	private UpdateUserUseCase updateUserUseCase;

	private UUID userId;
	private UpdateUserCommand command;
	private User existingUser;
	private User updatedUser;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();

		command = new UpdateUserCommand(
			userId,
			"John",
			"Updated",
			"john.updated@example.com",
			"newPassword123",
			"600999888",
			new AuthenticatedUser(userId)
		);

		existingUser = new User(
			userId,
			"John",
			"Doe",
			"john.doe@example.com",
			"oldHashedPassword",
			"600123456",
			Set.of(),
			Auditable.newAudit()
		);

		updatedUser = new User(
			userId,
			"John",
			"Updated",
			"john.updated@example.com",
			"newHashedPassword",
			"600999888",
			Set.of(),
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully update user")
	void shouldUpdateUser() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
		when(userCommandMapper.updateFromCommand(command, existingUser)).thenReturn(updatedUser);
		when(userRepository.save(updatedUser)).thenReturn(updatedUser);

		// When
		User result = updateUserUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.lastName()).isEqualTo("Updated");
		assertThat(result.email()).isEqualTo("john.updated@example.com");
		assertThat(result.phoneNumber()).isEqualTo("600999888");

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, times(1)).existsByEmail("john.updated@example.com");
		verify(userCommandMapper, times(1)).updateFromCommand(command, existingUser);
		verify(userRepository, times(1)).save(updatedUser);
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void shouldThrowExceptionWhenUserNotFound() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> updateUserUseCase.execute(command))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessageContaining(userId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, never()).existsByEmail(anyString());
		verify(userCommandMapper, never()).updateFromCommand(any(), any());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Should throw EmailAlreadyExistsException when email is taken by another user")
	void shouldThrowExceptionWhenEmailAlreadyExists() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmail("john.updated@example.com")).thenReturn(true);

		// When & Then
		assertThatThrownBy(() -> updateUserUseCase.execute(command))
			.isInstanceOf(EmailAlreadyExistsException.class)
			.hasMessageContaining("john.updated@example.com");

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, times(1)).existsByEmail("john.updated@example.com");
		verify(userCommandMapper, never()).updateFromCommand(any(), any());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Should not check email existence when email is unchanged")
	void shouldNotCheckEmailWhenUnchanged() {
		// Given
		UpdateUserCommand sameEmailCommand = new UpdateUserCommand(
			userId,
			"John",
			"Updated",
			"john.doe@example.com", // Same email as existing user
			"newPassword123",
			"600999888",
			new AuthenticatedUser(userId)
		);

		User updatedUserSameEmail = new User(
			userId,
			"John",
			"Updated",
			"john.doe@example.com",
			"newHashedPassword",
			"600999888",
			Set.of(),
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userCommandMapper.updateFromCommand(sameEmailCommand, existingUser)).thenReturn(updatedUserSameEmail);
		when(userRepository.save(updatedUserSameEmail)).thenReturn(updatedUserSameEmail);

		// When
		updateUserUseCase.execute(sameEmailCommand);

		// Then
		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, never()).existsByEmail(anyString());
		verify(userCommandMapper, times(1)).updateFromCommand(sameEmailCommand, existingUser);
		verify(userRepository, times(1)).save(updatedUserSameEmail);
	}

	@Test
	@DisplayName("Should use mapper to update user from command")
	void shouldUseMapperToUpdateUser() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmail(command.email())).thenReturn(false);
		when(userCommandMapper.updateFromCommand(command, existingUser)).thenReturn(updatedUser);
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		updateUserUseCase.execute(command);

		// Then
		verify(userCommandMapper).updateFromCommand(command, existingUser);
	}

	@Test
	@DisplayName("Should return the saved updated user")
	void shouldReturnSavedUser() {
		// Given
		User savedUser = new User(
			userId,
			"John",
			"Updated",
			"john.updated@example.com",
			"newHashedPassword",
			"600999888",
			Set.of(),
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmail(command.email())).thenReturn(false);
		when(userCommandMapper.updateFromCommand(command, existingUser)).thenReturn(updatedUser);
		when(userRepository.save(updatedUser)).thenReturn(savedUser);

		// When
		User result = updateUserUseCase.execute(command);

		// Then
		assertThat(result.userId()).isEqualTo(savedUser.userId());
		assertThat(result.firstName()).isEqualTo(savedUser.firstName());
		assertThat(result.lastName()).isEqualTo(savedUser.lastName());
		assertThat(result.email()).isEqualTo(savedUser.email());
		assertThat(result.phoneNumber()).isEqualTo(savedUser.phoneNumber());
		assertThat(result.passwordHash()).isEqualTo(savedUser.passwordHash());
	}

	@Test
	@DisplayName("Should allow update when email is used by the same user")
	void shouldAllowUpdateWhenEmailBelongsToSameUser() {
		// Given
		String sameEmail = "john.doe@example.com";
		UpdateUserCommand commandWithSameEmail = new UpdateUserCommand(
			userId,
			"John",
			"UpdatedName",
			sameEmail,
			"newPassword",
			"600999888",
			new AuthenticatedUser(userId)
		);

		User userWithUpdates = new User(
			userId,
			"John",
			"UpdatedName",
			sameEmail,
			"hashedPassword",
			"600999888",
			Set.of(),
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userCommandMapper.updateFromCommand(commandWithSameEmail, existingUser)).thenReturn(userWithUpdates);
		when(userRepository.save(userWithUpdates)).thenReturn(userWithUpdates);

		// When
		User result = updateUserUseCase.execute(commandWithSameEmail);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.lastName()).isEqualTo("UpdatedName");
		verify(userRepository, never()).existsByEmail(sameEmail);
	}

	@Test
	@DisplayName("Should perform email uniqueness check only when email changes")
	void shouldCheckEmailOnlyWhenChanged() {
		// Given
		String newEmail = "different@example.com";
		UpdateUserCommand commandWithNewEmail = new UpdateUserCommand(
			userId,
			"John",
			"Doe",
			newEmail,
			"password",
			"600123456",
			new AuthenticatedUser(userId)
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.existsByEmail(newEmail)).thenReturn(false);
		when(userCommandMapper.updateFromCommand(commandWithNewEmail, existingUser)).thenReturn(updatedUser);
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		updateUserUseCase.execute(commandWithNewEmail);

		// Then
		verify(userRepository).existsByEmail(newEmail);
	}
}

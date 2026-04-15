package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdatePasswordCommand;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.InvalidPasswordException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.port.out.PasswordHasher;
import com.bookings.padelcenter.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("UpdatePasswordUseCase Tests")
class UpdatePasswordUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordHasher passwordHasher;

	@InjectMocks
	private UpdatePasswordUseCase updatePasswordUseCase;

	private UUID userId;
	private UUID modifiedBy;
	private UpdatePasswordCommand command;
	private User existingUser;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		modifiedBy = UUID.randomUUID();

		command = new UpdatePasswordCommand(
			userId,
			"currentPassword123",
			"newPassword456",
			new AuthenticatedUser(modifiedBy)
		);

		existingUser = new User(
			userId,
			"John",
			"Doe",
			"john.doe@example.com",
			"hashed_currentPassword123",
			"600123456",
			Set.of(),
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully update password when current password is correct")
	void shouldUpdatePasswordWhenCurrentPasswordIsCorrect() {
		// Given
		String newPasswordHash = "hashed_newPassword456";
		User updatedUser = existingUser.updatePassword(newPasswordHash, modifiedBy);

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches("currentPassword123", existingUser.passwordHash())).thenReturn(true);
		when(passwordHasher.hash("newPassword456")).thenReturn(newPasswordHash);
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		User result = updatePasswordUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.passwordHash()).isEqualTo(newPasswordHash);

		verify(userRepository, times(1)).findById(userId);
		verify(passwordHasher, times(1)).matches("currentPassword123", existingUser.passwordHash());
		verify(passwordHasher, times(1)).hash("newPassword456");
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void shouldThrowExceptionWhenUserNotFound() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> updatePasswordUseCase.execute(command))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessageContaining(userId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(passwordHasher, never()).matches(anyString(), anyString());
		verify(passwordHasher, never()).hash(anyString());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Should throw InvalidPasswordException when current password is incorrect")
	void shouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches("currentPassword123", existingUser.passwordHash())).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> updatePasswordUseCase.execute(command))
			.isInstanceOf(InvalidPasswordException.class)
			.hasMessage("The current password provided is incorrect");

		verify(userRepository, times(1)).findById(userId);
		verify(passwordHasher, times(1)).matches("currentPassword123", existingUser.passwordHash());
		verify(passwordHasher, never()).hash(anyString());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Should hash new password before saving")
	void shouldHashNewPasswordBeforeSaving() {
		// Given
		String newPasswordHash = "hashed_newPassword456";

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches("currentPassword123", existingUser.passwordHash())).thenReturn(true);
		when(passwordHasher.hash("newPassword456")).thenReturn(newPasswordHash);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updatePasswordUseCase.execute(command);

		// Then
		verify(passwordHasher).hash("newPassword456");
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.passwordHash()).isEqualTo(newPasswordHash);
	}

	@Test
	@DisplayName("Should update audit modifiedBy field")
	void shouldUpdateModifiedByField() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);
		when(passwordHasher.hash(anyString())).thenReturn("hashed_newPassword456");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updatePasswordUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.audit().modifiedBy()).isEqualTo(modifiedBy);
		assertThat(capturedUser.audit().modifiedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should preserve user details during password update")
	void shouldPreserveUserDetails() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);
		when(passwordHasher.hash(anyString())).thenReturn("hashed_newPassword456");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updatePasswordUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.id()).isEqualTo(existingUser.id());
		assertThat(capturedUser.firstName()).isEqualTo(existingUser.firstName());
		assertThat(capturedUser.lastName()).isEqualTo(existingUser.lastName());
		assertThat(capturedUser.email()).isEqualTo(existingUser.email());
		assertThat(capturedUser.phoneNumber()).isEqualTo(existingUser.phoneNumber());
		assertThat(capturedUser.centerRoles()).isEqualTo(existingUser.centerRoles());
	}

	@Test
	@DisplayName("Should preserve audit creation fields during password update")
	void shouldPreserveAuditCreationFields() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);
		when(passwordHasher.hash(anyString())).thenReturn("hashed_newPassword456");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updatePasswordUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.audit().createdBy()).isEqualTo(existingUser.audit().createdBy());
		assertThat(capturedUser.audit().createdAt()).isEqualTo(existingUser.audit().createdAt());
	}

	@Test
	@DisplayName("Should verify password before hashing new password")
	void shouldVerifyPasswordBeforeHashing() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches("currentPassword123", existingUser.passwordHash())).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> updatePasswordUseCase.execute(command))
			.isInstanceOf(InvalidPasswordException.class);

		verify(passwordHasher).matches("currentPassword123", existingUser.passwordHash());
		verify(passwordHasher, never()).hash(anyString());
	}

	@Test
	@DisplayName("Should return the saved user with updated password")
	void shouldReturnSavedUserWithUpdatedPassword() {
		// Given
		String newPasswordHash = "hashed_newPassword456";
		User updatedUser = existingUser.updatePassword(newPasswordHash, modifiedBy);

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);
		when(passwordHasher.hash("newPassword456")).thenReturn(newPasswordHash);
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		User result = updatePasswordUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(updatedUser);
		assertThat(result.passwordHash()).isEqualTo(newPasswordHash);
	}

	@Test
	@DisplayName("Should handle different new passwords")
	void shouldHandleDifferentNewPasswords() {
		// Given
		UpdatePasswordCommand differentPasswordCommand = new UpdatePasswordCommand(
			userId,
			"currentPassword123",
			"differentNewPassword789",
			new AuthenticatedUser(modifiedBy)
		);

		String differentPasswordHash = "hashed_differentNewPassword789";

		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches("currentPassword123", existingUser.passwordHash())).thenReturn(true);
		when(passwordHasher.hash("differentNewPassword789")).thenReturn(differentPasswordHash);
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updatePasswordUseCase.execute(differentPasswordCommand);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.passwordHash()).isEqualTo(differentPasswordHash);
	}

	@Test
	@DisplayName("Should call password hasher methods in correct order")
	void shouldCallPasswordHasherMethodsInCorrectOrder() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches(anyString(), anyString())).thenReturn(true);
		when(passwordHasher.hash(anyString())).thenReturn("hashed_newPassword456");
		when(userRepository.save(any(User.class))).thenReturn(existingUser);

		// When
		updatePasswordUseCase.execute(command);

		// Then
		var inOrder = inOrder(passwordHasher);
		inOrder.verify(passwordHasher).matches("currentPassword123", existingUser.passwordHash());
		inOrder.verify(passwordHasher).hash("newPassword456");
	}

	@Test
	@DisplayName("Should not save user when password verification fails")
	void shouldNotSaveWhenVerificationFails() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

		// When & Then
		assertThatThrownBy(() -> updatePasswordUseCase.execute(command))
			.isInstanceOf(InvalidPasswordException.class);

		verify(userRepository, never()).save(any());
	}
}

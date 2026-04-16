package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateUserRolesCommand;
import com.bookings.padelcenter.application.mapper.UserCommandMapper;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.CenterRole;
import com.bookings.padelcenter.domain.model.Role;
import com.bookings.padelcenter.domain.model.User;
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
@DisplayName("UpdateUserRolesUseCase Tests")
class UpdateUserRolesUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserCommandMapper userCommandMapper;

	@InjectMocks
	private UpdateUserRolesUseCase updateUserRolesUseCase;

	private UUID userId;
	private UUID modifiedBy;
	private UUID centerId;
	private UpdateUserRolesCommand command;
	private User existingUser;
	private Set<CenterRole> newRoles;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		modifiedBy = UUID.randomUUID();
		centerId = UUID.randomUUID();

		newRoles = Set.of(
			new CenterRole(centerId, Role.ADMIN, Auditable.newAudit()),
			new CenterRole(UUID.randomUUID(), Role.MANAGER, Auditable.newAudit())
		);

		command = new UpdateUserRolesCommand(userId, newRoles, new AuthenticatedUser(modifiedBy));

		existingUser = new User(
			userId,
			"John",
			"Doe",
			"john.doe@example.com",
			"hashedPassword",
			"600123456",
			Set.of(new CenterRole(centerId, Role.USER, Auditable.newAudit())),
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully update user roles")
	void shouldUpdateUserRoles() {
		// Given
		User updatedUser = existingUser.updateRoles(newRoles, modifiedBy);
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		User result = updateUserRolesUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.centerRoles()).hasSize(2);
		assertThat(result.centerRoles()).isEqualTo(newRoles);

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void shouldThrowExceptionWhenUserNotFound() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> updateUserRolesUseCase.execute(command))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessageContaining(userId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Should call user.updateRoles() with correct parameters")
	void shouldCallUpdateRolesWithCorrectParameters() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updateUserRolesUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.centerRoles()).isEqualTo(newRoles);
		assertThat(savedUser.audit().modifiedBy()).isEqualTo(modifiedBy);
	}

	@Test
	@DisplayName("Should preserve user data when updating roles")
	void shouldPreserveUserData() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updateUserRolesUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.userId()).isEqualTo(existingUser.userId());
		assertThat(savedUser.firstName()).isEqualTo(existingUser.firstName());
		assertThat(savedUser.lastName()).isEqualTo(existingUser.lastName());
		assertThat(savedUser.email()).isEqualTo(existingUser.email());
		assertThat(savedUser.phoneNumber()).isEqualTo(existingUser.phoneNumber());
		assertThat(savedUser.passwordHash()).isEqualTo(existingUser.passwordHash());
	}

	@Test
	@DisplayName("Should update audit modifiedBy field")
	void shouldUpdateModifiedByField() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updateUserRolesUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.audit().modifiedBy()).isEqualTo(modifiedBy);
		assertThat(savedUser.audit().modifiedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should handle empty roles set")
	void shouldHandleEmptyRolesSet() {
		// Given
		Set<CenterRole> emptyRoles = Set.of();
		UpdateUserRolesCommand commandWithEmptyRoles = new UpdateUserRolesCommand(
			userId, emptyRoles, new AuthenticatedUser(modifiedBy)
		);

		User updatedUser = existingUser.updateRoles(emptyRoles, modifiedBy);
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		User result = updateUserRolesUseCase.execute(commandWithEmptyRoles);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.centerRoles()).isEmpty();

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("Should replace existing roles with new roles")
	void shouldReplaceExistingRoles() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updateUserRolesUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.centerRoles()).isEqualTo(newRoles);
		assertThat(savedUser.centerRoles()).doesNotContain(existingUser.centerRoles().iterator().next());
	}

	@Test
	@DisplayName("Should return the saved user with updated roles")
	void shouldReturnSavedUser() {
		// Given
		User updatedUser = existingUser.updateRoles(newRoles, modifiedBy);
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		User result = updateUserRolesUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(updatedUser);
		assertThat(result.centerRoles()).hasSize(2);
	}

	@Test
	@DisplayName("Should handle multiple roles for different centers")
	void shouldHandleMultipleRolesForDifferentCenters() {
		// Given
		UUID center1 = UUID.randomUUID();
		UUID center2 = UUID.randomUUID();
		UUID center3 = UUID.randomUUID();

		Set<CenterRole> multipleRoles = Set.of(
			new CenterRole(center1, Role.ADMIN, Auditable.newAudit()),
			new CenterRole(center2, Role.MANAGER, Auditable.newAudit()),
			new CenterRole(center3, Role.USER, Auditable.newAudit())
		);

		UpdateUserRolesCommand multiRoleCommand = new UpdateUserRolesCommand(
			userId, multipleRoles, new AuthenticatedUser(modifiedBy)
		);

		User updatedUser = existingUser.updateRoles(multipleRoles, modifiedBy);
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(updatedUser);

		// When
		User result = updateUserRolesUseCase.execute(multiRoleCommand);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.centerRoles()).hasSize(3);
		assertThat(result.centerRoles()).containsAll(multipleRoles);
	}

	@Test
	@DisplayName("Should preserve audit creation fields when updating roles")
	void shouldPreserveAuditCreationFields() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		updateUserRolesUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertThat(savedUser.audit().createdBy()).isEqualTo(existingUser.audit().createdBy());
		assertThat(savedUser.audit().createdAt()).isEqualTo(existingUser.audit().createdAt());
	}
}

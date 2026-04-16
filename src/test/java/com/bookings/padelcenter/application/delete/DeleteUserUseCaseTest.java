package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteUserCommand;
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

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUserUseCase Tests")
class DeleteUserUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private DeleteUserUseCase deleteUserUseCase;

	private UUID userId;
	private UUID deletedBy;
	private DeleteUserCommand command;
	private User existingUser;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		deletedBy = UUID.randomUUID();

		command = new DeleteUserCommand(userId, new AuthenticatedUser(deletedBy));

		existingUser = new User(
			userId,
			"John",
			"Doe",
			"john.doe@example.com",
			"hashedPassword",
			"600123456",
			Set.of(),
			new Auditable(
				UUID.randomUUID(),
				Instant.now().minusSeconds(3600),
				null,
				Instant.now().minusSeconds(3600),
				null,
				null
			)
		);
	}

	@Test
	@DisplayName("Should successfully delete a user (soft delete)")
	void shouldDeleteUser() {
		// Given
		User deletedUser = existingUser.delete(deletedBy);
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(deletedUser);

		// When
		User result = deleteUserUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.userId()).isEqualTo(userId);
		assertThat(result.audit().deletedBy()).isEqualTo(deletedBy);
		assertThat(result.audit().deletedAt()).isNotNull();

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void shouldThrowExceptionWhenUserNotFound() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> deleteUserUseCase.execute(command))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessageContaining(userId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("Should set deletedBy field correctly")
	void shouldSetDeletedByCorrectly() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.audit().deletedBy()).isEqualTo(deletedBy);
	}

	@Test
	@DisplayName("Should set deletedAt timestamp")
	void shouldSetDeletedAtTimestamp() {
		// Given
		Instant beforeDelete = Instant.now();
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);
		Instant afterDelete = Instant.now();

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.audit().deletedAt()).isNotNull();
		assertThat(capturedUser.audit().deletedAt()).isBetween(beforeDelete, afterDelete);
	}

	@Test
	@DisplayName("Should preserve user data during soft delete")
	void shouldPreserveUserData() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.userId()).isEqualTo(existingUser.userId());
		assertThat(capturedUser.firstName()).isEqualTo(existingUser.firstName());
		assertThat(capturedUser.lastName()).isEqualTo(existingUser.lastName());
		assertThat(capturedUser.email()).isEqualTo(existingUser.email());
		assertThat(capturedUser.phoneNumber()).isEqualTo(existingUser.phoneNumber());
		assertThat(capturedUser.centerRoles()).hasSize(existingUser.centerRoles().size());
	}

	@Test
	@DisplayName("Should preserve audit creation fields during delete")
	void shouldPreserveAuditCreationFields() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.audit().createdBy()).isEqualTo(existingUser.audit().createdBy());
		assertThat(capturedUser.audit().createdAt()).isEqualTo(existingUser.audit().createdAt());
	}

	@Test
	@DisplayName("Should preserve audit modification fields during delete")
	void shouldPreserveAuditModificationFields() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.audit().modifiedBy()).isEqualTo(existingUser.audit().modifiedBy());
		assertThat(capturedUser.audit().modifiedAt()).isEqualTo(existingUser.audit().modifiedAt());
	}

	@Test
	@DisplayName("Should return the deleted user")
	void shouldReturnDeletedUser() {
		// Given
		User deletedUser = existingUser.delete(deletedBy);
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenReturn(deletedUser);

		// When
		User result = deleteUserUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(deletedUser);
		assertThat(result.audit().deletedBy()).isNotNull();
		assertThat(result.audit().deletedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should call user.delete() method")
	void shouldCallUserDeleteMethod() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		User result = deleteUserUseCase.execute(command);

		// Then
		assertThat(result.audit().deletedBy()).isEqualTo(deletedBy);
		assertThat(result.audit().deletedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should handle user with existing center roles")
	void shouldHandleUserWithCenterRoles() {
		// Given
		UUID centerId1 = UUID.randomUUID();
		UUID centerId2 = UUID.randomUUID();

		Set<CenterRole> roles = Set.of(
			new CenterRole(centerId1, Role.ADMIN, Auditable.newAudit()),
			new CenterRole(centerId2, Role.MANAGER, Auditable.newAudit())
		);

		User userWithRoles = new User(
			userId, "John", "Doe", "john.doe@example.com",
			"hashedPassword", "600123456", roles, existingUser.audit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userWithRoles));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.centerRoles()).hasSize(2);
		assertThat(capturedUser.audit().deletedBy()).isEqualTo(deletedBy);
	}

	@Test
	@DisplayName("Should soft delete all center roles when deleting user")
	void shouldSoftDeleteAllCenterRoles() {
		// Given
		UUID centerId1 = UUID.randomUUID();
		UUID centerId2 = UUID.randomUUID();

		Set<CenterRole> roles = Set.of(
			new CenterRole(centerId1, Role.ADMIN, Auditable.newAudit()),
			new CenterRole(centerId2, Role.MANAGER, Auditable.newAudit())
		);

		User userWithRoles = new User(
			userId, "John", "Doe", "john.doe@example.com",
			"hashedPassword", "600123456", roles, existingUser.audit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userWithRoles));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.centerRoles()).hasSize(2);
		capturedUser.centerRoles().forEach(centerRole -> {
			assertThat(centerRole.audit().deletedBy()).isEqualTo(deletedBy);
			assertThat(centerRole.audit().deletedAt()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should preserve center role data except audit when deleting")
	void shouldPreserveCenterRoleDataExceptAudit() {
		// Given
		UUID centerId1 = UUID.randomUUID();

		CenterRole originalRole = new CenterRole(centerId1, Role.ADMIN, Auditable.newAudit());
		Set<CenterRole> roles = Set.of(originalRole);

		User userWithRoles = new User(
			userId, "John", "Doe", "john.doe@example.com",
			"hashedPassword", "600123456", roles, existingUser.audit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userWithRoles));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		CenterRole deletedRole = capturedUser.centerRoles().iterator().next();
		assertThat(deletedRole.centerId()).isEqualTo(centerId1);
		assertThat(deletedRole.role()).isEqualTo(Role.ADMIN);
		assertThat(deletedRole.audit().deletedBy()).isEqualTo(deletedBy);
	}

	@Test
	@DisplayName("Should handle user with no center roles")
	void shouldHandleUserWithNoCenterRoles() {
		// Given
		User userWithoutRoles = new User(
			userId, "John", "Doe", "john.doe@example.com",
			"hashedPassword", "600123456", Set.of(), existingUser.audit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userWithoutRoles));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		// When
		deleteUserUseCase.execute(command);

		// Then
		verify(userRepository).save(userCaptor.capture());
		User capturedUser = userCaptor.getValue();

		assertThat(capturedUser.centerRoles()).isEmpty();
		assertThat(capturedUser.audit().deletedBy()).isEqualTo(deletedBy);
	}
}

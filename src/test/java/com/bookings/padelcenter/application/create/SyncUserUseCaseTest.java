package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.SyncUserCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncUserUseCase Tests")
class SyncUserUseCaseTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private SyncUserUseCase useCase;

	private SyncUserCommand command;

	@BeforeEach
	void setUp() {
		command = new SyncUserCommand("kc-123", "test@padel.com", "Ana", "García");
	}

	@Test
	@DisplayName("should return existing user when keycloakId already synced")
	void execute_existingKeycloakId_returnsWithoutSaving() {
		var existing = userWithKeycloak("kc-123");
		given(userRepository.findByKeycloakId("kc-123")).willReturn(Optional.of(existing));

		var result = useCase.execute(command);

		assertThat(result.keycloakId()).isEqualTo("kc-123");
		then(userRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("should link keycloak id when user exists with same email but no keycloakId")
	void execute_existingEmailNoKeycloakId_linksKeycloakId() {
		var existingUser = userWithoutKeycloak();
		given(userRepository.findByKeycloakId(any())).willReturn(Optional.empty());
		given(userRepository.findByEmail(command.email())).willReturn(Optional.of(existingUser));
		given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		var result = useCase.execute(command);

		then(userRepository).should().save(any());
		assertThat(result.email()).isEqualTo(command.email());
	}

	@Test
	@DisplayName("should create new user when no match by keycloakId or email")
	void execute_newUser_createsAndSaves() {
		given(userRepository.findByKeycloakId(any())).willReturn(Optional.empty());
		given(userRepository.findByEmail(any())).willReturn(Optional.empty());
		given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		var result = useCase.execute(command);

		then(userRepository).should().save(any());
		assertThat(result.keycloakId()).isEqualTo("kc-123");
		assertThat(result.email()).isEqualTo("test@padel.com");
	}

	private User userWithKeycloak(String keycloakId) {
		return new User(UUID.randomUUID(), keycloakId, "Ana", "García",
				"test@padel.com", "", null, Set.of(), Auditable.newAudit());
	}

	private User userWithoutKeycloak() {
		return new User(UUID.randomUUID(), null, "Ana", "García",
				"test@padel.com", "hash", "600000000", Set.of(), Auditable.newAudit());
	}
}

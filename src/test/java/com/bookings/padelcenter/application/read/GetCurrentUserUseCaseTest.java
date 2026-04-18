package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetCurrentUserQuery;
import com.bookings.padelcenter.domain.exception.ResourceNotFoundException;
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
@DisplayName("GetCurrentUserUseCase Tests")
class GetCurrentUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @Test
    @DisplayName("execute_syncedUser_returnsUser")
    void execute_syncedUser_returnsUser() {
        var keycloakId = "kc-sub-abc123";
        var userId = UUID.randomUUID();
        var user = new User(userId, keycloakId, "John", "Doe",
                "john@example.com", "", "600123456", Set.of(), Auditable.newAudit());
        var query = new GetCurrentUserQuery(keycloakId);

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(user));

        var result = getCurrentUserUseCase.execute(query);

        assertThat(result).isEqualTo(user);
        assertThat(result.keycloakId()).isEqualTo(keycloakId);
        verify(userRepository).findByKeycloakId(keycloakId);
    }

    @Test
    @DisplayName("execute_unsyncedUser_throwsResourceNotFoundException")
    void execute_unsyncedUser_throwsResourceNotFoundException() {
        var keycloakId = "kc-sub-unknown";
        var query = new GetCurrentUserQuery(keycloakId);

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCurrentUserUseCase.execute(query))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not synced");

        verify(userRepository).findByKeycloakId(keycloakId);
    }
}

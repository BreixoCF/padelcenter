package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetCurrentUserQuery;
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
@DisplayName("GetCurrentUserUseCase Tests")
class GetCurrentUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetCurrentUserUseCase getCurrentUserUseCase;

    @Test
    @DisplayName("execute_existingUser_returnsUser")
    void execute_existingUser_returnsUser() {
        var userId = UUID.randomUUID();
        var user = new User(userId, "John", "Doe",
                "john@example.com", "", "600123456", Set.of(), Auditable.newAudit());
        var query = new GetCurrentUserQuery(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = getCurrentUserUseCase.execute(query);

        assertThat(result).isEqualTo(user);
        assertThat(result.userId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("execute_unknownUser_throwsUserNotFoundException")
    void execute_unknownUser_throwsUserNotFoundException() {
        var userId = UUID.randomUUID();
        var query = new GetCurrentUserQuery(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getCurrentUserUseCase.execute(query))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
    }
}

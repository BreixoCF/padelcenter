package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetCurrentUserQuery;
import com.bookings.padelcenter.domain.exception.ResourceNotFoundException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(GetCurrentUserQuery query) {
        User user = userRepository.findByKeycloakId(query.keycloakId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not synced — call POST /auth/sync first"));
        log.info("user.me.resolved keycloakId={} userId={}", query.keycloakId(), user.userId());
        return user;
    }
}

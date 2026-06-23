package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.LoginQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.UnauthorizedException;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginUseCase implements QueryUseCase<LoginQuery, User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User execute(LoginQuery query) {
        var user = userRepository.findByEmail(query.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(query.password(), user.passwordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return user;
    }
}

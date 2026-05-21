package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.LoginCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
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
public class LoginUseCase implements CommandUseCase<LoginCommand, User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User execute(LoginCommand command) {
        var user = userRepository.findByEmail(command.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(command.password(), user.passwordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return user;
    }
}

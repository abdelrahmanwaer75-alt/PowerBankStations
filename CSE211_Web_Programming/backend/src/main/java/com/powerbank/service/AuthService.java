package com.powerbank.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.powerbank.entity.User;
import com.powerbank.repository.UserRepository;
import com.powerbank.security.PasswordHasher;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository users) {
        this.users = users;
        this.passwordHasher = new PasswordHasher();
    }

    @Transactional
    public User register(User user) {
        if (user.name == null || user.name.isBlank()
                || user.email == null || user.email.isBlank()
                || user.password == null || user.password.length() < 8) {
            throw new IllegalArgumentException("Name, email, and a password of at least 8 characters are required");
        }
        if (users.findByEmailIgnoreCase(user.email.trim()).isPresent()) {
            throw new IllegalStateException("An account with this email already exists");
        }

        user.name = user.name.trim();
        user.email = user.email.trim().toLowerCase(java.util.Locale.ROOT);
        user.password = passwordHasher.hash(user.password);
        user.role = "CUSTOMER";
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return users.findByEmailIgnoreCase(email.trim())
                .filter(user -> passwordHasher.matches(password, user.password));
    }
}

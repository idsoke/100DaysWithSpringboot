package com.belajar.belajarspring.config;

import com.belajar.belajarspring.entity.Role;
import com.belajar.belajarspring.entity.User;
import com.belajar.belajarspring.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createUserIfNotExists("admin", "admin123", Role.ROLE_ADMIN);
        createUserIfNotExists("user", "user123", Role.ROLE_USER);
    }

    private void createUserIfNotExists(String username, String rawPassword, Role role) {
        userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            return userRepository.save(user);
        });
    }
}

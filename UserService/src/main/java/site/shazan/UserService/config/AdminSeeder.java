package site.shazan.UserService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import site.shazan.UserService.models.User;
import site.shazan.UserService.repo.UserRepository;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AdminSeedProperties adminSeedProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!adminSeedProperties.isSeed()) {
            return;
        }

        if (isBlank(adminSeedProperties.getEmail()) || isBlank(adminSeedProperties.getPassword())) {
            throw new IllegalStateException("Admin seeding is enabled but admin.email or admin.password is missing");
        }

        userRepository.findByEmail(adminSeedProperties.getEmail()).ifPresentOrElse(existing -> {
            if (!"ADMIN".equalsIgnoreCase(existing.getRole())) {
                throw new IllegalStateException("Admin seed email already exists with a non-admin role");
            }
        }, () -> {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminSeedProperties.getEmail());
            admin.setPassword(passwordEncoder.encode(adminSeedProperties.getPassword()));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}


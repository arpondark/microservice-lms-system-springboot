package site.shazan.AuthService.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import site.shazan.AuthService.Dtos.LoginRequest;
import site.shazan.AuthService.Dtos.RegisterRequest;
import site.shazan.AuthService.Dtos.Role;
import site.shazan.AuthService.Dtos.UserResponse;
import site.shazan.AuthService.repo.UserClient;
import site.shazan.AuthService.utils.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KafkaProducerService kafka;

    public String signup(RegisterRequest request) {

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole("STUDENT");

        userClient.createUser(request);

        kafka.sendUserCreatedEvent(request.getEmail());

        return "User Registered";
    }

    public String login(LoginRequest request) {

        UserResponse user = userClient.getByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user);
    }

    public UserResponse adminCreateUser(RegisterRequest request) {
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole(normalizeRole(request.getRole()));
        return userClient.adminCreateUser(request);
    }

    public String adminDeleteUser(String email) {
        userClient.adminDeleteUser(email);
        return "User deleted";
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new RuntimeException("Role is required");
        }

        try {
            return Role.valueOf(role.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid role. Allowed roles: ADMIN, TECHER, STUDENT");
        }
    }
}
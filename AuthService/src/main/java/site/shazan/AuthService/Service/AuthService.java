package site.shazan.AuthService.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

        return jwtUtil.generateToken(user.getEmail());
    }
}
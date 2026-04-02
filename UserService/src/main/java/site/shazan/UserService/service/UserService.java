package site.shazan.UserService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.shazan.UserService.dtos.RegisterRequest;
import site.shazan.UserService.models.User;
import site.shazan.UserService.repo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    public User create(RegisterRequest req) {

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setRole(req.getRole());
        user.setProvider("LOCAL");

        return repo.save(user);
    }

    public User getByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

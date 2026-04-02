package site.shazan.UserService.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import site.shazan.UserService.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

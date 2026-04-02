package site.shazan.USER_SERRVICE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.shazan.USER_SERRVICE.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);
}

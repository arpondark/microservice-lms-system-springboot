package site.shazan.AuthService.repo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.shazan.AuthService.Dtos.RegisterRequest;
import site.shazan.AuthService.Dtos.UserResponse;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @PostMapping("/users")
    UserResponse createUser(@RequestBody RegisterRequest request);

    @PostMapping("/users/admin/create")
    UserResponse adminCreateUser(@RequestBody RegisterRequest request);

    @DeleteMapping("/users/admin/delete/{email}")
    void adminDeleteUser(@PathVariable("email") String email);

    @GetMapping("/users/email/{email}")
    UserResponse getByEmail(@PathVariable("email") String email);
}
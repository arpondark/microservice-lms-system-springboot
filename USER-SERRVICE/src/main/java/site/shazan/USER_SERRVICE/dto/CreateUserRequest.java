package site.shazan.USER_SERRVICE.dto;
import lombok.Data;
import site.shazan.USER_SERRVICE.models.Role;

@Data
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
}

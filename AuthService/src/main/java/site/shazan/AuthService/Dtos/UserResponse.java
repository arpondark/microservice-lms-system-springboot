package site.shazan.AuthService.Dtos;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
}
package site.shazan.AuthService.Dtos;

import lombok.Data;

@Data
public class UserResponse {
    private String email;
    private String password;
}
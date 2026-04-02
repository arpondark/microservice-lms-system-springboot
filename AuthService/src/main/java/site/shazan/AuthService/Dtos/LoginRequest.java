package site.shazan.AuthService.Dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
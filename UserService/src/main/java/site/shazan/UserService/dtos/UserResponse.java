package site.shazan.UserService.dtos;

import lombok.Data;

@Data
public class UserResponse {
    private String email;
    private String password;
}
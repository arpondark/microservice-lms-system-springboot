package site.shazan.UserService.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "admin")
public class AdminSeedProperties {

    private boolean seed;
    private String email;
    private String password;
}


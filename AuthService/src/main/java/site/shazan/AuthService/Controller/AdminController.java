package site.shazan.AuthService.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.shazan.AuthService.Dtos.RegisterRequest;
import site.shazan.AuthService.Service.AuthService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService service;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.adminCreateUser(request));
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<?> delete(@PathVariable String email) {
        return ResponseEntity.ok(service.adminDeleteUser(email));
    }
}


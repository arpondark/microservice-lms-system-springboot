package site.shazan.UserService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.shazan.UserService.dtos.RegisterRequest;
import site.shazan.UserService.models.User;
import site.shazan.UserService.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public User create(@RequestBody RegisterRequest req) {
        return service.create(req);
    }

    @GetMapping("/email/{email}")
    public User get(@PathVariable String email) {
        return service.getByEmail(email);
    }

    @PostMapping("/admin/create")
     public User adminController(@RequestBody RegisterRequest req) {
         return service.adminCreation(req);
     }
}
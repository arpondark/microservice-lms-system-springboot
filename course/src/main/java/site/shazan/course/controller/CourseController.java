package site.shazan.course.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.shazan.course.models.Course;
import site.shazan.course.models.Enrollment;
import site.shazan.course.service.CourseService;

import java.util.List;


@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService service;

    @PostMapping
    public Course create(
            Authentication auth,
            @ModelAttribute Course course,
            @RequestParam MultipartFile image,
            @RequestParam MultipartFile video,
            @RequestParam MultipartFile material
    ) {

        Long userId = (Long) auth.getPrincipal();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("");

        if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
            throw new RuntimeException("Only teacher/admin allowed");
        }

        return service.create(course, image, video, material, userId);
    }

    @GetMapping
    public List<Course> all() {
        return service.getAll();
    }

    @PostMapping("/{courseId}/enroll")
    public Enrollment enroll(Authentication auth, @PathVariable Long courseId) {

        Long studentId = (Long) auth.getPrincipal();

        return service.enroll(studentId, courseId);
    }
}
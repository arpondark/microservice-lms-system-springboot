package site.shazan.course.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import site.shazan.course.models.Course;

public interface CourseRepository extends JpaRepository<Course, Integer> {
}

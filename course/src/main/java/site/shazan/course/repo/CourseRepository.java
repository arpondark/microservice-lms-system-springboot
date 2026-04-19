package site.shazan.course.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import site.shazan.course.models.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByTeacherId(Long teacherId);
}

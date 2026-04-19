package site.shazan.course.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import site.shazan.course.models.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {
}

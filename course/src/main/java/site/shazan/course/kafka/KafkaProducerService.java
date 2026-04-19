package site.shazan.course.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import site.shazan.course.models.Enrollment;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publishes an enrollment event when a student enrolls in a course
     */
    public void publishEnrollmentEvent(Enrollment enrollment) {
        try {
            String enrollmentJson = objectMapper.writeValueAsString(enrollment);
            kafkaTemplate.send("course-enrollment", enrollment.getId().toString(), enrollmentJson);
            log.info("Enrollment event published for enrollment ID: {}", enrollment.getId());
        } catch (Exception e) {
            log.error("Failed to publish enrollment event", e);
        }
    }

    /**
     * Publishes a course creation event when a new course is created
     */
    public void publishCourseCreatedEvent(String courseId, String courseName, String teacherId) {
        try {
            String eventJson = String.format(
                "{\"courseId\":\"%s\",\"courseName\":\"%s\",\"teacherId\":\"%s\",\"timestamp\":%d}",
                courseId, courseName, teacherId, System.currentTimeMillis()
            );
            kafkaTemplate.send("course-created", courseId, eventJson);
            log.info("Course created event published for course ID: {}", courseId);
        } catch (Exception e) {
            log.error("Failed to publish course created event", e);
        }
    }
}


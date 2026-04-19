package site.shazan.course.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "user-created", groupId = "course-group")
    public void consumeUserCreatedEvent(String message) {
        log.info("Received user-created event from Auth Service: {}", message);
        // Process user creation events if needed (e.g., initialize user preferences, etc.)
    }

    @KafkaListener(topics = "course-enrollment", groupId = "enrollment-group")
    public void consumeEnrollmentEvent(String message) {
        log.info("Received enrollment event: {}", message);
        // Additional processing can be done here (e.g., notifications, analytics, etc.)
    }
}


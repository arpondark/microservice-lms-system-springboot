package site.shazan.UserService.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics = "user-created", groupId = "user-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}

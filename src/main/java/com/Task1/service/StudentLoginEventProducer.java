package com.Task1.service;

import com.Task1.model.StudentLoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLoginEventProducer {

    private final KafkaTemplate<String, StudentLoginEvent> kafkaTemplate;

    @Value("${app.kafka.student-login-topic}")
    private String topicName;

    public void publish(StudentLoginEvent event) {
        log.info("Publishing Kafka event for email={}, topic={}", event.getEmail(), topicName);
        kafkaTemplate.send(topicName, event.getEmail(), event);
        log.info("Kafka event published successfully for email={}", event.getEmail());
    }
}
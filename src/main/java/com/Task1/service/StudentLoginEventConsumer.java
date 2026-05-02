package com.Task1.service;

import com.Task1.model.Student;
import com.Task1.model.StudentLoginEvent;
import com.Task1.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLoginEventConsumer {

    private final AttendanceService attendenceService;
    private final EmailService emailService;
    private final StudentRepository studentRepository;

    @KafkaListener(
            topics = "${app.kafka.student-login-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(StudentLoginEvent event) {

        log.info("Kafka event consumed for email={}, role={}", event.getEmail(), event.getRole());

        try {
            attendenceService.markAttendanceFromKafka(event.getEmail());
            log.info("Attendance processed successfully from Kafka for email={}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to process attendance from Kafka for email={}", event.getEmail(), e);
            return;
        }

        try {
            emailService.sendStudentLoginAlert(event.getEmail(), event.getLoginTime());
            log.info("Login alert email sent successfully to email={}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send login alert email to email={}", event.getEmail(), e);
        }

        try {
            Student student = studentRepository.findByEmail(event.getEmail()).orElse(null);

            String studentName = "Student";

            if (student != null && student.getUsername() != null && !student.getUsername().isBlank()) {
                studentName = student.getUsername();
            }

            emailService.sendAttendancePresentNotification(
                    event.getEmail(),
                    studentName,
                    event.getLoginTime()
            );

            log.info("Present attendance notification email processed for email={}", event.getEmail());

        } catch (Exception e) {
            log.error("Failed to send present attendance notification for email={}", event.getEmail(), e);
        }
    }
}
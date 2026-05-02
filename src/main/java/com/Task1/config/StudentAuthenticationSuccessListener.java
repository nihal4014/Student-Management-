package com.Task1.config;

import com.Task1.model.StudentLoginEvent;
import com.Task1.service.StudentLoginEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudentAuthenticationSuccessListener {

    private final StudentLoginEventProducer studentLoginEventProducer;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();

        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority()));

        if (!isStudent) {
            return;
        }

        String email = authentication.getName();

        log.info("Authentication success for student email={}", email);

        StudentLoginEvent loginEvent = StudentLoginEvent.builder()
                .email(email)
                .role("ROLE_STUDENT")
                .loginTime(LocalDateTime.now())
                .message("Student login successfully")
                .build();

        studentLoginEventProducer.publish(loginEvent);
        log.info("Student login event sent to Kafka for email={}", email);
    }
}
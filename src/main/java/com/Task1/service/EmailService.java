package com.Task1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendStudentLoginAlert(String toEmail, LocalDateTime loginTime) {

        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.error("Email sending failed: toEmail is null or empty");
            return;
        }

        String cleanToEmail = toEmail.trim();
        String cleanFromEmail = fromEmail.trim();

        LocalDateTime safeLoginTime = loginTime == null ? LocalDateTime.now() : loginTime;

        String formattedLoginTime = safeLoginTime.format(
                DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a")
        );

        log.info("Preparing login alert email fromEmail={} toEmail={}", cleanFromEmail, cleanToEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(cleanFromEmail);
            message.setTo(cleanToEmail);
            message.setSubject("Student Login Alert");

            message.setText(
                    "Hello,\n\n" +
                            "Your student account has been logged in successfully.\n\n" +
                            "Login Time: " + formattedLoginTime + "\n\n" +
                            "If this login was not performed by you, please reset your password immediately.\n\n" +
                            "Regards,\n" +
                            "Student Management System"
            );

            javaMailSender.send(message);

            log.info("Login alert email sent successfully fromEmail={} toEmail={}", cleanFromEmail, cleanToEmail);

        } catch (MailException e) {
            log.error("Failed to send login alert email toEmail={}. Reason={}", cleanToEmail, e.getMessage());
        }
    }

    public void sendPasswordResetOtp(String toEmail, String otp) {

        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.error("Password reset OTP email failed: toEmail is null or empty");
            return;
        }

        String cleanToEmail = toEmail.trim();
        String cleanFromEmail = fromEmail.trim();

        log.info("Preparing password reset OTP email toEmail={}", cleanToEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(cleanFromEmail);
            message.setTo(cleanToEmail);
            message.setSubject("Password Reset OTP");

            message.setText(
                    "Hello,\n\n" +
                            "Your password reset OTP is: " + otp + "\n\n" +
                            "This OTP is valid for 5 minutes.\n\n" +
                            "If you did not request this, please ignore this email.\n\n" +
                            "Regards,\n" +
                            "Student Management System"
            );

            javaMailSender.send(message);

            log.info("Password reset OTP email sent successfully toEmail={}", cleanToEmail);

        } catch (MailException e) {
            log.error("Failed to send password reset OTP toEmail={}. Reason={}", cleanToEmail, e.getMessage());
            throw e;
        }
    }

    public void sendLowAttendanceAlert(String toEmail, String studentName, double attendancePercentage) {

        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.error("Low attendance email failed: toEmail is null or empty");
            return;
        }

        String cleanToEmail = toEmail.trim();
        String cleanFromEmail = fromEmail.trim();

        String safeStudentName = studentName == null || studentName.trim().isEmpty()
                ? "Student"
                : studentName.trim();

        log.info("Preparing low attendance alert email toEmail={}", cleanToEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(cleanFromEmail);
            message.setTo(cleanToEmail);
            message.setSubject("Low Attendance Alert");

            message.setText(
                    "Hello " + safeStudentName + ",\n\n" +
                            "Your current attendance is below the required limit.\n\n" +
                            "Current Attendance: " + String.format("%.0f", attendancePercentage) + "%\n" +
                            "Required Attendance: 75%\n\n" +
                            "Please attend classes regularly to improve your attendance.\n\n" +
                            "Regards,\n" +
                            "Student Management System"
            );

            javaMailSender.send(message);

            log.info("Low attendance alert email sent successfully toEmail={}", cleanToEmail);

        } catch (MailException e) {
            log.error("Failed to send low attendance alert toEmail={}. Reason={}", cleanToEmail, e.getMessage());
        }
    }

    public void sendAttendancePresentNotification(String toEmail, String studentName, LocalDateTime attendanceTime) {

        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.error("Present attendance email failed: toEmail is null or empty");
            return;
        }

        String cleanToEmail = toEmail.trim();
        String cleanFromEmail = fromEmail.trim();

        String safeStudentName = studentName == null || studentName.trim().isEmpty()
                ? "Student"
                : studentName.trim();

        LocalDateTime safeAttendanceTime = attendanceTime == null ? LocalDateTime.now() : attendanceTime;

        String formattedDate = safeAttendanceTime.toLocalDate().format(
                DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        );

        String formattedTime = safeAttendanceTime.format(
                DateTimeFormatter.ofPattern("hh:mm a")
        );

        log.info("Preparing present attendance notification email toEmail={}", cleanToEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(cleanFromEmail);
            message.setTo(cleanToEmail);
            message.setSubject("Attendance Marked Present");

            message.setText(
                    "Hello " + safeStudentName + ",\n\n" +
                            "Your attendance has been marked as PRESENT successfully.\n\n" +
                            "Date: " + formattedDate + "\n" +
                            "Time: " + formattedTime + "\n" +
                            "Status: PRESENT\n\n" +
                            "Regards,\n" +
                            "Student Management System"
            );

            javaMailSender.send(message);

            log.info("Present attendance notification email sent successfully toEmail={}", cleanToEmail);

        } catch (MailException e) {
            log.error("Failed to send present attendance notification toEmail={}. Reason={}", cleanToEmail, e.getMessage());
        }
    }
}
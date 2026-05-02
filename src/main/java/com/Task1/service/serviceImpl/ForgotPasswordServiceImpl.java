package com.Task1.service.serviceImpl;

import com.Task1.entity.PasswordResetOtp;
import com.Task1.model.Student;
import com.Task1.repository.PasswordResetOtpRepository;
import com.Task1.repository.StudentRepository;
import com.Task1.service.EmailService;
import com.Task1.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final StudentRepository studentRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void sendOtp(String email) {

        String cleanEmail = email.trim();

        Optional<Student> studentOptional = studentRepository.findByEmail(cleanEmail);

        if (studentOptional.isEmpty()) {
            throw new RuntimeException("Student not found with email: " + cleanEmail);
        }

        String otp = generateOtp();

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .email(cleanEmail)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        passwordResetOtpRepository.save(passwordResetOtp);

        emailService.sendPasswordResetOtp(cleanEmail, otp);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {

        String cleanEmail = email.trim();
        String cleanOtp = otp.trim();

        PasswordResetOtp passwordResetOtp = passwordResetOtpRepository
                .findTopByEmailOrderByIdDesc(cleanEmail)
                .orElseThrow(() -> new RuntimeException("OTP not found. Please request again."));

        if (passwordResetOtp.isUsed()) {
            throw new RuntimeException("OTP already used. Please request new OTP.");
        }

        if (passwordResetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Please request new OTP.");
        }

        if (!passwordResetOtp.getOtp().equals(cleanOtp)) {
            throw new RuntimeException("Invalid OTP.");
        }

        return true;
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        String cleanEmail = email.trim();

        boolean verified = verifyOtp(cleanEmail, otp);

        if (!verified) {
            throw new RuntimeException("OTP verification failed.");
        }

        Student student = studentRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Student not found with email: " + cleanEmail));

        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);

        PasswordResetOtp passwordResetOtp = passwordResetOtpRepository
                .findTopByEmailOrderByIdDesc(cleanEmail)
                .orElseThrow(() -> new RuntimeException("OTP not found."));

        passwordResetOtp.setUsed(true);
        passwordResetOtpRepository.save(passwordResetOtp);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
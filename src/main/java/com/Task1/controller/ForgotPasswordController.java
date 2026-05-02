package com.Task1.controller;

import com.Task1.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam("email") String email, Model model) {
        try {
            forgotPasswordService.sendOtp(email);
            model.addAttribute("email", email);
            model.addAttribute("success", "OTP sent successfully to your Gmail.");
            return "verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage() {
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp,
            Model model
    ) {
        try {
            forgotPasswordService.verifyOtp(email, otp);
            model.addAttribute("email", email);
            model.addAttribute("otp", otp);
            model.addAttribute("success", "OTP verified successfully.");
            return "reset-password";
        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "verify-otp";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("email", email);
                model.addAttribute("otp", otp);
                model.addAttribute("error", "New password and confirm password do not match.");
                return "reset-password";
            }

            forgotPasswordService.resetPassword(email, otp, newPassword);

            model.addAttribute("success", "Password reset successfully. Please login with new password.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("otp", otp);
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }
}
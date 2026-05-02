package com.Task1.service;

public interface ForgotPasswordService {

    void sendOtp(String email);

    boolean verifyOtp(String email, String otp);

    void resetPassword(String email, String otp, String newPassword);
}
package com.Task1.controller;

import com.Task1.model.Admin;
import com.Task1.model.Student;
import com.Task1.repository.AdminRepository;
import com.Task1.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalPageDataControllerAdvice {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    public GlobalPageDataControllerAdvice(StudentRepository studentRepository,
                                          AdminRepository adminRepository) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
    }

    @ModelAttribute
    public void addLoggedInUserData(Model model, Authentication authentication) {

        model.addAttribute("isAdminRole", false);
        model.addAttribute("isStudentRole", false);

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return;
        }

        boolean isAdminRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        boolean isStudentRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority()));

        String displayName = authentication.getName();
        String profilePicture = null;
        String email = authentication.getName();
        boolean isGoogleUser = false;

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth2User) {
            String googleName = oauth2User.getAttribute("name");
            String googleEmail = oauth2User.getAttribute("email");
            String googlePicture = oauth2User.getAttribute("picture");

            if (googleName != null && !googleName.isBlank()) {
                displayName = googleName;
            }

            if (googleEmail != null && !googleEmail.isBlank()) {
                email = googleEmail;
            }

            profilePicture = googlePicture;
            isGoogleUser = true;
        } else {
            Admin admin = adminRepository.findByEmail(email);

            if (admin != null && admin.getName() != null && !admin.getName().isBlank()) {
                displayName = admin.getName();
            }

            Student student = studentRepository.findByEmail(email).orElse(null);

            if (student != null) {
                if (student.getUsername() != null && !student.getUsername().isBlank()) {
                    displayName = student.getUsername();
                }

                if (student.getProfileImageUrl() != null && !student.getProfileImageUrl().isBlank()) {
                    profilePicture = student.getProfileImageUrl();
                }

                if ("GOOGLE".equalsIgnoreCase(student.getAuthProvider())) {
                    isGoogleUser = true;
                }
            }
        }

        model.addAttribute("loggedInDisplayName", displayName);
        model.addAttribute("loggedInEmail", email);
        model.addAttribute("loggedInProfilePicture", profilePicture);
        model.addAttribute("isGoogleUser", isGoogleUser);
        model.addAttribute("isAdminRole", isAdminRole);
        model.addAttribute("isStudentRole", isStudentRole);
    }
}
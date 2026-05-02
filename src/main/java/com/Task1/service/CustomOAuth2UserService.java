package com.Task1.service;

import com.Task1.model.Admin;
import com.Task1.model.Student;
import com.Task1.repository.AdminRepository;
import com.Task1.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not found from Google account");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("OAUTH2_USER"));

        Admin admin = adminRepository.findByEmail(email);

        if (admin != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
        }

        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            Student newStudent = new Student();

            newStudent.setUsername(generateUniqueUsername(name, email));
            newStudent.setEmail(email);

            // Google login ke liye random password, kyunki user password se login nahi kar raha
            newStudent.setPassword(UUID.randomUUID().toString());

            newStudent.setAge(0);
            newStudent.setSubjects(new ArrayList<>());
            newStudent.setRole("ROLE_STUDENT");
            newStudent.setAuthProvider("GOOGLE");
            newStudent.setProfileImageUrl(picture);

            student = studentService.saveStudent(newStudent);

        } else {
            boolean changed = false;

            if (student.getUsername() == null || student.getUsername().isBlank()) {
                student.setUsername(generateUniqueUsername(name, email));
                changed = true;
            }

            if (student.getAuthProvider() == null || !"GOOGLE".equalsIgnoreCase(student.getAuthProvider())) {
                student.setAuthProvider("GOOGLE");
                changed = true;
            }

            if (picture != null && !picture.equals(student.getProfileImageUrl())) {
                student.setProfileImageUrl(picture);
                changed = true;
            }

            if (changed) {
                studentRepository.save(student);
            }
        }

        authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }

    private String generateUniqueUsername(String name, String email) {

        String baseName;

        if (name != null && !name.isBlank()) {
            baseName = name.trim().replaceAll("\\s+", "_");
        } else {
            baseName = email.split("@")[0];
        }

        String candidate = baseName;
        int counter = 1;

        while (studentRepository.existsByUsername(candidate)) {
            candidate = baseName + "_" + counter;
            counter++;
        }

        return candidate;
    }
}
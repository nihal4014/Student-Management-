package com.Task1.controller;

import com.Task1.model.Admin;
import com.Task1.model.Student;
import com.Task1.service.AdminService;
import com.Task1.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final StudentService studentService;
    private final AdminService adminService;

    public AuthController(StudentService studentService, AdminService adminService) {
        this.studentService = studentService;
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> userData) {
        String role = (String) userData.get("role");
        String email = (String) userData.get("email");

        log.info("Registration request received for email={}, role={}", email, role);

        if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
            Admin savedAdmin = adminService.saveAdmin(toAdmin(userData));
            log.info("Admin registered successfully with email={}", savedAdmin.getEmail());
            return ResponseEntity.ok(savedAdmin);
        } else {
            Student savedStudent = studentService.saveStudent(toStudent(userData));
            log.info("Student registered successfully with email={}, id={}",
                    savedStudent.getEmail(), savedStudent.getId());
            return ResponseEntity.ok(savedStudent);
        }
    }

    private Student toStudent(Map<String, Object> userData) {
        Student student = new Student();
        student.setUsername((String) userData.get("username"));
        student.setEmail((String) userData.get("email"));
        student.setPassword((String) userData.get("password"));
        student.setAge((int) userData.get("age"));

        if (userData.get("subjects") != null) {
            student.setSubjects((List<String>) userData.get("subjects"));
        }

        if (userData.get("rollnumber") != null) {
            student.setRollnumber(Long.valueOf(userData.get("rollnumber").toString()));
        }

        String role = (String) userData.get("role");
        student.setRole((role != null && !role.isEmpty()) ? role : "ROLE_STUDENT");

        return student;
    }

    private Admin toAdmin(Map<String, Object> userData) {
        Admin admin = new Admin();
        admin.setName((String) userData.get("name"));
        admin.setEmail((String) userData.get("email"));
        admin.setPassword((String) userData.get("password"));
        return admin;
    }
}
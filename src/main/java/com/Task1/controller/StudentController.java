package com.Task1.controller;

import com.Task1.model.Student;
import com.Task1.repository.StudentRepository;
import com.Task1.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    private final StudentRepository studentRepository;


    public StudentController(StudentService studentService, StudentRepository studentRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }


    @GetMapping("/profile/{id}")
    public Student getStudentById(@PathVariable Long id) {
        log.info("Fetching student profile for id={}", id);
        Student student = studentService.getStudentById(id);
        log.info("Student profile fetched succesfully for id ={}", id);
        return student;
    }

    @PutMapping("/attendance/{id}")
    public ResponseEntity<String> markMyAttendence(@PathVariable long id, Principal principal) {
        String email = principal.getName();
        log.info("Attendence request recived from email={},pathStudent{}", email, id);

        Student student = studentRepository.findByEmail(email).orElse(null);
        if (student == null) {
            log.error("student not found for email={}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student Not Found");
        }
        if (!student.getId().equals(id)) {
            log.warn("Unauthorized attendance attempt by email={}, requestedId={}, actualId={}",
                    email, id, student.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can mark only your own attendance");
        }
        String response = studentService.markAttendence(student.getId());
        log.info("Attendence response for student={}=>{}", student.getId(), response);
        return ResponseEntity.ok(response);
    }

}

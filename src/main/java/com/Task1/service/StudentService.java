package com.Task1.service;

import com.Task1.model.AttendenceRecord;
import com.Task1.model.Student;
import com.Task1.repository.AttendanceRepository;
import com.Task1.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;

    public StudentService(StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder,
                          AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.attendanceRepository = attendanceRepository;
    }

    public Student saveStudent(Student student) {
        log.info("Saving student with email={}", student.getEmail());

        if (studentRepository.existsByUsername(student.getUsername())) {
            log.warn("Student username already exists username={}", student.getUsername());
            throw new RuntimeException("User " + student.getUsername() + " already exist try another name");
        }

        Random random = new Random();
        Long randomRollNo = 100000L + random.nextInt(900000);
        student.setRollnumber(randomRollNo);

        String hashPassword = passwordEncoder.encode(student.getPassword());
        student.setPassword(hashPassword);

        if (student.getRole() == null || student.getRole().isEmpty()) {
            student.setRole("ROLE_STUDENT");
        }

        Student savedStudent = studentRepository.save(student);
        log.info("Student saved successfully with id={}, email={}", savedStudent.getId(), savedStudent.getEmail());
        return savedStudent;
    }

    public List<Student> getAllStudents() {
        log.info("Fetching all students from database");
        List<Student> students = studentRepository.findAll();
        log.info("Fetched students count={}", students.size());
        return students;
    }

    public String deleteStudent(Long id) {
        log.warn("Delete request received for studentId={}", id);

        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            log.warn("Student deleted successfully for studentId={}", id);
            return "Student with id " + id + " has been delete succesfully";
        } else {
            log.warn("Student not found for delete studentId={}", id);
            return "Student with id " + id + " not found";
        }
    }

    public Student getStudentById(Long id) {
        log.info("Fetching student by id={}", id);
        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Student not found for id={}", id);
                    return new RuntimeException("Student not found with id " + id);
                });
    }

    public Student updateStudent(Long id, Student updateStudent) {
        log.info("Updating student with id={}", id);

        Student updated = studentRepository.findById(id).map(existingStudent -> {
            existingStudent.setUsername(updateStudent.getUsername());
            existingStudent.setEmail(updateStudent.getEmail());
            existingStudent.setAge(updateStudent.getAge());
            existingStudent.setSubjects(updateStudent.getSubjects());
            existingStudent.setAdmissionDate(updateStudent.getAdmissionDate());
            return studentRepository.save(existingStudent);
        }).orElseThrow(() -> {
            log.error("Student not found for update id={}", id);
            return new RuntimeException("Student not found with id " + id);
        });

        log.info("Student updated successfully for id={}", id);
        return updated;
    }

    public String markAttendence(Long id) {
        log.info("Manual attendance marking started for studentId={}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Student not found for attendance studentId={}", id);
                    return new RuntimeException("Student not Found");
                });

        LocalDate today = LocalDate.now();
        Optional<AttendenceRecord> existing = attendanceRepository.findByStudentIdAndDate(student.getId(), today);

        if (existing.isPresent()) {
            log.warn("Attendance already marked for studentId={}, date={}", student.getId(), today);
            return "Attendence is already marked for today!";
        }

        AttendenceRecord record = new AttendenceRecord();
        record.setDate(today);
        record.setStudentId(student.getId());
        record.setStatus("PRESENT");
        record.setLoginCount(1);

        attendanceRepository.save(record);

        student.setPresent(true);
        studentRepository.save(student);

        log.info("Attendance marked successfully for studentId={}, date={}", student.getId(), today);
        return "Attendence marked succesfully for " + today;
    }
}
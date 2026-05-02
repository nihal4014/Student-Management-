package com.Task1.controller;

import com.Task1.model.Admin;
import com.Task1.model.AttendenceRecord;
import com.Task1.model.Student;
import com.Task1.repository.AdminRepository;
import com.Task1.repository.AttendanceRepository;
import com.Task1.repository.StudentRepository;
import com.Task1.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final AttendanceRepository attendanceRepository;

    public AdminController(StudentService studentService,
                           StudentRepository studentRepository,
                           AdminRepository adminRepository,
                           AttendanceRepository attendanceRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @PostMapping("/add-student")
    public Student addStudentByAdmin(@RequestBody Student student, Principal principal) {
        String adminEmail = principal.getName();
        log.info("Admin add-student request recived by adminEmail={}", adminEmail);
        Admin currentAdmin = adminRepository.findByEmail(adminEmail);
        if (currentAdmin == null) {
            log.error("Admin not found for email={}", adminEmail);
            throw new RuntimeException("Admin not found");
        }
        student.setAdmin(currentAdmin);
        Student savedStudent = studentService.saveStudent(student);
        log.info("Student added succesfully by adminEmail ={},studentEmail={},studentId{}", adminEmail, savedStudent.getEmail(), savedStudent.getId());
        return savedStudent;
    }

    @GetMapping("/all")
    public List<Student> getAllStudents() {
        log.info("Fetching all students");
        List<Student> students = studentService.getAllStudents();
        log.info("Fetched total students={}", students.size());
        return students;
    }

    @DeleteMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        log.warn("Delete student request recived for id={}", id);
        String response = studentService.deleteStudent(id);
        log.warn("Delete student response for id ={} => {}", id, response);
        return response;
    }

    @PutMapping("/update/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student student) {
        log.info("Update student request recived for id ={}", id);
        Student updatedStudent = studentService.updateStudent(id, student);
        log.info("Student updated succcesfully  for id={}", id);
        return updatedStudent;
    }

    @GetMapping("/attendance-report/today")
    public List<AttendenceRecord> getTodayAttendanceReport() {
        LocalDate today = LocalDate.now();
        log.info("Fetching today's attendance report for date={}", today);
        List<AttendenceRecord> records = attendanceRepository.findByDate(today);
        log.info("Today's attendance records count={}", records.size());
        return records;
    }

    @GetMapping("/attendance-report/date")
    public List<AttendenceRecord> getAttendanceByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Fetching attendance report for date={}", date);
        List<AttendenceRecord> records = attendanceRepository.findByDate(date);
        log.info("Attendance records found for date={} => {}", date, records.size());
        return records;
    }

    @GetMapping("/attendence-report/present")
    public List<AttendenceRecord> getTodayPresentStudents() {
        LocalDate today = LocalDate.now();
        log.info("Fetching present students for date={}", today);
        List<AttendenceRecord> records = attendanceRepository.findByDateAndStatus(today, "PRESENT");
        log.info("Present students count for date={} => {}", today, records.size());
        return records;
    }

    @GetMapping("/attendence-report/absent")
    public List<AttendenceRecord> getTodayAbsentStudents() {
        LocalDate today = LocalDate.now();
        log.info("Fetching absent students for date={}", today);
        List<AttendenceRecord> records = attendanceRepository.findByDateAndStatus(today, "ABSENT");
        log.info("Absent students count for date={} => {}", today, records.size());
        return records;
    }

    @GetMapping("/attendence-history/{studentId}")
    public List<AttendenceRecord> getHistory(@PathVariable Long studentId) {
        log.info("Fetching attendance history for studentId={}", studentId);
        List<AttendenceRecord> history = attendanceRepository.findByStudentId(studentId);
        log.info("Attendance history count for studentId={} => {}", studentId, history.size());
        return history;
    }

    @GetMapping("/attendence-count/today")
    public ResponseEntity<Map<String, Object>> getTodayAttendanceCount() {
        LocalDate today = LocalDate.now();
        log.info("Fetching today's attendance count for date={}", today);

        long presentCount = attendanceRepository.countByDateAndStatus(today, "PRESENT");
        long absentCount = attendanceRepository.countByDateAndStatus(today, "ABSENT");

        Map<String, Object> response = new HashMap<>();
        response.put("date", today);
        response.put("presentCount", presentCount);
        response.put("absentCount", absentCount);
        response.put("totalmarked", presentCount + absentCount);

        log.info("Attendance count for date={} => present={}, absent={}, total={}",
                today, presentCount, absentCount, presentCount + absentCount);

        return ResponseEntity.ok(response);
    }
}
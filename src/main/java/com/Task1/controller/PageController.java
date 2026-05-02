package com.Task1.controller;

import com.Task1.model.AttendenceRecord;
import com.Task1.model.Student;
import com.Task1.repository.AttendanceRepository;
import com.Task1.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PageController {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public PageController(StudentRepository studentRepository,
                          AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping("/")
    public String publicHomePage() {
        return "home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/home")
    public String homeRedirect(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }

        if (isStudent) {
            return "redirect:/student/dashboard";
        }

        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        LocalDate today = LocalDate.now();

        long totalStudents = studentRepository.count();
        long presentToday = attendanceRepository.countByDateAndStatus(today, "PRESENT");
        long absentToday = attendanceRepository.countByDateAndStatus(today, "ABSENT");
        long markedToday = attendanceRepository.countByDate(today);

        double attendancePercentage = totalStudents == 0
                ? 0.0
                : (markedToday * 100.0) / totalStudents;

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("absentToday", absentToday);
        model.addAttribute("markedToday", markedToday);
        model.addAttribute("attendancePercentage", String.format("%.0f", attendancePercentage));

        return "admin-dashboard";
    }

    @GetMapping("/admin/students-page")
    public String adminStudentsPage(Model model) {
        LocalDate today = LocalDate.now();

        List<Student> students = studentRepository.findAll().stream()
                .sorted(Comparator.comparing(Student::getId))
                .toList();

        List<AttendenceRecord> todayRecords = attendanceRepository.findByDate(today);

        Map<Long, String> todayStatusMap = new HashMap<>();
        for (AttendenceRecord record : todayRecords) {
            todayStatusMap.put(record.getStudentId(), record.getStatus());
        }

        long totalStudents = studentRepository.count();
        long presentToday = attendanceRepository.countByDateAndStatus(today, "PRESENT");
        long markedToday = attendanceRepository.countByDate(today);

        double attendancePercentage = totalStudents == 0
                ? 0.0
                : (markedToday * 100.0) / totalStudents;

        model.addAttribute("students", students);
        model.addAttribute("todayStatusMap", todayStatusMap);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("attendancePercentage", String.format("%.0f", attendancePercentage));

        return "admin-students";
    }

    @GetMapping("/admin/attendance-page")
    public String adminAttendancePage(Model model) {
        LocalDate today = LocalDate.now();

        List<AttendenceRecord> records = attendanceRepository.findByDate(today).stream()
                .sorted(Comparator.comparing(AttendenceRecord::getStudentId))
                .toList();

        long presentToday = attendanceRepository.countByDateAndStatus(today, "PRESENT");
        long absentToday = attendanceRepository.countByDateAndStatus(today, "ABSENT");
        long totalMarked = attendanceRepository.countByDate(today);

        double averageLoginCount = records.stream()
                .mapToInt(record -> record.getLoginCount() == null ? 0 : record.getLoginCount())
                .average()
                .orElse(0.0);

        model.addAttribute("attendanceRecords", records);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("absentToday", absentToday);
        model.addAttribute("totalMarked", totalMarked);
        model.addAttribute("averageLoginCount", String.format("%.1f", averageLoginCount));

        return "attendance-report";
    }

    @GetMapping("/admin/add-student-page")
    public String addStudentPage() {
        return "add-student";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();
        AttendenceRecord todayRecord = attendanceRepository
                .findByStudentIdAndDate(student.getId(), today)
                .orElse(null);

        List<AttendenceRecord> history = attendanceRepository.findByStudentId(student.getId());

        long presentDays = history.stream()
                .filter(record -> "PRESENT".equalsIgnoreCase(record.getStatus()))
                .count();

        long totalDays = history.size();

        double attendancePercentage = totalDays == 0
                ? 0.0
                : (presentDays * 100.0) / totalDays;

        model.addAttribute("student", student);
        model.addAttribute("todayRecord", todayRecord);
        model.addAttribute("attendancePercentage", String.format("%.0f", attendancePercentage));

        return "student-dashboard";
    }

    @GetMapping("/student/attendance-page")
    public String studentAttendancePage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();
        AttendenceRecord todayRecord = attendanceRepository
                .findByStudentIdAndDate(student.getId(), today)
                .orElse(null);

        List<AttendenceRecord> history = attendanceRepository.findByStudentId(student.getId()).stream()
                .sorted(Comparator.comparing(AttendenceRecord::getDate).reversed())
                .toList();

        long presentDays = history.stream()
                .filter(record -> "PRESENT".equalsIgnoreCase(record.getStatus()))
                .count();

        long totalDays = history.size();

        double attendancePercentage = totalDays == 0
                ? 0.0
                : (presentDays * 100.0) / totalDays;

        model.addAttribute("student", student);
        model.addAttribute("todayRecord", todayRecord);
        model.addAttribute("attendanceHistory", history);
        model.addAttribute("attendancePercentage", String.format("%.0f", attendancePercentage));

        return "student-attendance";
    }

    @GetMapping("/student/profile-page")
    public String studentProfilePage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String email = authentication.getName();
        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();
        AttendenceRecord todayRecord = attendanceRepository
                .findByStudentIdAndDate(student.getId(), today)
                .orElse(null);

        List<AttendenceRecord> history = attendanceRepository.findByStudentId(student.getId());

        long presentDays = history.stream()
                .filter(record -> "PRESENT".equalsIgnoreCase(record.getStatus()))
                .count();

        long totalDays = history.size();

        double attendancePercentage = totalDays == 0
                ? 0.0
                : (presentDays * 100.0) / totalDays;

        model.addAttribute("student", student);
        model.addAttribute("todayRecord", todayRecord);
        model.addAttribute("totalAttendanceRecords", totalDays);
        model.addAttribute("presentDays", presentDays);
        model.addAttribute("attendancePercentage", String.format("%.0f", attendancePercentage));

        return "student-profile";
    }
}
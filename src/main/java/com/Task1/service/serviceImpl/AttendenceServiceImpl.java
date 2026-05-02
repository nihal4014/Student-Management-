package com.Task1.service.serviceImpl;

import com.Task1.model.AttendenceRecord;
import com.Task1.model.Student;
import com.Task1.repository.AttendanceRepository;
import com.Task1.repository.StudentRepository;
import com.Task1.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendenceServiceImpl implements AttendanceService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public void markPresentAndIncreaseCount(String email) {
        log.info("Attendance marking started for email={}", email);

        if (email == null || email.trim().isEmpty()) {
            log.error("Attendance marking failed: email is null or empty");
            throw new RuntimeException("Email is null or empty");
        }

        String cleanEmail = email.trim();

        Student student = studentRepository.findByEmail(cleanEmail).orElse(null);

        if (student == null) {
            log.error("Student not found for email={}", cleanEmail);
            throw new RuntimeException("Student not found with email: " + cleanEmail);
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Optional<AttendenceRecord> optionalRecord =
                attendanceRepository.findByStudentIdAndDate(student.getId(), today);

        if (optionalRecord.isPresent()) {
            AttendenceRecord record = optionalRecord.get();

            Integer oldLoginCount = record.getLoginCount() == null ? 0 : record.getLoginCount();

            record.setStatus("PRESENT");
            record.setLastLoginTime(now);
            record.setLoginCount(oldLoginCount + 1);

            if (record.getFirstLoginTime() == null) {
                record.setFirstLoginTime(now);
            }

            attendanceRepository.save(record);

            student.setPresent(true);
            studentRepository.save(student);

            log.info(
                    "Attendance already existed and updated as PRESENT. studentId={}, date={}, loginCount={}",
                    student.getId(),
                    today,
                    record.getLoginCount()
            );
            return;
        }

        AttendenceRecord record = new AttendenceRecord();
        record.setStudentId(student.getId());
        record.setDate(today);
        record.setStatus("PRESENT");
        record.setLoginCount(1);
        record.setFirstLoginTime(now);
        record.setLastLoginTime(now);

        attendanceRepository.save(record);

        student.setPresent(true);
        studentRepository.save(student);

        log.info("Attendance marked successfully for studentId={}, date={}", student.getId(), today);
    }

    @Override
    public String markAttendanceManually(Long studentId) {
        log.info("Manual attendance request for studentId={}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Student not found for studentId={}", studentId);
                    return new RuntimeException("Student Not Found");
                });

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Optional<AttendenceRecord> existing =
                attendanceRepository.findByStudentIdAndDate(student.getId(), today);

        if (existing.isPresent()) {
            AttendenceRecord record = existing.get();

            Integer oldLoginCount = record.getLoginCount() == null ? 0 : record.getLoginCount();

            record.setStatus("PRESENT");
            record.setLastLoginTime(now);
            record.setLoginCount(oldLoginCount + 1);

            if (record.getFirstLoginTime() == null) {
                record.setFirstLoginTime(now);
            }

            attendanceRepository.save(record);

            student.setPresent(true);
            studentRepository.save(student);

            log.warn(
                    "Manual attendance already existed and updated as PRESENT. studentId={}, date={}, loginCount={}",
                    studentId,
                    today,
                    record.getLoginCount()
            );

            return "Attendance already existed, updated as PRESENT for " + today;
        }

        AttendenceRecord record = new AttendenceRecord();
        record.setStudentId(student.getId());
        record.setDate(today);
        record.setStatus("PRESENT");
        record.setLoginCount(1);
        record.setFirstLoginTime(now);
        record.setLastLoginTime(now);

        attendanceRepository.save(record);

        student.setPresent(true);
        studentRepository.save(student);

        log.info("Manual attendance marked successfully for studentId={}, date={}", studentId, today);
        return "Attendance marked successfully for " + today;
    }

    @Override
    public void markAbsentForStudentsWhoDidNotLoginToday() {
        log.info("Absent marking scheduler started");

        LocalDate today = LocalDate.now();
        List<Student> students = studentRepository.findAll();

        int absentCount = 0;

        for (Student student : students) {
            Optional<AttendenceRecord> existing =
                    attendanceRepository.findByStudentIdAndDate(student.getId(), today);

            if (existing.isEmpty()) {
                AttendenceRecord record = new AttendenceRecord();
                record.setStudentId(student.getId());
                record.setDate(today);
                record.setStatus("ABSENT");
                record.setLoginCount(0);
                record.setFirstLoginTime(null);
                record.setLastLoginTime(null);

                attendanceRepository.save(record);

                student.setPresent(false);
                studentRepository.save(student);

                absentCount++;
                log.info("Marked ABSENT for studentId={}", student.getId());
            }
        }

        log.info("Absent marking scheduler completed, totalAbsentMarked={}", absentCount);
    }

    @Override
    public void markAttendanceFromKafka(String email) {

        log.info("Kafka attendance marking started for email={}", email);

        if (email == null || email.trim().isEmpty()) {
            log.error("Kafka attendance marking failed: email is null or empty");
            throw new RuntimeException("Email is null or empty");
        }

        String cleanEmail = email.trim();

        Student student = studentRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> {
                    log.error("Student not found with email={}", cleanEmail);
                    return new RuntimeException("Student not found with email: " + cleanEmail);
                });

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        AttendenceRecord attendanceRecord = attendanceRepository
                .findByStudentIdAndDate(student.getId(), today)
                .orElse(null);

        if (attendanceRecord != null) {
            Integer oldLoginCount = attendanceRecord.getLoginCount() == null
                    ? 0
                    : attendanceRecord.getLoginCount();

            attendanceRecord.setStatus("PRESENT");
            attendanceRecord.setLastLoginTime(now);
            attendanceRecord.setLoginCount(oldLoginCount + 1);

            if (attendanceRecord.getFirstLoginTime() == null) {
                attendanceRecord.setFirstLoginTime(now);
            }

            attendanceRepository.save(attendanceRecord);

            student.setPresent(true);
            studentRepository.save(student);

            log.info(
                    "Attendance updated as PRESENT from Kafka. studentId={}, date={}, loginCount={}",
                    student.getId(),
                    today,
                    attendanceRecord.getLoginCount()
            );
            return;
        }

        AttendenceRecord newRecord = new AttendenceRecord();
        newRecord.setStudentId(student.getId());
        newRecord.setDate(today);
        newRecord.setStatus("PRESENT");
        newRecord.setFirstLoginTime(now);
        newRecord.setLastLoginTime(now);
        newRecord.setLoginCount(1);

        attendanceRepository.save(newRecord);

        student.setPresent(true);
        studentRepository.save(student);

        log.info("Attendance marked PRESENT from Kafka. studentId={}, date={}", student.getId(), today);
    }
}
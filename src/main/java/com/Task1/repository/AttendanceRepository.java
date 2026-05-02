package com.Task1.repository;

import com.Task1.model.AttendenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendenceRecord, Long> {

    List<AttendenceRecord> findByStudentId(Long studentId);

    Optional<AttendenceRecord> findByStudentIdAndDate(Long studentId, LocalDate date);

    List<AttendenceRecord> findByDate(LocalDate date);

    List<AttendenceRecord> findByDateAndStatus(LocalDate date, String status);

    long countByDate(LocalDate date);

    long countByDateAndStatus(LocalDate date, String status);
}
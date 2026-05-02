package com.Task1.repository;

import com.Task1.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;


public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmail(String email);

    Optional<Teacher> findByPhone(String phone);

    Optional<Teacher> findByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmployeeCode(String employeeCode);

    Optional<Teacher> findByIdAndIsDeletedFalse(Long id);

    Page<Teacher> findByIsDeletedFalse(Pageable pageable);

    Page<Teacher> findByTeacherNameContainingIgnoreCaseAndIsDeletedFalse(String teacherName, Pageable pageable);

    Page<Teacher> findByEmailContainingIgnoreCaseAndIsDeletedFalse(String email, Pageable pageable);

    Page<Teacher> findByTeacherNameContainingIgnoreCaseAndIsDeletedFalseOrEmailContainingIgnoreCaseAndIsDeletedFalse(
            String teacherName,
            String email,
            Pageable pageable
    );
}

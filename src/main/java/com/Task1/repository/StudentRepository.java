package com.Task1.repository;

import com.Task1.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
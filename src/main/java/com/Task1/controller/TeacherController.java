package com.Task1.controller;

import com.Task1.dto.TeacherRequestDto;
import com.Task1.dto.TeacherResponseDto;
import com.Task1.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<TeacherResponseDto> addTeacher(@Valid @RequestBody TeacherRequestDto teacherRequestDto) {
        TeacherResponseDto savedTeacher = teacherService.addTeacher(teacherRequestDto);
        return new ResponseEntity<>(savedTeacher, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponseDto> getTeacherById(@PathVariable Long id) {
        TeacherResponseDto teacher = teacherService.getTeacherById(id);
        return ResponseEntity.ok(teacher);
    }

    @GetMapping
    public ResponseEntity<Page<TeacherResponseDto>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Page<TeacherResponseDto> teachers = teacherService.getAllTeachers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(teachers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponseDto> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherRequestDto teacherRequestDto
    ) {
        TeacherResponseDto updatedTeacher = teacherService.updateTeacher(id, teacherRequestDto);
        return ResponseEntity.ok(updatedTeacher);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok("Teacher deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TeacherResponseDto>> searchTeachers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Page<TeacherResponseDto> teachers =
                teacherService.searchTeachers(keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(teachers);
    }
}
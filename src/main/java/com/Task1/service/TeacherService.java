package com.Task1.service;

import com.Task1.dto.TeacherRequestDto;
import com.Task1.dto.TeacherResponseDto;
import org.springframework.data.domain.Page;

public interface TeacherService {

    TeacherResponseDto addTeacher(TeacherRequestDto teacherRequestDto);

    TeacherResponseDto getTeacherById(Long id);

    Page<TeacherResponseDto> getAllTeachers(int page, int size, String sortBy, String sortDir);

    TeacherResponseDto updateTeacher(Long id, TeacherRequestDto teacherRequestDto);

    void deleteTeacher(Long id);

    Page<TeacherResponseDto> searchTeachers(String keyword, int page, int size, String sortBy, String sortDir);
}
package com.Task1.service.serviceImpl;

import com.Task1.dto.TeacherRequestDto;
import com.Task1.dto.TeacherResponseDto;
import com.Task1.exceptions.DuplicateResourceException;
import com.Task1.exceptions.ResourceNotFoundException;
import com.Task1.model.Teacher;
import com.Task1.repository.TeacherRepository;
import com.Task1.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Override
    public TeacherResponseDto addTeacher(TeacherRequestDto teacherRequestDto) {

        if (teacherRepository.existsByEmail(teacherRequestDto.getEmail())) {
            throw new DuplicateResourceException("Teacher already exists with email: " + teacherRequestDto.getEmail());
        }

        if (teacherRepository.existsByPhone(teacherRequestDto.getPhone())) {
            throw new DuplicateResourceException("Teacher already exists with phone: " + teacherRequestDto.getPhone());
        }

        Teacher teacher = Teacher.builder()
                .teacherName(teacherRequestDto.getTeacherName())
                .email(teacherRequestDto.getEmail())
                .phone(teacherRequestDto.getPhone())
                .gender(teacherRequestDto.getGender())
                .qualification(teacherRequestDto.getQualification())
                .specialization(teacherRequestDto.getSpecialization())
                .experienceYears(teacherRequestDto.getExperienceYears())
                .salary(teacherRequestDto.getSalary())
                .joiningDate(teacherRequestDto.getJoiningDate())
                .address(teacherRequestDto.getAddress())
                .status(teacherRequestDto.getStatus())
                .employeeCode(generateUniqueEmployeeCode())
                .isDeleted(false)
                .build();

        Teacher savedTeacher = teacherRepository.save(teacher);

        return mapToResponseDto(savedTeacher);
    }

    @Override
    public TeacherResponseDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        return mapToResponseDto(teacher);
    }

    @Override
    public Page<TeacherResponseDto> getAllTeachers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Teacher> teacherPage = teacherRepository.findByIsDeletedFalse(pageable);

        return teacherPage.map(this::mapToResponseDto);
    }

    @Override
    public TeacherResponseDto updateTeacher(Long id, TeacherRequestDto teacherRequestDto) {
        Teacher existingTeacher = teacherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        teacherRepository.findByEmail(teacherRequestDto.getEmail()).ifPresent(teacher -> {
            if (!teacher.getId().equals(id)) {
                throw new DuplicateResourceException("Teacher already exists with email: " + teacherRequestDto.getEmail());
            }
        });

        teacherRepository.findByPhone(teacherRequestDto.getPhone()).ifPresent(teacher -> {
            if (!teacher.getId().equals(id)) {
                throw new DuplicateResourceException("Teacher already exists with phone: " + teacherRequestDto.getPhone());
            }
        });

        existingTeacher.setTeacherName(teacherRequestDto.getTeacherName());
        existingTeacher.setEmail(teacherRequestDto.getEmail());
        existingTeacher.setPhone(teacherRequestDto.getPhone());
        existingTeacher.setGender(teacherRequestDto.getGender());
        existingTeacher.setQualification(teacherRequestDto.getQualification());
        existingTeacher.setSpecialization(teacherRequestDto.getSpecialization());
        existingTeacher.setExperienceYears(teacherRequestDto.getExperienceYears());
        existingTeacher.setSalary(teacherRequestDto.getSalary());
        existingTeacher.setJoiningDate(teacherRequestDto.getJoiningDate());
        existingTeacher.setAddress(teacherRequestDto.getAddress());
        existingTeacher.setStatus(teacherRequestDto.getStatus());

        Teacher updatedTeacher = teacherRepository.save(existingTeacher);

        return mapToResponseDto(updatedTeacher);
    }

    @Override
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        teacher.setIsDeleted(true);
        teacherRepository.save(teacher);
    }

    @Override
    public Page<TeacherResponseDto> searchTeachers(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Teacher> teacherPage =
                teacherRepository.findByTeacherNameContainingIgnoreCaseAndIsDeletedFalseOrEmailContainingIgnoreCaseAndIsDeletedFalse(
                        keyword, keyword, pageable
                );

        return teacherPage.map(this::mapToResponseDto);
    }

    private TeacherResponseDto mapToResponseDto(Teacher teacher) {
        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .teacherName(teacher.getTeacherName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .gender(teacher.getGender())
                .qualification(teacher.getQualification())
                .specialization(teacher.getSpecialization())
                .experienceYears(teacher.getExperienceYears())
                .salary(teacher.getSalary())
                .joiningDate(teacher.getJoiningDate())
                .address(teacher.getAddress())
                .status(teacher.getStatus())
                .employeeCode(teacher.getEmployeeCode())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    private String generateUniqueEmployeeCode() {
        Random random = new Random();
        String employeeCode;

        do {
            employeeCode = "TCH" + (1000 + random.nextInt(9000));
        } while (teacherRepository.existsByEmployeeCode(employeeCode));

        return employeeCode;
    }
}
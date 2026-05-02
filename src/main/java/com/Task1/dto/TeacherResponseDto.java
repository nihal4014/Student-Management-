package com.Task1.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponseDto {

    private Long id;
    private String teacherName;
    private String email;
    private String phone;
    private String gender;
    private String qualification;
    private String specialization;
    private Integer experienceYears;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private String address;
    private String status;
    private String employeeCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

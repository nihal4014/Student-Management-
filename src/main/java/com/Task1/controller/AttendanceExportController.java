package com.Task1.controller;

import com.Task1.model.AttendenceRecord;
import com.Task1.model.Student;
import com.Task1.repository.AttendanceRepository;
import com.Task1.repository.StudentRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AttendanceExportController {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @GetMapping("/admin/students/{studentId}/attendance/export")
    public void exportStudentAttendanceForAdmin(
            @PathVariable Long studentId,
            HttpServletResponse response
    ) throws IOException {

        log.info("Admin attendance export request started for studentId={}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Attendance export failed: student not found with studentId={}", studentId);
                    return new RuntimeException("Student not found with id: " + studentId);
                });

        List<AttendenceRecord> attendanceRecords = attendanceRepository.findByStudentId(studentId)
                .stream()
                .sorted(Comparator.comparing(AttendenceRecord::getDate).reversed())
                .toList();

        log.info(
                "Attendance records fetched for export. studentId={}, email={}, totalRecords={}",
                student.getId(),
                student.getEmail(),
                attendanceRecords.size()
        );

        exportAttendanceExcel(student, attendanceRecords, response);

        log.info(
                "Admin attendance export completed successfully for studentId={}, email={}",
                student.getId(),
                student.getEmail()
        );
    }

    @GetMapping("/student/attendance/export")
    public void exportOwnAttendance(
            Authentication authentication,
            HttpServletResponse response
    ) throws IOException {

        log.info("Student attendance export request started");

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Attendance export failed: user is not authenticated");
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();

        log.info("Fetching student for attendance export. email={}", email);

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Attendance export failed: student not found with email={}", email);
                    return new RuntimeException("Student not found with email: " + email);
                });

        List<AttendenceRecord> attendanceRecords = attendanceRepository.findByStudentId(student.getId())
                .stream()
                .sorted(Comparator.comparing(AttendenceRecord::getDate).reversed())
                .toList();

        log.info(
                "Attendance records fetched for student export. studentId={}, email={}, totalRecords={}",
                student.getId(),
                student.getEmail(),
                attendanceRecords.size()
        );

        exportAttendanceExcel(student, attendanceRecords, response);

        log.info(
                "Student attendance export completed successfully. studentId={}, email={}",
                student.getId(),
                student.getEmail()
        );
    }

    private void exportAttendanceExcel(
            Student student,
            List<AttendenceRecord> attendanceRecords,
            HttpServletResponse response
    ) throws IOException {

        log.info(
                "Excel generation started for studentId={}, email={}, totalRecords={}",
                student.getId(),
                student.getEmail(),
                attendanceRecords.size()
        );

        String fileName = "attendance_" + student.getEmail().replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        Workbook workbook = new XSSFWorkbook();

        try {
            Sheet sheet = workbook.createSheet("Attendance Report");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);

            int rowIndex = 0;

            Row titleRow = sheet.createRow(rowIndex++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Student Attendance Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

            rowIndex++;

            Row studentNameRow = sheet.createRow(rowIndex++);
            studentNameRow.createCell(0).setCellValue("Student Name");
            studentNameRow.createCell(1).setCellValue(student.getUsername() == null ? "N/A" : student.getUsername());

            Row studentEmailRow = sheet.createRow(rowIndex++);
            studentEmailRow.createCell(0).setCellValue("Email");
            studentEmailRow.createCell(1).setCellValue(student.getEmail() == null ? "N/A" : student.getEmail());

            Row studentRollRow = sheet.createRow(rowIndex++);
            studentRollRow.createCell(0).setCellValue("Roll Number");
            studentRollRow.createCell(1).setCellValue(student.getRollnumber() == null ? "N/A" : String.valueOf(student.getRollnumber()));

            rowIndex++;

            Row headerRow = sheet.createRow(rowIndex++);

            String[] headers = {
                    "S.No",
                    "Date",
                    "Status",
                    "First Login Time",
                    "Last Login Time",
                    "Login Count",
                    "Student ID"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int serialNumber = 1;

            for (AttendenceRecord record : attendanceRecords) {
                Row row = sheet.createRow(rowIndex++);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(serialNumber++);
                cell0.setCellStyle(normalStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(record.getDate() == null ? "N/A" : record.getDate().format(DATE_FORMATTER));
                cell1.setCellStyle(normalStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(record.getStatus() == null ? "N/A" : record.getStatus());
                cell2.setCellStyle(normalStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(record.getFirstLoginTime() == null ? "N/A" : record.getFirstLoginTime().format(DATE_TIME_FORMATTER));
                cell3.setCellStyle(normalStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(record.getLastLoginTime() == null ? "N/A" : record.getLastLoginTime().format(DATE_TIME_FORMATTER));
                cell4.setCellStyle(normalStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(record.getLoginCount() == null ? 0 : record.getLoginCount());
                cell5.setCellStyle(normalStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(record.getStudentId() == null ? "N/A" : String.valueOf(record.getStudentId()));
                cell6.setCellStyle(normalStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());

            log.info(
                    "Excel generation completed. fileName={}, studentId={}, totalRows={}",
                    fileName,
                    student.getId(),
                    attendanceRecords.size()
            );

        } catch (IOException e) {
            log.error(
                    "Excel generation failed for studentId={}, email={}. Reason={}",
                    student.getId(),
                    student.getEmail(),
                    e.getMessage(),
                    e
            );
            throw e;

        } finally {
            workbook.close();
            log.info("Workbook closed successfully for studentId={}", student.getId());
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }
}
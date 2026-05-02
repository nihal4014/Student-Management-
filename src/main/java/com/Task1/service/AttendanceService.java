package com.Task1.service;

public interface AttendanceService {

    void markPresentAndIncreaseCount(String email);

    String markAttendanceManually(Long studentId);

    void markAbsentForStudentsWhoDidNotLoginToday();

    void markAttendanceFromKafka(String email);

}
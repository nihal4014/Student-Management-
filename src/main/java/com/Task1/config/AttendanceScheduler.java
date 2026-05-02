package com.Task1.config;

import com.Task1.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {
    private final AttendanceService attendanceService;

    @Scheduled(cron = "0 59 23 * * *")
    public void markAbsentStudentsDaily(){
        attendanceService.markAbsentForStudentsWhoDidNotLoginToday();
    }
}

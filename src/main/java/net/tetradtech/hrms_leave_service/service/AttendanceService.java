package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.AttendanceCalendarDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceSummaryDTO;


import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDTO clockIn(Long userId);
    AttendanceDTO clockOut(Long userId);
    List<AttendanceDTO> getAttendanceForCalendar(Long userId, int year, int month);
    void autoMarkAbsentees();
    List<AttendanceDTO> getAllUserDailyLogs(LocalDate date);
    AttendanceDTO getUserDailyLog(Long userId, LocalDate date);

    List<AttendanceDTO> getMonthlyCalendar(Long userId, int year, int month);
}
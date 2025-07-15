package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDTO clockIn(Long userId);
    AttendanceDTO clockOut(Long userId);
    AttendanceSummaryDTO getMonthlySummary(Long userId, int year, int month);
    List<AttendanceSummaryDTO> getMonthlySummaryForAllUsers(int year, int month);

    List<AttendanceDTO> getDailyLogs(Long userId);
    List<AttendanceDTO> getAllDailyLogs();

}

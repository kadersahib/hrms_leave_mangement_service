package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.model.AttendanceRecord;


import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDTO clockIn(Long userId);
    AttendanceDTO clockOut(Long userId);
    void autoMarkAbsentees();
    List<AttendanceDTO> getAllUserDailyLogs(LocalDate date);
    AttendanceDTO getUserDailyLog(Long userId, LocalDate date);
    List<AttendanceDTO> getAllAttendanceRecords();
    int getDailyPresentCount(LocalDate date);
    void deleteRecentAttendanceByUserId(Long userId);

//    List<AttendanceDTO> getAttendanceByDesignation(String designation);
//    List<AttendanceDTO> getAttendanceByDesignationAndDateRange(String designation, LocalDate startDate, LocalDate endDate);
//    List<String> getAllDesignations();

}
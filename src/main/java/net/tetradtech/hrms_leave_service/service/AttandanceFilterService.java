package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.AttendanceDTO;
import net.tetradtech.hrms_leave_service.dto.AttendanceStatsDTO;

import java.time.LocalDate;
import java.util.List;

public interface AttandanceFilterService {
    List<AttendanceDTO> getFilterByDesignation(String designation);
    List<AttendanceDTO> getFilterByDesignationAndDateRange(String designation, LocalDate startDate, LocalDate endDate);

    //Total Summary
    List<AttendanceStatsDTO> getAllUserAttendanceStats();
    AttendanceStatsDTO getUserAttendanceStats(Long userId);

}

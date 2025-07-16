package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.CalendarAttendanceDTO;

import java.util.List;

public interface LeaveCalenderService {
    List<CalendarAttendanceDTO> getUserMonthlyCalendar(Long userId, int year, int month);
    List<CalendarAttendanceDTO> getAllUsersMonthlyCalendar(int year, int month);
}

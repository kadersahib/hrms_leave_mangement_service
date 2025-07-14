package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveCalendarDTO;

import java.util.List;

public interface LeaveCalendarService {
    List<LeaveCalendarDTO> getCalendarData();
    List<LeaveCalendarDTO> getCalendarDataByUser(Long userId);

}

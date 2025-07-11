package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.model.LeaveApplication;

import java.time.LocalDate;
import java.util.List;

public interface LeaveApplicationStatusService {
    List<LeaveApplication> filterByStatus(String status);
    List<LeaveApplication> filterByUserId(Long userId);
    List<LeaveApplication> filterByLeaveType(Long leaveTypeId);
    List<LeaveApplication> filterByUserIdAndStatus(Long userId, String status);
    List<LeaveApplication> filterByDateRange(Long userId,LocalDate startDate, LocalDate endDate);

}

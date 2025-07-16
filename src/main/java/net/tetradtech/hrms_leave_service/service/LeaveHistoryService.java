package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveHistoryDTO;

import java.util.List;

public interface LeaveHistoryService {
    List<LeaveHistoryDTO> getLeaveHistoryByUserId(Long userId);
    List<LeaveHistoryDTO> getAllUsersLeaveHistory();
}

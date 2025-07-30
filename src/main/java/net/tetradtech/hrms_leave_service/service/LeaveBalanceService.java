package net.tetradtech.hrms_leave_service.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface LeaveBalanceService {
    List<Map<String, Object>> getAllLeaves();
    List<Map<String, Object>>getLeavesByUserId(Long userId);
    Map<String, Object> getLeaveBalanceByUserIdAndLeaveType(Long userId, Long leaveTypeId);

}

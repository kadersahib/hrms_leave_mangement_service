package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveBalanceDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LeaveBalanceService {
    List<LeaveBalanceDTO> getAllLeaves();
    List<LeaveBalanceDTO> getLeavesByUserId(Long userId);
    List<LeaveBalanceDTO> getLeaveBalanceByUserIdAndLeaveType(Long userId, Long leaveTypeId);

}

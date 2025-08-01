package net.tetradtech.hrms_leave_service.service;


import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;

import java.util.List;


public interface LeaveApprovalService {

    LeaveApplication approveOrRejectLeave(Long leaveId, LeaveApprovalDTO request);
    LeaveApplication changeLeaveStatus(Long leaveId, Long approverId, String comment);
    List<LeaveApplication> getAllApprovals();
    List<LeaveApplication> getByUserId(Long userId);
    void deleteApprovalById(Long id);

}

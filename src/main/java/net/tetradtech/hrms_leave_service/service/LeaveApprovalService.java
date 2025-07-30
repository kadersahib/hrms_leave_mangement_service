package net.tetradtech.hrms_leave_service.service;


import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;


public interface LeaveApprovalService {

    LeaveApplication approveOrRejectLeave(Long leaveId, LeaveApprovalDTO request);


}

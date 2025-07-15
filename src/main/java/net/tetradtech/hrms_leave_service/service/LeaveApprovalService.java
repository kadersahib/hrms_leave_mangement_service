package net.tetradtech.hrms_leave_service.service;


import net.tetradtech.hrms_leave_service.dto.LeaveApprovalDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;

import java.util.List;

public interface LeaveApprovalService {

    LeaveApplication performAction(Long leaveId, LeaveApprovalDTO dto);
    LeaveApplication updateApproval(Long id , LeaveApprovalDTO dto);
    List<LeaveApplication> getAll();
    LeaveApplication getById(Long id);
    void deleteById(Long id);

}

package net.tetradtech.hrms_leave_service.service;


import net.tetradtech.hrms_leave_service.dto.LeaveApprovalUpdateDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApproval;

import java.util.List;

public interface LeaveApprovalService {

    LeaveApproval performAction(LeaveApproval dto);
    LeaveApproval updateApproval(Long id , LeaveApprovalUpdateDTO dto);
    List<LeaveApproval> getAll();
    LeaveApproval getById(Long id);
    void deleteById(Long id);

}

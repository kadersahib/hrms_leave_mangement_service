package net.tetradtech.hrms_leave_service.service;


import net.tetradtech.hrms_leave_service.model.LeaveApproval;

import java.util.List;

public interface LeaveApprovalService {

    LeaveApproval performAction(LeaveApproval dto);
    LeaveApproval updateApproval(Long id , LeaveApproval dto);
    List<LeaveApproval> getAll();
    LeaveApproval getById(Long id);
    void deleteById(Long id);

}

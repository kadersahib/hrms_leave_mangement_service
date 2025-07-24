package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.dto.LeaveUpdateRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;

import java.util.List;

public interface LeaveApplicationService {

    LeaveApplication applyLeave(LeaveRequestDTO application);

    LeaveApplication updateLeave(Long id, LeaveUpdateRequestDTO updatedData);

    List<LeaveApplication> getAllLeaves();

    List<LeaveApplication> getLeavesByUserId(Long userId);

    void deleteLatestLeaveByUserId(Long userId);

    LeaveApplication cancelLeave(Long userId);

}
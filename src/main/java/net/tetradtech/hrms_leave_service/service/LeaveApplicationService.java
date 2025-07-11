package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveCancelDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;

import java.util.List;
import java.util.Optional;

public interface LeaveApplicationService {

    LeaveApplication applyLeave(LeaveApplication application);

    LeaveApplication updateLeave(Long id, LeaveApplication updatedData);

    List<LeaveApplication> getAllLeaves();//active data only

    Optional<LeaveApplication> getUpdateByUserId(Long userId);

    List<LeaveApplication> getLeavesByUserId(Long userId);

    void deleteLatestLeaveByUserId(Long userId);

    LeaveApplication cancelLeave(LeaveCancelDTO dto);

}
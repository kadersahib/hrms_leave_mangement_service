package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.dto.LeaveRequestDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;

import java.util.List;
import java.util.Optional;

public interface LeaveApplicationService {

    LeaveApplication applyLeave(LeaveRequestDTO application);

    LeaveApplication updateLeave(Long leaveId, LeaveRequestDTO updatedData);

    List<LeaveApplication> getAllLeaves();//active data only

    Optional<LeaveApplication> getUpdateByUserId(Long userId);

    List<LeaveApplication> getLeavesByUserId(Long userId);

    void deleteLatestLeaveByUserId(Long userId);

    LeaveApplication cancelLeave(Long userId);

}
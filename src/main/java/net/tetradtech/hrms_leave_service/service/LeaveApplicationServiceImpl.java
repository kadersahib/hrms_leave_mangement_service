
package net.tetradtech.hrms_leave_service.service;

import net.tetradtech.hrms_leave_service.client.LeaveTypeClient;
import net.tetradtech.hrms_leave_service.client.UserServiceClient;
import net.tetradtech.hrms_leave_service.dto.LeaveTypeDTO;
import net.tetradtech.hrms_leave_service.dto.UserDTO;
import net.tetradtech.hrms_leave_service.model.LeaveApplication;
import net.tetradtech.hrms_leave_service.model.LeaveStatus;
import net.tetradtech.hrms_leave_service.repository.LeaveApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Autowired
    private LeaveApplicationRepository leaveApplicationRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private LeaveTypeClient leaveTypeClient;

    private static final String SYSTEM_USER = "system";

    @Override
    public LeaveApplication applyLeave(LeaveApplication application) {

        //  Validate the userId
        UserDTO user = userServiceClient.getUserById(application.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + application.getUserId());
        }
        // Validate leaveTypeId using leaveTypeClient
        LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(application.getLeaveTypeId());
        if (leaveType == null) {
            throw new IllegalArgumentException("Invalid leave type ID: " + application.getLeaveTypeId());
        }

        if (!leaveType.isActive()){
            throw new IllegalArgumentException("Leave Type is inactive: " + leaveType.getName());

        }

        long requestedDays = ChronoUnit.DAYS.between(application.getStartDate(), application.getEndDate()) + 1;
        if (requestedDays > leaveType.getMaxDays()) {
            throw new IllegalArgumentException("Requested days (" + requestedDays + ") exceed max allowed (" + leaveType.getMaxDays() + ") for leave type: " + leaveType.getName());
        }



    boolean exists = leaveApplicationRepository.findAll().stream()
        .filter(l -> !l.isDeleted())
        .anyMatch(existing ->
                existing.getUserId().equals(application.getUserId()) &&
                        existing.getStartDate().equals(application.getStartDate()) &&
                        existing.getEndDate().equals(application.getEndDate()));

    if (exists) {
        throw new IllegalArgumentException("Leave already applied for this date range.");
    }

    application.setCreatedAt(LocalDateTime.now());
//        application.setCreatedBy(user.getName());
    application.setCreatedBy(SYSTEM_USER);
    application.setStatus(LeaveStatus.PENDING);
    application.setDeleted(false);
    return leaveApplicationRepository.save(application);
    }

    @Override
    public LeaveApplication updateLeave(Long id, LeaveApplication updatedData) {
        LeaveApplication leave = leaveApplicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave application not found with ID: " + id));


        Long userId = updatedData.getUserId() != null ? updatedData.getUserId() : leave.getUserId();

        UserDTO user = userServiceClient.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        LeaveTypeDTO leaveType = leaveTypeClient.getLeaveTypeById(updatedData.getLeaveTypeId());
        if (leaveType == null) {
            throw new IllegalArgumentException("Invalid leave type ID: " + updatedData.getLeaveTypeId());
        }

        if (!leaveType.isActive()) {
            throw new IllegalArgumentException("Leave Type is inactive: " + leaveType.getName());
        }
        long requestedDays = ChronoUnit.DAYS.between(updatedData.getStartDate(), updatedData.getEndDate()) + 1;
        if (requestedDays > leaveType.getMaxDays()) {
            throw new IllegalArgumentException("Requested days (" + requestedDays + ") exceed max allowed ("
                    + leaveType.getMaxDays() + ") for leave type: " + leaveType.getName());
        }

        boolean overlap = leaveApplicationRepository.findAll().stream()
                .filter(l -> !l.isDeleted() && !l.getId().equals(id))
                .anyMatch(l ->
                l.getUserId().equals(userId) &&
                !l.getStartDate().isAfter(updatedData.getEndDate()) &&
                !l.getEndDate().isBefore(updatedData.getStartDate()));

        if (overlap) {
            throw new IllegalArgumentException("Updated leave dates overlap with an existing leave.");
        }

        leave.setUserId(userId);
        leave.setLeaveTypeId(updatedData.getLeaveTypeId());
        leave.setReason(updatedData.getReason());
        leave.setStartDate(updatedData.getStartDate());
        leave.setEndDate(updatedData.getEndDate());
        leave.setStatus(updatedData.getStatus());
        leave.setReportingManager(updatedData.getReportingManager());
        leave.setMaxDays(updatedData.getMaxDays());
//        leave.setUpdatedBy(user.getName());
        leave.setUpdatedBy(SYSTEM_USER);
        leave.setUpdatedAt(LocalDateTime.now());

        return leaveApplicationRepository.save(leave);
    }


    @Override
    public Optional<LeaveApplication> getUpdateByUserId(Long userId) {
        return leaveApplicationRepository.findAll().stream()
                .filter(l -> l.getUserId().equals(userId) && !l.isDeleted())
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .findFirst();
    }


    @Override
    public List<LeaveApplication> getLeavesByUserId(Long userId) {
        // Validate the user exists
        userServiceClient.getUserById(userId);
        return leaveApplicationRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    // Return only active (non-deleted) leaves
    @Override
    public List<LeaveApplication> getAllLeaves() {
        return leaveApplicationRepository.findByIsDeletedFalse();
    }

    // Return all leaves including soft-deleted
//    @Override
//    public List<LeaveApplication> getAllLeavesIncludingDeleted() {
//        return leaveApplicationRepository.findAll();
//    }

    @Override
    public void deleteLatestLeaveByUserId(Long userId) {
        // Validate user exists
        UserDTO user = userServiceClient.getUserById(userId);

        // Fetch the latest non-deleted leave
        LeaveApplication latest = leaveApplicationRepository
                .findTopByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("No recent leave found for user"));

        latest.setDeleted(true);
        latest.setDeletedAt(LocalDateTime.now());
//        latest.setDeletedBy(user.getName());
        latest.setDeletedBy(SYSTEM_USER);
        leaveApplicationRepository.save(latest);
    }


}

